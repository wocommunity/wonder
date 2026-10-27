#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <stdatomic.h>
#include <limits.h>
#include <signal.h>

#include "config.h"
#include "request.h"
#include "response.h"
#include "transaction.h"
#include "appcfg.h"
#include "strtbl.h"
#include "MoreURLCUtilities.h"
#include "log.h"
#include "Platform.h"
#include "listing.h"

#define DEFAULT_WORKER_THREADS 10
#define DEFAULT_REQUESTS_PER_THREAD 20
/* Set size > 1MB (1048576) so REQUEST_STREAMED_THRESHOLD is exceeded and getMoreContent is actually called! */
#define UPLOAD_BODY_SIZE (1024 * 1024 + 131072) /* 1 MB + 128 KB */

char *WA_adaptorName = "TestAdaptorRunner/1.0";

static atomic_int total_sent = 0;
static atomic_int total_success = 0;
static atomic_int total_failed = 0;
static atomic_int headers_without_colon = 0;
static atomic_int corrupt_load_average = 0;

static char *shared_body_data = NULL;
static int requests_per_thread = DEFAULT_REQUESTS_PER_THREAD;

static void crash_handler(int sig) {
    const char *name = (sig == SIGABRT) ? "SIGABRT (Memory corruption / Double-free)" :
                       (sig == SIGSEGV) ? "SIGSEGV (Segmentation fault / Null pointer)" :
                       (sig == SIGBUS)  ? "SIGBUS (Bus error / Memory alignment)" : "UNKNOWN SIGNAL";
    fprintf(stderr, "\n=======================================================\n");
    fprintf(stderr, "❌ TEST FAILED: Caught %s (Signal %d)!\n", name, sig);
    fprintf(stderr, "   Adaptor crashed during concurrent request handling.\n");
    fprintf(stderr, "=======================================================\n");
    _exit(2);
}

typedef struct {
    long long total_size;
    long long bytes_sent;
} StreamCtx;

typedef struct {
    long thread_id;
    const char *url;
    const char *config_path;
} WorkerArgs;

/* Callback to stream more content when content_length > 1MB */
static int test_getMoreContent(void *handle, void *buffer, int bufferSize, int mustFill) {
    HTTPRequest *req = (HTTPRequest *)handle;
    StreamCtx *ctx = (StreamCtx *)req->api_handle;
    long long remaining = ctx->total_size - ctx->bytes_sent;
    if (remaining <= 0) return 0;
    int to_read = (remaining > bufferSize) ? bufferSize : (int)remaining;
    memset(buffer, 'Y', to_read);
    ctx->bytes_sent += to_read;
    return to_read;
}

void *worker_func(void *arg) {
    WorkerArgs *args = (WorkerArgs *)arg;
    long tid = args->thread_id;
    int i;

    for (i = 0; i < requests_per_thread; i++) {
        HTTPRequest *req = req_new("POST", NULL);
        if (!req) {
            fprintf(stderr, "[Thread %ld] Failed to create HTTPRequest\n", tid);
            atomic_fetch_add(&total_failed, 1);
            continue;
        }

        /* Set up 1MB+ POST body with streaming */
        StreamCtx sctx;
        sctx.total_size = UPLOAD_BODY_SIZE;
        sctx.bytes_sent = 0;
        req->content_length = UPLOAD_BODY_SIZE;
        req_allocateContent(req, req->content_length, 1);
        if (req->content && req->content_buffer_size > 0) {
            memcpy(req->content, shared_body_data, req->content_buffer_size);
            sctx.bytes_sent = req->content_buffer_size;
        }
        req->api_handle = &sctx;
        req->getMoreContent = (req_getMoreContentCallback)test_getMoreContent;

        /* Set standard headers */
        req_addHeader(req, "Content-Type", "application/octet-stream", 0);
        char len_str[32];
        snprintf(len_str, sizeof(len_str), "%d", UPLOAD_BODY_SIZE);
        req_addHeader(req, "Content-Length", len_str, 0);
        req_addHeader(req, "User-Agent", "TestAdaptorRunner/1.0", 0);

        /* Set up URL components */
        WOURLComponents wc = WOURLComponents_Initializer;
        WOURLError err = WOParseApplicationName(&wc, args->url);
        if (err != WOURLOK) {
            fprintf(stderr, "[Thread %ld] Failed to parse URL: %s\n", tid, args->url);
            req_free(req);
            atomic_fetch_add(&total_failed, 1);
            continue;
        }

        atomic_fetch_add(&total_sent, 1);

        /* Execute transaction */
        HTTPResponse *resp = tr_handleRequest(req, args->url, &wc, "HTTP/1.1", NULL);

        if (resp == NULL) {
            fprintf(stderr, "[Thread %ld] tr_handleRequest returned NULL (request %d failed)\n", tid, i);
            atomic_fetch_add(&total_failed, 1);
        } else {
            /* Check status */
            if (resp->status != 200) {
                fprintf(stderr, "[Thread %ld] Unexpected status: %d (msg: %s, body: %s)\n", tid, resp->status, resp->statusMsg ? resp->statusMsg : "", resp->content ? (char *)resp->content : "");
                atomic_fetch_add(&total_failed, 1);
            } else {
                /* If load average was NOT removed, or was truncated, that's an error.
                   Note: transaction.c strips LOAD_AVERAGE_HEADER upon successful processing! */
                const char *trunc = st_valueFor(resp->headers, "x-webobjects-loa");
                if (trunc != NULL) {
                    fprintf(stderr, "[Thread %ld] TRUNCATED HEADER FOUND IN HEADERS: 'x-webobjects-loa'!\n", tid);
                    atomic_fetch_add(&headers_without_colon, 1);
                }

                /* Check other expected headers */
                const char *ctype = st_valueFor(resp->headers, "content-type");
                if (ctype == NULL) {
                    fprintf(stderr, "[Thread %ld] Missing content-type header!\n", tid);
                    atomic_fetch_add(&total_failed, 1);
                }

                /* Drain response content */
                while (resp->content_read < resp->content_length) {
                    int count = resp_getResponseContent(resp, 1);
                    if (count <= 0) break;
                    resp->content_read += count;
                }

                atomic_fetch_add(&total_success, 1);
            }
            resp_free(resp);
        }

        req_free(req);
    }

    return NULL;
}

int main(int argc, char **argv) {
    int num_threads = DEFAULT_WORKER_THREADS;
    requests_per_thread = DEFAULT_REQUESTS_PER_THREAD;

    if (argc > 1) {
        requests_per_thread = atoi(argv[1]);
        if (requests_per_thread <= 0) requests_per_thread = DEFAULT_REQUESTS_PER_THREAD;
    }
    if (argc > 2) {
        num_threads = atoi(argv[2]);
        if (num_threads <= 0) num_threads = DEFAULT_WORKER_THREADS;
    }

    /* Set up signal handlers */
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = crash_handler;
    sigaction(SIGABRT, &sa, NULL);
    sigaction(SIGSEGV, &sa, NULL);
    sigaction(SIGBUS, &sa, NULL);

    char config_url[1024];
    char cwd[512];
    getcwd(cwd, sizeof(cwd));

    if (access("test_woadaptor.xml", R_OK) == 0) {
        snprintf(config_url, sizeof(config_url), "file://%s/test_woadaptor.xml", cwd);
    } else if (access("testsuite/test_woadaptor.xml", R_OK) == 0) {
        snprintf(config_url, sizeof(config_url), "file://%s/testsuite/test_woadaptor.xml", cwd);
    } else {
        snprintf(config_url, sizeof(config_url), "file://%s/test_woadaptor.xml", cwd);
    }
    printf("=== Starting Adaptor 1MB POST Stress Test ===\n");
    printf("Config URI: %s\n", config_url);
    printf("Threads: %d, Requests per thread: %d, Body size: %d bytes (1.125 MB streaming)\n",
           num_threads, requests_per_thread, UPLOAD_BODY_SIZE);

    /* Allocate dummy 1MB body */
    shared_body_data = (char *)malloc(UPLOAD_BODY_SIZE);
    memset(shared_body_data, 'X', UPLOAD_BODY_SIZE);

    /* Initialize Adaptor options */
    strtbl *options = st_new(8);
    st_add(options, WOCONFIG, config_url, 0);
    char log_file_path[1024];
    snprintf(log_file_path, sizeof(log_file_path), "%s/test_adaptor.log", cwd);
    unlink(log_file_path);
    st_add(options, WOLOGPATH, log_file_path, 0);
    st_add(options, WOLOGLEVEL, "DEBUG", 0);
    st_add(options, WOUSERNAME, "public", 0);
    unlink("/tmp/test_shmem_state");
    st_add(options, WOSTATEFILE, "test_shmem_state", 0);

    if (init_adaptor(options) != 0) {
        fprintf(stderr, "init_adaptor() failed\n");
        return 1;
    }

    /* Start worker threads */
    pthread_t *threads = (pthread_t *)malloc(sizeof(pthread_t) * num_threads);
    WorkerArgs *args = (WorkerArgs *)malloc(sizeof(WorkerArgs) * num_threads);
    long t;

    for (t = 0; t < num_threads; t++) {
        args[t].thread_id = t;
        args[t].url = "/cgi-bin/WebObjects/TestApp.woa";
        args[t].config_path = config_url;
        pthread_create(&threads[t], NULL, worker_func, &args[t]);
    }

    for (t = 0; t < num_threads; t++) {
        pthread_join(threads[t], NULL);
    }

    /* Test WOAdaptorInfo generation */
    int info_check_ok = 1;
    WOURLComponents wc;
    memset(&wc, 0, sizeof(wc));
    wc.prefix.start = "/cgi-bin/WebObjects";
    wc.prefix.length = strlen(wc.prefix.start);
    wc.applicationName.start = "WOAdaptorInfo";
    wc.applicationName.length = strlen(wc.applicationName.start);
    HTTPResponse *info_resp = WOAdaptorInfo(NULL, &wc);
    if (info_resp && info_resp->content) {
        if (!strstr(info_resp->content, "WebObjects Server Adaptor version = " ADAPTOR_VERSION)) {
            fprintf(stderr, "ERROR: WOAdaptorInfo missing version string!\n");
            info_check_ok = 0;
        }
        if (ADAPTOR_GIT_COMMIT[0] != '\0') {
            if (!strstr(info_resp->content, "<br>Git commit = " ADAPTOR_GIT_COMMIT)) {
                fprintf(stderr, "ERROR: WOAdaptorInfo missing git commit string!\n");
                info_check_ok = 0;
            } else {
                printf("WOAdaptorInfo verified: version %s, git commit %s\n", ADAPTOR_VERSION, ADAPTOR_GIT_COMMIT);
            }
        }
    } else {
        fprintf(stderr, "ERROR: WOAdaptorInfo returned NULL or empty content!\n");
        info_check_ok = 0;
    }
    if (info_resp) {
        resp_free(info_resp);
    }

    printf("\n=== Test Results ===\n");
    printf("Total requests sent:     %d\n", atomic_load(&total_sent));
    printf("Successful requests:     %d\n", atomic_load(&total_success));
    printf("Failed requests:         %d\n", atomic_load(&total_failed));
    printf("Corrupted load average:  %d\n", atomic_load(&corrupt_load_average));
    printf("Headers truncated at ':':%d\n", atomic_load(&headers_without_colon));

    free(shared_body_data);
    free(threads);
    free(args);

    unlink("/tmp/test_shmem_state");
    if (!info_check_ok || atomic_load(&total_failed) > 0 || atomic_load(&headers_without_colon) > 0) {
        printf("\nTEST RESULT: ISSUES DETECTED!\n");
        return 1;
    } else {
        printf("\nTEST RESULT: SUCCESS!\n");
        return 0;
    }
}
