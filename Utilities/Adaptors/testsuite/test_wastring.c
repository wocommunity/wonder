#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <assert.h>
#include <stdatomic.h>
#include <signal.h>

#include "wastring.h"
#include "Platform.h"

const char *tmp() {
    return "/tmp";
}

#define DEFAULT_THREADS 16
#define DEFAULT_ITERATIONS 5000

static atomic_int errors_detected = 0;
static atomic_int running = 1;
static atomic_long total_requests = 0;

static void crash_handler(int sig) {
    const char *name = (sig == SIGABRT) ? "SIGABRT (Memory corruption / Double-free)" :
                       (sig == SIGSEGV) ? "SIGSEGV (Segmentation fault / Null pointer)" :
                       (sig == SIGBUS)  ? "SIGBUS (Bus error / Memory alignment)" : "UNKNOWN SIGNAL";
    fprintf(stderr, "\n=======================================================\n");
    fprintf(stderr, "❌ TEST FAILED: Caught %s (Signal %d)!\n", name, sig);
    fprintf(stderr, "   Race condition in string cache caused heap corruption or invalid memory access.\n");
    fprintf(stderr, "=======================================================\n");
    _exit(2);
}

/* Worker type 1: Mimics header allocation and freeing in response.c */
void *header_worker(void *arg) {
    long id = (long)arg;
    int iter;

    for (iter = 0; iter < DEFAULT_ITERATIONS && atomic_load(&running); iter++) {
        String *list = NULL;
        int h;

        /* Create a linked list of 5 headers */
        for (h = 0; h < 5; h++) {
            String *s = str_create(NULL, 64);
            if (!s) {
                fprintf(stderr, "[Thread %ld] str_create returned NULL\n", id);
                atomic_fetch_add(&errors_detected, 1);
                return NULL;
            }
            if (h == 0) {
                str_append(s, "x-webobjects-loadaverage: 1.25\r\n");
            } else if (h == 1) {
                str_append(s, "content-type: text/html; charset=UTF-8\r\n");
            } else if (h == 2) {
                str_append(s, "content-length: 1048576\r\n");
            } else if (h == 3) {
                str_append(s, "server: WebObjects/5.4.3\r\n");
            } else {
                str_append(s, "connection: keep-alive\r\n");
            }
            s->next = list;
            list = s;
        }

        /* Check the first header for corruption before freeing */
        String *curr = list;
        while (curr) {
            if (curr->text && strstr(curr->text, "x-webobjects-")) {
                /* If corrupted at index 16, text would be 'x-webobjects-loa' */
                if (strlen(curr->text) == 16 && strcmp(curr->text, "x-webobjects-loa") == 0) {
                    fprintf(stderr, "\n[CRITICAL RACE DETECTED] Header truncated at 16 chars: '%s'!\n", curr->text);
                    atomic_fetch_add(&errors_detected, 1);
                }
                /* Verify character at offset 16 is 'd' (from loadaverage), not '\0' */
                if (curr->length >= 17 && curr->text[16] == '\0') {
                    fprintf(stderr, "\n[CRITICAL RACE DETECTED] Null byte at offset 16 in: '%s'!\n", curr->text);
                    atomic_fetch_add(&errors_detected, 1);
                }
            }
            curr = curr->next;
        }

        /* Free the entire list (just like resp_free does) */
        str_free(list);
        atomic_fetch_add(&total_requests, 1);
    }
    return NULL;
}

/* Worker type 2: Mimics tr_uniqueID in transaction.c */
void *uniqueid_worker(void *arg) {
    long id = (long)arg;
    int iter;
    char dummy_id[17] = "0123456789abcdef"; /* 16 chars */

    for (iter = 0; iter < DEFAULT_ITERATIONS && atomic_load(&running); iter++) {
        String *s = str_create(dummy_id, 25);
        if (!s) {
            fprintf(stderr, "[Thread %ld] str_create returned NULL\n", id);
            atomic_fetch_add(&errors_detected, 1);
            return NULL;
        }
        /* Append 8-char counter like tr_uniqueID does */
        str_appendLength(s, "12345678", 8);

        str_free(s);
        atomic_fetch_add(&total_requests, 1);
    }
    return NULL;
}

int main(int argc, char **argv) {
    int num_threads = DEFAULT_THREADS;
    int iterations = DEFAULT_ITERATIONS;

    if (argc > 1) {
        iterations = atoi(argv[1]);
        if (iterations <= 0) iterations = DEFAULT_ITERATIONS;
    }
    if (argc > 2) {
        num_threads = atoi(argv[2]);
        if (num_threads <= 0) num_threads = DEFAULT_THREADS;
    }

    /* Set up signal handlers to catch heap corruption and memory faults */
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = crash_handler;
    sigaction(SIGABRT, &sa, NULL);
    sigaction(SIGSEGV, &sa, NULL);
    sigaction(SIGBUS, &sa, NULL);

    printf("=== Starting test_wastring Concurrency Test ===\n");
    printf("Threads: %d, Iterations per thread: %d\n", num_threads, iterations);

    if (str_init() != 0) {
        fprintf(stderr, "str_init() failed\n");
        return 1;
    }

    pthread_t *threads = (pthread_t *)malloc(sizeof(pthread_t) * num_threads);
    long i;

    /* Start half header workers and half uniqueid workers */
    for (i = 0; i < num_threads; i++) {
        if (i % 2 == 0) {
            pthread_create(&threads[i], NULL, header_worker, (void *)i);
        } else {
            pthread_create(&threads[i], NULL, uniqueid_worker, (void *)i);
        }
    }

    for (i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }

    free(threads);

    int errs = atomic_load(&errors_detected);
    long total = atomic_load(&total_requests);
    printf("Total string operations: %ld\n", total);
    printf("Errors detected: %d\n", errs);

    if (errs > 0) {
        printf("FAILED: Race condition / corruption reproduced!\n");
        return 1;
    } else {
        printf("PASSED: No string corruptions detected.\n");
        return 0;
    }
}
