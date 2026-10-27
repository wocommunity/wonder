/*
 * ngx_http_webobjects_module.c
 *
 * Nginx HTTP module for Apple / WebObjects Adaptor
 * Provides native reverse-proxying, load-balancing, and session-affinity
 * to WebObjects application instances using the shared Adaptor C core.
 */

#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>

#if (NGX_THREADS)
#include <ngx_thread_pool.h>
#endif

#ifndef MULTITHREADED_ADAPTOR
#define MULTITHREADED_ADAPTOR 1
#endif

#ifndef NGX_HTTP_WEBOBJECTS
#define NGX_HTTP_WEBOBJECTS 1
#endif

#include "config.h"
#include "womalloc.h"
#include "request.h"
#include "response.h"
#include "log.h"
#include "transaction.h"
#include "appcfg.h"
#include "MoreURLCUtilities.h"
#include "listing.h"
#include "errors.h"
#include "httperrors.h"
#include "strtbl.h"
#include "Platform.h"

char *WA_adaptorName = "Nginx/1.0";
static pthread_mutex_t ngx_wo_init_mutex = PTHREAD_MUTEX_INITIALIZER;

/*
 * Compatibility wrapper for ngx_http_parse_chunked:
 * NGINX >= 1.27.2 added a 4th argument (keep_trailers), whereas
 * NGINX <= 1.27.1 (including the 1.26.x stable branch) takes 3 arguments.
 */
#if (defined(nginx_version) && nginx_version >= 1027002)
#define ngx_wo_parse_chunked(r, b, ctx) ngx_http_parse_chunked(r, b, ctx, 0)
#else
#define ngx_wo_parse_chunked(r, b, ctx) ngx_http_parse_chunked(r, b, ctx)
#endif

typedef struct {
    ngx_flag_t          enable;
    ngx_str_t           config_url;
    ngx_int_t           config_interval;
    ngx_str_t           log_path;
    ngx_str_t           log_level;
    ngx_str_t           docroot;
    ngx_str_t           alias;
    ngx_str_t           options;
    ngx_str_t           user;
    ngx_str_t           password;
#if (NGX_THREADS)
    ngx_str_t           thread_pool_name;
    ngx_thread_pool_t  *thread_pool;
#endif
    strtbl             *options_tbl;
    ngx_uint_t          adaptor_initialized;
} ngx_http_webobjects_loc_conf_t;

typedef struct {
    ngx_http_request_t *r;
    HTTPRequest        *req;
    HTTPResponse       *resp;
    WOURLComponents     wc;
    char               *uri;
    char               *protocol;
    char               *docroot;
    ngx_int_t           rc;

    /* Request body buffering/streaming */
    u_char             *body_buffer;
    size_t              body_len;
    size_t              body_read_offset;

    /* Response body streaming */
    ngx_chain_t        *free;          /* reusable output buffers */
    ngx_chain_t        *busy;          /* buffers still owned by nginx filters */
    long                read_count;    /* result of resp_getResponseContent() in thread task */
#if (NGX_THREADS)
    ngx_thread_pool_t  *thread_pool;
    ngx_thread_task_t  *task;          /* reused for all thread pool operations */
#endif

    /* Chunked response parsing */
    ngx_uint_t          chunked:1;
    ngx_uint_t          chunked_done:1;
    ngx_http_chunked_t  chunked_ctx;
} ngx_http_webobjects_ctx_t;

static ngx_int_t ngx_http_webobjects_init(ngx_conf_t *cf);
static void *ngx_http_webobjects_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_webobjects_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child);
static ngx_int_t ngx_http_webobjects_handler(ngx_http_request_t *r);
static void ngx_http_webobjects_post_handler(ngx_http_request_t *r);
static void ngx_http_webobjects_exec(ngx_http_webobjects_ctx_t *ctx);
static void ngx_http_webobjects_send_response(ngx_http_request_t *r, ngx_http_webobjects_ctx_t *ctx);
static void ngx_http_webobjects_pump(ngx_http_request_t *r, ngx_http_webobjects_ctx_t *ctx);
static void ngx_http_webobjects_write_handler(ngx_http_request_t *r);
static void ngx_http_webobjects_finish(ngx_http_request_t *r, ngx_http_webobjects_ctx_t *ctx, ngx_int_t rc);

#if (NGX_THREADS)
static void ngx_http_webobjects_thread_task_handler(void *data, ngx_log_t *log);
static void ngx_http_webobjects_thread_event_handler(ngx_event_t *ev);
static void ngx_http_webobjects_read_task_handler(void *data, ngx_log_t *log);
static void ngx_http_webobjects_read_event_handler(ngx_event_t *ev);
#endif

static char *ngx_http_webobjects_set_enable(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);
static char *ngx_http_webobjects_set_config(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);
static char *ngx_http_webobjects_set_log(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);
static char *ngx_http_webobjects_set_user(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);

static ngx_command_t ngx_http_webobjects_commands[] = {

    { ngx_string("webobjects"),
      NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_http_webobjects_set_enable,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_webobjects_loc_conf_t, enable),
      NULL },

    { ngx_string("webobjects_config"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE12,
      ngx_http_webobjects_set_config,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("webobjects_log"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE12,
      ngx_http_webobjects_set_log,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("webobjects_document_root"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_webobjects_loc_conf_t, docroot),
      NULL },

    { ngx_string("webobjects_alias"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_webobjects_loc_conf_t, alias),
      NULL },

    { ngx_string("webobjects_options"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_webobjects_loc_conf_t, options),
      NULL },

    { ngx_string("webobjects_user"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE12,
      ngx_http_webobjects_set_user,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

#if (NGX_THREADS)
    { ngx_string("webobjects_thread_pool"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_webobjects_loc_conf_t, thread_pool_name),
      NULL },
#endif

    ngx_null_command
};

static ngx_http_module_t ngx_http_webobjects_module_ctx = {
    NULL,                                  /* preconfiguration */
    ngx_http_webobjects_init,              /* postconfiguration */
    NULL,                                  /* create main configuration */
    NULL,                                  /* init main configuration */
    NULL,                                  /* create server configuration */
    NULL,                                  /* merge server configuration */
    ngx_http_webobjects_create_loc_conf,   /* create location configuration */
    ngx_http_webobjects_merge_loc_conf     /* merge location configuration */
};

ngx_module_t ngx_http_webobjects_module = {
    NGX_MODULE_V1,
    &ngx_http_webobjects_module_ctx,       /* module context */
    ngx_http_webobjects_commands,          /* module directives */
    NGX_HTTP_MODULE,                       /* module type */
    NULL,                                  /* init master */
    NULL,                                  /* init module */
    NULL,                                  /* init process */
    NULL,                                  /* init thread */
    NULL,                                  /* exit thread */
    NULL,                                  /* exit process */
    NULL,                                  /* exit master */
    NGX_MODULE_V1_PADDING
};

static char *ngx_http_webobjects_set_enable(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_core_loc_conf_t *clcf;
    char *rv;

    rv = ngx_conf_set_flag_slot(cf, cmd, conf);
    if (rv != NGX_CONF_OK) {
        return rv;
    }

    ngx_http_webobjects_loc_conf_t *lcf = conf;
    if (lcf->enable) {
        clcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_core_module);
        if (clcf) {
            clcf->handler = ngx_http_webobjects_handler;
        }
    }

    return NGX_CONF_OK;
}

static char *ngx_http_webobjects_set_config(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_webobjects_loc_conf_t *lcf = conf;
    ngx_str_t *value = cf->args->elts;

    lcf->config_url = value[1];

    if (cf->args->nelts > 2) {
        lcf->config_interval = ngx_atoi(value[2].data, value[2].len);
        if (lcf->config_interval == NGX_ERROR) {
            return "invalid config interval";
        }
    } else {
        /*
         * Check if interval was provided after a trailing comma in the URL string,
         * e.g. "http://host1:81/WOConfig.xml,http://host2:81/WOConfig.xml,5"
         */
        u_char *last_comma = NULL;
        for (ssize_t i = (ssize_t)lcf->config_url.len - 1; i >= 0; i--) {
            if (lcf->config_url.data[i] == ',') {
                last_comma = &lcf->config_url.data[i];
                break;
            }
        }
        if (last_comma) {
            size_t num_len = (lcf->config_url.data + lcf->config_url.len) - (last_comma + 1);
            if (num_len > 0) {
                ngx_int_t n = ngx_atoi(last_comma + 1, num_len);
                if (n != NGX_ERROR) {
                    lcf->config_interval = n;
                    lcf->config_url.len = last_comma - lcf->config_url.data;
                }
            }
        }
    }
    return NGX_CONF_OK;
}

static char *ngx_http_webobjects_set_log(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_webobjects_loc_conf_t *lcf = conf;
    ngx_str_t *value = cf->args->elts;

    lcf->log_path = value[1];
    if (cf->args->nelts > 2) {
        lcf->log_level = value[2];
    }
    return NGX_CONF_OK;
}

static char *ngx_http_webobjects_set_user(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_webobjects_loc_conf_t *lcf = conf;
    ngx_str_t *value = cf->args->elts;

    lcf->user = value[1];
    if (cf->args->nelts > 2) {
        lcf->password = value[2];
    }
    return NGX_CONF_OK;
}

static void *ngx_http_webobjects_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_webobjects_loc_conf_t *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_webobjects_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    conf->enable = NGX_CONF_UNSET;
    conf->config_interval = NGX_CONF_UNSET;
    conf->options_tbl = st_new(8);
    conf->adaptor_initialized = 0;

    return conf;
}

static char *ngx_http_webobjects_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_webobjects_loc_conf_t *prev = parent;
    ngx_http_webobjects_loc_conf_t *conf = child;

    ngx_conf_merge_value(conf->enable, prev->enable, 0);
    ngx_conf_merge_str_value(conf->config_url, prev->config_url, "http://localhost:1085");
    ngx_conf_merge_value(conf->config_interval, prev->config_interval, 10);
    ngx_conf_merge_str_value(conf->log_path, prev->log_path, "/tmp/WebObjects.log");
    ngx_conf_merge_str_value(conf->log_level, prev->log_level, "INFO");
    ngx_conf_merge_str_value(conf->docroot, prev->docroot, "");
    ngx_conf_merge_str_value(conf->alias, prev->alias, "/cgi-bin/WebObjects");
    ngx_conf_merge_str_value(conf->options, prev->options, "");
    ngx_conf_merge_str_value(conf->user, prev->user, "");
    ngx_conf_merge_str_value(conf->password, prev->password, "");

#if (NGX_THREADS)
    ngx_conf_merge_str_value(conf->thread_pool_name, prev->thread_pool_name, "");
    if (conf->thread_pool_name.len > 0) {
        conf->thread_pool = ngx_thread_pool_add(cf, &conf->thread_pool_name);
        if (conf->thread_pool == NULL) {
            return NGX_CONF_ERROR;
        }
    }
#endif

    if (conf->enable == 1) {
        ngx_http_core_loc_conf_t *clcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_core_module);
        if (clcf && clcf->handler == NULL) {
            clcf->handler = ngx_http_webobjects_handler;
        }
    }

    return NGX_CONF_OK;
}

static ngx_int_t ngx_http_webobjects_init(ngx_conf_t *cf)
{
    ngx_http_handler_pt        *h;
    ngx_http_core_main_conf_t  *cmcf;

    cmcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_core_module);

    h = ngx_array_push(&cmcf->phases[NGX_HTTP_CONTENT_PHASE].handlers);
    if (h == NULL) {
        return NGX_ERROR;
    }

    *h = ngx_http_webobjects_handler;

    return NGX_OK;
}

/* Callback for streaming request bodies > REQUEST_STREAMED_THRESHOLD */
static int ngx_webobjects_stream_body(void *handle, void *buffer, int bufferSize, int mustFill)
{
    HTTPRequest *req = (HTTPRequest *)handle;
    ngx_http_webobjects_ctx_t *ctx = req ? (ngx_http_webobjects_ctx_t *)req->api_handle : NULL;
    if (!ctx) return -1;
    size_t remaining = ctx->body_len - ctx->body_read_offset;
    if (remaining <= 0) return 0;

    size_t to_copy = (remaining > (size_t)bufferSize) ? (size_t)bufferSize : remaining;
    memcpy(buffer, ctx->body_buffer + ctx->body_read_offset, to_copy);
    ctx->body_read_offset += to_copy;

    return (int)to_copy;
}

static ngx_int_t ngx_http_webobjects_handler(ngx_http_request_t *r)
{
    ngx_http_webobjects_loc_conf_t *lcf;
    ngx_int_t rc;

    lcf = ngx_http_get_module_loc_conf(r, ngx_http_webobjects_module);
    if (!lcf->enable) {
        return NGX_DECLINED;
    }

    /* Initialize Adaptor Core on first active location execution if not done */
    if (!lcf->adaptor_initialized) {
        pthread_mutex_lock(&ngx_wo_init_mutex);
        if (!lcf->adaptor_initialized) {
            char url_buf[1024];
            char intvl_buf[32];
            char log_buf[512];
            char lvl_buf[32];

            ngx_snprintf((u_char *)url_buf, sizeof(url_buf) - 1, "%V%Z", &lcf->config_url);
            ngx_snprintf((u_char *)intvl_buf, sizeof(intvl_buf) - 1, "%d%Z", lcf->config_interval);
            ngx_snprintf((u_char *)log_buf, sizeof(log_buf) - 1, "%V%Z", &lcf->log_path);
            ngx_snprintf((u_char *)lvl_buf, sizeof(lvl_buf) - 1, "%V%Z", &lcf->log_level);

            st_add(lcf->options_tbl, WOCONFIG, url_buf, STR_COPYKEY | STR_COPYVALUE);
            st_add(lcf->options_tbl, WOCNFINTVL, intvl_buf, STR_COPYKEY | STR_COPYVALUE);
            st_add(lcf->options_tbl, WOLOGPATH, log_buf, STR_COPYKEY | STR_COPYVALUE);
            st_add(lcf->options_tbl, WOLOGLEVEL, lvl_buf, STR_COPYKEY | STR_COPYVALUE);

            if (lcf->options.len > 0) {
                char opt_buf[2048];
                ngx_snprintf((u_char *)opt_buf, sizeof(opt_buf) - 1, "%V%Z", &lcf->options);
                st_add(lcf->options_tbl, WOOPTIONS, opt_buf, STR_COPYKEY | STR_COPYVALUE);
            }

            if (lcf->user.len > 0) {
                char user_buf[256];
                ngx_snprintf((u_char *)user_buf, sizeof(user_buf) - 1, "%V%Z", &lcf->user);
                st_add(lcf->options_tbl, WOUSERNAME, user_buf, STR_COPYKEY | STR_COPYVALUE);
            }
            if (lcf->password.len > 0) {
                char pass_buf[256];
                ngx_snprintf((u_char *)pass_buf, sizeof(pass_buf) - 1, "%V%Z", &lcf->password);
                st_add(lcf->options_tbl, WOPASSWORD, pass_buf, STR_COPYKEY | STR_COPYVALUE);
            }

            if (init_adaptor(lcf->options_tbl) != 0) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "ngx_http_webobjects_module: init_adaptor() failed");
                pthread_mutex_unlock(&ngx_wo_init_mutex);
                return NGX_HTTP_INTERNAL_SERVER_ERROR;
            }
            lcf->adaptor_initialized = 1;
        }
        pthread_mutex_unlock(&ngx_wo_init_mutex);
    }

    /* Set default request event handlers to prevent NULL pointer dereferences */
    r->read_event_handler = ngx_http_block_reading;
    r->write_event_handler = ngx_http_request_empty_handler;

    /* For methods with a body (POST/PUT), read request body first */
    if (r->method == NGX_HTTP_POST || r->method == NGX_HTTP_PUT) {
        rc = ngx_http_read_client_request_body(r, ngx_http_webobjects_post_handler);
        if (rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }
        return NGX_DONE;
    }

    rc = ngx_http_discard_request_body(r);
    if (rc != NGX_OK) {
        return rc;
    }

    /*
     * For GET, HEAD, etc., increment reference count so that
     * ngx_http_finalize_request inside post_handler / thread_event_handler
     * balances the NGX_DONE returned to Nginx's content phase engine.
     */
    r->main->count++;
    ngx_http_webobjects_post_handler(r);
    return NGX_DONE;
}

static void ngx_http_webobjects_get_var(ngx_http_request_t *r, const char *name_str, char *buf, size_t buf_size)
{
    ngx_str_t var_name;
    var_name.data = (u_char *)name_str;
    var_name.len = ngx_strlen(name_str);
    ngx_uint_t key = ngx_hash_key(var_name.data, var_name.len);
    ngx_http_variable_value_t *vv = ngx_http_get_variable(r, &var_name, key);
    if (vv && !vv->not_found && vv->len > 0) {
        size_t len = (vv->len < buf_size - 1) ? vv->len : buf_size - 1;
        ngx_memcpy(buf, vv->data, len);
        buf[len] = '\0';
    } else {
        buf[0] = '\0';
    }
}

static void ngx_http_webobjects_post_handler(ngx_http_request_t *r)
{
    ngx_http_webobjects_loc_conf_t *lcf;
    ngx_http_webobjects_ctx_t      *ctx;
    ngx_list_part_t                *part;
    ngx_table_elt_t                *header;
    ngx_uint_t                      i;
    WOURLError                      urlerr;

    lcf = ngx_http_get_module_loc_conf(r, ngx_http_webobjects_module);

    ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_webobjects_ctx_t));
    if (ctx == NULL) {
        ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }
    ctx->r = r;
    ngx_http_set_ctx(r, ctx, ngx_http_webobjects_module);

    /* Build null-terminated URI string */
    ctx->uri = ngx_pnalloc(r->pool, r->uri.len + 1);
    if (!ctx->uri) {
        ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }
    ngx_memcpy(ctx->uri, r->uri.data, r->uri.len);
    ctx->uri[r->uri.len] = '\0';

    /* Parse WebObjects URL components */
    ctx->wc = WOURLComponents_Initializer;
    urlerr = WOParseApplicationName(&ctx->wc, ctx->uri);
    if (urlerr != WOURLOK) {
        if (urlerr == WOURLInvalidApplicationName && ac_authorizeAppListing(&ctx->wc)) {
            /* Valid request for WOAdaptorInfo / application listing (e.g. /cgi-bin/WebObjects or /cgi-bin/WebObjects/) */
        } else {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "ngx_http_webobjects: invalid WO URL '%s': %s", ctx->uri, WOURLstrerror(urlerr));
            ngx_http_finalize_request(r, (urlerr == WOURLInvalidApplicationName) ? NGX_HTTP_NOT_FOUND : NGX_HTTP_BAD_REQUEST);
            return;
        }
    }

    /* Add query string if present */
    if (r->args.len > 0) {
        ctx->wc.queryString.start = (char *)r->args.data;
        ctx->wc.queryString.length = r->args.len;
    }

    /* Build HTTPRequest */
    char method_buf[16];
    ngx_snprintf((u_char *)method_buf, sizeof(method_buf) - 1, "%V%Z", &r->method_name);
    ctx->req = req_new(method_buf, NULL);
    if (!ctx->req) {
        ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }
    ctx->req->shouldProcessUrl = 1;
    ctx->req->api_handle = ctx;

    /* Copy client request headers */
    part = &r->headers_in.headers.part;
    header = part->elts;
    for (i = 0; /* void */; i++) {
        if (i >= part->nelts) {
            if (part->next == NULL) break;
            part = part->next;
            header = part->elts;
            i = 0;
        }
        char k[256], v[2048];
        size_t klen = (header[i].key.len < sizeof(k) - 1) ? header[i].key.len : sizeof(k) - 1;
        size_t vlen = (header[i].value.len < sizeof(v) - 1) ? header[i].value.len : sizeof(v) - 1;
        ngx_memcpy(k, header[i].key.data, klen);
        k[klen] = '\0';
        ngx_memcpy(v, header[i].value.data, vlen);
        v[vlen] = '\0';
        req_addHeader(ctx->req, k, v, STR_COPYKEY | STR_COPYVALUE);
    }

    /* Set common CGI / WebObjects environment headers */
    if (r->connection && r->connection->addr_text.len > 0) {
        char remote_addr[64];
        size_t len = (r->connection->addr_text.len < sizeof(remote_addr) - 1) ? r->connection->addr_text.len : sizeof(remote_addr) - 1;
        ngx_memcpy(remote_addr, r->connection->addr_text.data, len);
        remote_addr[len] = '\0';
        req_addHeader(ctx->req, "REMOTE_ADDR", remote_addr, STR_COPYKEY | STR_COPYVALUE);
        req_addHeader(ctx->req, "REMOTE_HOST", remote_addr, STR_COPYKEY | STR_COPYVALUE);
    }

    req_addHeader(ctx->req, "SERVER_SOFTWARE", "Nginx", STR_COPYKEY | STR_COPYVALUE);

    if (r->headers_in.server.len > 0) {
        char server_name[256];
        size_t len = (r->headers_in.server.len < sizeof(server_name) - 1) ? r->headers_in.server.len : sizeof(server_name) - 1;
        ngx_memcpy(server_name, r->headers_in.server.data, len);
        server_name[len] = '\0';
        req_addHeader(ctx->req, "SERVER_NAME", server_name, STR_COPYKEY | STR_COPYVALUE);
    }

    if (lcf->docroot.len > 0) {
        char docroot_buf[512];
        size_t len = (lcf->docroot.len < sizeof(docroot_buf) - 1) ? lcf->docroot.len : sizeof(docroot_buf) - 1;
        ngx_memcpy(docroot_buf, lcf->docroot.data, len);
        docroot_buf[len] = '\0';
        req_addHeader(ctx->req, "DOCUMENT_ROOT", docroot_buf, STR_COPYKEY | STR_COPYVALUE);
    }

    /* SERVER_PORT and REMOTE_PORT */
    if (r->connection) {
        if (r->connection->local_sockaddr) {
            in_port_t port = ngx_inet_get_port(r->connection->local_sockaddr);
            char port_buf[16];
            ngx_snprintf((u_char *)port_buf, sizeof(port_buf) - 1, "%d%Z", (int)port);
            req_addHeader(ctx->req, "SERVER_PORT", port_buf, STR_COPYKEY | STR_COPYVALUE);
        }
        if (r->connection->sockaddr) {
            in_port_t port = ngx_inet_get_port(r->connection->sockaddr);
            char port_buf[16];
            ngx_snprintf((u_char *)port_buf, sizeof(port_buf) - 1, "%d%Z", (int)port);
            req_addHeader(ctx->req, "REMOTE_PORT", port_buf, STR_COPYKEY | STR_COPYVALUE);
        }
    }

    /* HTTPS and SSL attributes */
    char ssl_var[256];

    /* 1. Check NGINX $https variable (evaluates to "on" for SSL connections) */
    ngx_http_webobjects_get_var(r, "https", ssl_var, sizeof(ssl_var));
    if (ssl_var[0] != '\0' && strcasecmp(ssl_var, "on") == 0) {
        req_addHeader(ctx->req, "HTTPS", "on", STR_COPYKEY | STR_COPYVALUE);
    }

    /* 2. Direct connection SSL check if available */
#if (NGX_HTTP_SSL || NGX_SSL)
    if (r->connection && r->connection->ssl) {
        if (!req_HeaderForKey(ctx->req, "HTTPS")) {
            req_addHeader(ctx->req, "HTTPS", "on", STR_COPYKEY | STR_COPYVALUE);
        }
    }
#endif

    /* 3. Check for reverse-proxy HTTPS indicators (X-Forwarded-Proto / X-Forwarded-Port) */
    const char *fwd_proto = req_HeaderForKey(ctx->req, "x-forwarded-proto");
    if (!fwd_proto) fwd_proto = req_HeaderForKey(ctx->req, "X-Forwarded-Proto");
    if (fwd_proto && strcasecmp(fwd_proto, "https") == 0) {
        if (!req_HeaderForKey(ctx->req, "HTTPS")) {
            req_addHeader(ctx->req, "HTTPS", "on", STR_COPYKEY | STR_COPYVALUE);
        }
    }

    /* 4. SSL_TLS_SNI: check NGINX $ssl_server_name or OpenSSL SNI */
    ngx_http_webobjects_get_var(r, "ssl_server_name", ssl_var, sizeof(ssl_var));
    if (ssl_var[0] != '\0') {
        req_addHeader(ctx->req, "SSL_TLS_SNI", ssl_var, STR_COPYKEY | STR_COPYVALUE);
    }
#if (NGX_HTTP_SSL || NGX_SSL)
    else if (r->connection && r->connection->ssl && r->connection->ssl->connection) {
#if defined(TLSEXT_NAMETYPE_host_name)
        const char *sni = SSL_get_servername(r->connection->ssl->connection, TLSEXT_NAMETYPE_host_name);
        if (sni && *sni) {
            req_addHeader(ctx->req, "SSL_TLS_SNI", sni, STR_COPYKEY | STR_COPYVALUE);
        }
#endif
    }
#endif

    /* 5. SSL_PROTOCOL_VERSION: check $ssl_protocol */
    ngx_http_webobjects_get_var(r, "ssl_protocol", ssl_var, sizeof(ssl_var));
    if (ssl_var[0] != '\0') {
        req_addHeader(ctx->req, "SSL_PROTOCOL_VERSION", ssl_var, STR_COPYKEY | STR_COPYVALUE);
    }

    /* 6. SSL_CIPHER: check $ssl_cipher */
    ngx_http_webobjects_get_var(r, "ssl_cipher", ssl_var, sizeof(ssl_var));
    if (ssl_var[0] != '\0') {
        req_addHeader(ctx->req, "SSL_CIPHER", ssl_var, STR_COPYKEY | STR_COPYVALUE);
    }

    /* SERVER_PROTOCOL (e.g. HTTP/1.1) */
    if (r->http_protocol.len > 0) {
        char proto_buf[32];
        size_t plen = (r->http_protocol.len < sizeof(proto_buf) - 1) ? r->http_protocol.len : sizeof(proto_buf) - 1;
        ngx_memcpy(proto_buf, r->http_protocol.data, plen);
        proto_buf[plen] = '\0';
        req_addHeader(ctx->req, "SERVER_PROTOCOL", proto_buf, STR_COPYKEY | STR_COPYVALUE);
    }

    /* SCRIPT_URL: request URI path without query string */
    req_addHeader(ctx->req, "SCRIPT_URL", ctx->uri, STR_COPYKEY | STR_COPYVALUE);

    /* SCRIPT_URI: canonical full URI (scheme://host[:port]/path) */
    {
        int is_ssl = (req_HeaderForKey(ctx->req, "HTTPS") != NULL);
        const char *scheme = is_ssl ? "https" : "http";
        char script_uri[2048];
        script_uri[0] = '\0';

        in_port_t port = 0;
        if (r->connection && r->connection->local_sockaddr) {
            port = ngx_inet_get_port(r->connection->local_sockaddr);
        }

        if (r->headers_in.host && r->headers_in.host->value.len > 0) {
            char host_buf[256];
            size_t hlen = (r->headers_in.host->value.len < sizeof(host_buf) - 1)
                          ? r->headers_in.host->value.len : sizeof(host_buf) - 1;
            ngx_memcpy(host_buf, r->headers_in.host->value.data, hlen);
            host_buf[hlen] = '\0';

            /* If host doesn't include port, check if server port is non-standard */
            if (strchr(host_buf, ':') == NULL) {
                if (port > 0 && ((is_ssl && port != 443) || (!is_ssl && port != 80))) {
                    ngx_snprintf((u_char *)script_uri, sizeof(script_uri) - 1, "%s://%s:%d%s%Z",
                                 scheme, host_buf, (int)port, ctx->uri);
                } else {
                    ngx_snprintf((u_char *)script_uri, sizeof(script_uri) - 1, "%s://%s%s%Z",
                                 scheme, host_buf, ctx->uri);
                }
            } else {
                ngx_snprintf((u_char *)script_uri, sizeof(script_uri) - 1, "%s://%s%s%Z",
                             scheme, host_buf, ctx->uri);
            }
        } else if (r->headers_in.server.len > 0) {
            char srv_buf[256];
            size_t slen = (r->headers_in.server.len < sizeof(srv_buf) - 1)
                          ? r->headers_in.server.len : sizeof(srv_buf) - 1;
            ngx_memcpy(srv_buf, r->headers_in.server.data, slen);
            srv_buf[slen] = '\0';

            if (port > 0 && ((is_ssl && port != 443) || (!is_ssl && port != 80))) {
                ngx_snprintf((u_char *)script_uri, sizeof(script_uri) - 1, "%s://%s:%d%s%Z",
                             scheme, srv_buf, (int)port, ctx->uri);
            } else {
                ngx_snprintf((u_char *)script_uri, sizeof(script_uri) - 1, "%s://%s%s%Z",
                             scheme, srv_buf, ctx->uri);
            }
        }

        if (script_uri[0] != '\0') {
            req_addHeader(ctx->req, "SCRIPT_URI", script_uri, STR_COPYKEY | STR_COPYVALUE);
        }
    }

    /* Process Request Body */
    if ((r->method == NGX_HTTP_POST || r->method == NGX_HTTP_PUT) && r->request_body && r->request_body->bufs) {
        off_t len = 0;
        ngx_chain_t *cl;
        for (cl = r->request_body->bufs; cl; cl = cl->next) {
            len += ngx_buf_size(cl->buf);
        }
        ctx->body_len = len;
        ctx->body_read_offset = 0;
        if (len > 0) {
            ctx->body_buffer = ngx_pnalloc(r->pool, len);
            if (ctx->body_buffer) {
                u_char *p = ctx->body_buffer;
                for (cl = r->request_body->bufs; cl; cl = cl->next) {
                    size_t sz = ngx_buf_size(cl->buf);
                    if (cl->buf->in_file) {
                        /* Read from temporary file if buffered to disk */
                        ssize_t n = ngx_read_file(cl->buf->file, p, sz, cl->buf->file_pos);
                        if (n > 0) p += n;
                    } else {
                        p = ngx_cpymem(p, cl->buf->pos, sz);
                    }
                }
            }
            ctx->req->content_length = ctx->body_len;
            req_allocateContent(ctx->req, ctx->req->content_length, 1);
            if (ctx->req->content && ctx->req->content_buffer_size > 0 && ctx->body_buffer) {
                memcpy(ctx->req->content, ctx->body_buffer, ctx->req->content_buffer_size);
                ctx->body_read_offset = ctx->req->content_buffer_size;
            }
            ctx->req->getMoreContent = (req_getMoreContentCallback)ngx_webobjects_stream_body;

            /* Ensure Content-Length header accurately reflects actual body length */
            char cl_buf[32];
            ngx_snprintf((u_char *)cl_buf, sizeof(cl_buf) - 1, "%O%Z", ctx->body_len);
            req_removeHeader(ctx->req, "content-length");
            req_removeHeader(ctx->req, "content_length");
            req_removeHeader(ctx->req, CONTENT_LENGTH);
            req_addHeader(ctx->req, CONTENT_LENGTH, cl_buf, STR_COPYKEY | STR_COPYVALUE);
        } else {
            /* POST/PUT with 0 body bytes */
            req_removeHeader(ctx->req, "content-length");
            req_removeHeader(ctx->req, "content_length");
            req_removeHeader(ctx->req, CONTENT_LENGTH);
            ctx->req->content_length = 0;
            ctx->req->content_buffer_size = 0;
            ctx->req->content = NULL;
            ctx->req->getMoreContent = NULL;
        }
    } else {
        /* Non-body methods (GET, HEAD, DELETE, OPTIONS, etc.) or requests without body:
         * Ensure no body streaming is attempted and any stray content-length header is removed.
         */
        req_removeHeader(ctx->req, "content-length");
        req_removeHeader(ctx->req, "content_length");
        req_removeHeader(ctx->req, CONTENT_LENGTH);
        ctx->req->content_length = 0;
        ctx->req->content_buffer_size = 0;
        ctx->req->content = NULL;
        ctx->req->getMoreContent = NULL;
    }

    /* Protocol & Docroot */
    ctx->protocol = "HTTP/1.1";
    ctx->docroot = (lcf->docroot.len > 0) ? (char *)lcf->docroot.data : "";

#if (NGX_THREADS)
    /* If thread pool is configured, offload the blocking WebObjects transaction */
    if (lcf->thread_pool) {
        ngx_thread_task_t *task;
        task = ngx_thread_task_alloc(r->pool, 0);
        if (task == NULL) {
            if (ctx->req) req_free(ctx->req);
            ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }
        task->ctx = ctx;
        task->handler = ngx_http_webobjects_thread_task_handler;
        task->event.handler = ngx_http_webobjects_thread_event_handler;
        task->event.data = ctx;
        ctx->task = task;
        ctx->thread_pool = lcf->thread_pool;

        /* Block reading and writing on client socket while thread pool task executes */
        r->read_event_handler = ngx_http_block_reading;
        r->write_event_handler = ngx_http_request_empty_handler;

        if (ngx_thread_task_post(lcf->thread_pool, task) != NGX_OK) {
            if (ctx->req) req_free(ctx->req);
            ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }
        r->main->blocked++;
        return;
    }
#endif

    /* Synchronous fallback when thread pools are not enabled */
    ngx_http_webobjects_exec(ctx);
    ngx_http_webobjects_send_response(r, ctx);
}

#if (NGX_THREADS)
static void ngx_http_webobjects_thread_task_handler(void *data, ngx_log_t *log)
{
    ngx_http_webobjects_ctx_t *ctx = data;
    ngx_http_webobjects_exec(ctx);
}

static void ngx_http_webobjects_thread_event_handler(ngx_event_t *ev)
{
    ngx_http_webobjects_ctx_t *ctx = ev->data;
    ngx_http_request_t *r = ctx->r;

    r->main->blocked--;
    ctx->task = NULL;

    if (r->connection->error) {
        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
        return;
    }

    ngx_http_webobjects_send_response(r, ctx);
    ngx_http_run_posted_requests(r->connection);
}

/* Thread pool: read the next response block from the instance (blocking) */
static void ngx_http_webobjects_read_task_handler(void *data, ngx_log_t *log)
{
    ngx_http_webobjects_ctx_t *ctx = data;
    ctx->read_count = resp_getResponseContent(ctx->resp, 1);
}

/* Event loop: block was read, continue streaming */
static void ngx_http_webobjects_read_event_handler(ngx_event_t *ev)
{
    ngx_http_webobjects_ctx_t *ctx = ev->data;
    ngx_http_request_t *r = ctx->r;

    r->main->blocked--;
    ctx->task = NULL;

    if (ctx->read_count > 0) {
        /* incomplete block (unset/wrong content-length): RESP_LENGTH_INVALID is set */
        ctx->resp->content_read += ctx->read_count;
        ctx->resp->content_valid = ctx->read_count;
    }

    if (r->connection->error) {
        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
        return;
    }

    ngx_http_webobjects_pump(r, ctx);
    ngx_http_run_posted_requests(r->connection);
}
#endif

static void ngx_http_webobjects_exec(ngx_http_webobjects_ctx_t *ctx)
{
    if (ctx->wc.applicationName.length == 0 && ac_authorizeAppListing(&ctx->wc)) {
        ctx->resp = WOAdaptorInfo(ctx->req, &ctx->wc);
    } else {
        ctx->resp = tr_handleRequest(ctx->req, ctx->uri, &ctx->wc, ctx->protocol, ctx->docroot);
    }
    if (!ctx->resp) {
        ctx->rc = NGX_HTTP_BAD_GATEWAY;
    } else {
        ctx->rc = NGX_OK;
    }
}

/* Callback for copying WebObjects response headers into Nginx */
static void ngx_copy_response_header(const char *key, const char *value, void **context)
{
    ngx_http_request_t *r = (ngx_http_request_t *)*context;
    ngx_table_elt_t *h;

    /* Content-Type is special in Nginx */
    if (strcasecmp(key, "content-type") == 0) {
        r->headers_out.content_type.len = strlen(value);
        r->headers_out.content_type.data = ngx_pnalloc(r->pool, r->headers_out.content_type.len);
        if (r->headers_out.content_type.data) {
            ngx_memcpy(r->headers_out.content_type.data, value, r->headers_out.content_type.len);
        }
        r->headers_out.content_type_len = r->headers_out.content_type.len;
        return;
    }

    if (strcasecmp(key, "content-length") == 0) {
        r->headers_out.content_length_n = ngx_atoof((u_char *)value, strlen(value));
        return;
    }

    /* Hop-by-hop headers managed directly by Nginx */
    if (strcasecmp(key, "connection") == 0 ||
        strcasecmp(key, "keep-alive") == 0 ||
        strcasecmp(key, "transfer-encoding") == 0) {
        return;
    }

    h = ngx_list_push(&r->headers_out.headers);
    if (h == NULL) return;

    h->hash = 1;
    h->key.len = strlen(key);
    h->key.data = ngx_pnalloc(r->pool, h->key.len);
    if (h->key.data) {
        ngx_memcpy(h->key.data, key, h->key.len);
    }

    h->value.len = strlen(value);
    h->value.data = ngx_pnalloc(r->pool, h->value.len);
    if (h->value.data) {
        ngx_memcpy(h->value.data, value, h->value.len);
    }

    if (strcasecmp(key, "content-encoding") == 0) {
        r->headers_out.content_encoding = h;
    } else if (strcasecmp(key, "location") == 0) {
        r->headers_out.location = h;
    } else if (strcasecmp(key, "server") == 0) {
        r->headers_out.server = h;
    } else if (strcasecmp(key, "date") == 0) {
        r->headers_out.date = h;
    } else if (strcasecmp(key, "expires") == 0) {
        r->headers_out.expires = h;
    } else if (strcasecmp(key, "last-modified") == 0) {
        r->headers_out.last_modified = h;
        r->headers_out.last_modified_time = ngx_http_parse_time(h->value.data, h->value.len);
    } else if (strcasecmp(key, "etag") == 0) {
        r->headers_out.etag = h;
    }
}

/*
 * Release the WebObjects response/request and finalize the nginx request.
 * Must be called exactly once per request after the response was started.
 */
static void ngx_http_webobjects_finish(ngx_http_request_t *r, ngx_http_webobjects_ctx_t *ctx, ngx_int_t rc)
{
    HTTPResponse *resp = ctx->resp;

    if (resp) {
        if (rc == NGX_ERROR
            || (!ctx->chunked && (resp->flags & RESP_LENGTH_INVALID))
            || (!ctx->chunked && resp->content_read < resp->content_length)
            || (ctx->chunked && !ctx->chunked_done))
        {
            /*
             * Incomplete/aborted stream (client gone, instance died, or no
             * content-length): never reuse this instance connection, it may
             * still contain unread data.
             */
            resp->keepConnection = 0;
            resp->flags |= RESP_CLOSE_CONNECTION;
        }
        resp_free(resp);
        ctx->resp = NULL;
    }
    if (ctx->req) {
        req_free(ctx->req);
        ctx->req = NULL;
    }

    /* Reset event handlers before finalization so no stale/null handlers can be dispatched */
    r->read_event_handler = ngx_http_block_reading;
    r->write_event_handler = ngx_http_request_empty_handler;

    if (r->connection && r->connection->write && r->connection->write->timer_set) {
        ngx_del_timer(r->connection->write);
    }

    ctx->rc = rc;
    ngx_http_finalize_request(r, rc);
}

/* Client socket writable again: flush pending output, then continue streaming */
static void ngx_http_webobjects_write_handler(ngx_http_request_t *r)
{
    ngx_http_webobjects_ctx_t *ctx = ngx_http_get_module_ctx(r, ngx_http_webobjects_module);
    ngx_http_core_loc_conf_t  *clcf;
    ngx_connection_t          *c = r->connection;
    ngx_int_t                  rc;

    if (ctx == NULL) {
        return;
    }

    if (c->write->timedout) {
        c->timedout = 1;
        ngx_log_error(NGX_LOG_INFO, c->log, NGX_ETIMEDOUT, "ngx_http_webobjects: client timed out");
        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
        return;
    }

    rc = ngx_http_output_filter(r, NULL);
    ngx_chain_update_chains(r->pool, &ctx->free, &ctx->busy, NULL,
                            (ngx_buf_tag_t) &ngx_http_webobjects_module);

    if (rc == NGX_ERROR || c->error) {
        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
        return;
    }

    clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

    if (rc == NGX_AGAIN) {
        if (!c->write->delayed) {
            ngx_add_timer(c->write, clcf->send_timeout);
        }
        if (ngx_handle_write_event(c->write, clcf->send_lowat) != NGX_OK) {
            ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
        }
        return;
    }

    if (c->write->timer_set) {
        ngx_del_timer(c->write);
    }

    r->write_event_handler = ngx_http_request_empty_handler;
    ngx_http_webobjects_pump(r, ctx);
}

/*
 * Streaming loop: hand the current block to nginx, then read the next block
 * from the instance.  The next block is only read once nginx accepted the
 * previous one (backpressure), so memory usage stays bounded regardless of
 * the response size.  With a thread pool, the blocking read runs in the pool.
 */
static void ngx_http_webobjects_pump(ngx_http_request_t *r, ngx_http_webobjects_ctx_t *ctx)
{
    ngx_http_core_loc_conf_t *clcf;
    ngx_connection_t         *c = r->connection;
    ngx_int_t                 rc;

    for ( ;; ) {
        HTTPResponse *resp = ctx->resp;
        int more;

        if (ctx->chunked) {
            more = !ctx->chunked_done;
        } else {
            more = (resp->flags & RESP_LENGTH_INVALID) != RESP_LENGTH_INVALID
                   && resp->content_read < resp->content_length;
        }

        if (ctx->chunked && resp->content_valid == 0 && !ctx->chunked_done) {
            /* Upstream connection closed before the terminating 0\r\n\r\n chunk was received */
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "ngx_http_webobjects: upstream closed connection before final chunk");
            ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
            return;
        }

        if (resp->content_valid > 0 || !more) {
            ngx_chain_t *out = NULL;
            ngx_buf_t   *b = NULL;

            if (ctx->chunked) {
                if (resp->content_valid > 0) {
                    ngx_buf_t in_buf;
                    ngx_memzero(&in_buf, sizeof(ngx_buf_t));
                    in_buf.pos = (u_char *)resp->content;
                    in_buf.last = (u_char *)resp->content + resp->content_valid;
                    in_buf.start = in_buf.pos;
                    in_buf.end = in_buf.last;
                    in_buf.memory = 1;

                    size_t max_payload = (size_t)resp->content_valid;
                    out = ngx_chain_get_free_buf(r->pool, &ctx->free);
                    if (out == NULL) {
                        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                        return;
                    }
                    b = out->buf;
                    if (b->start == NULL || (size_t)(b->end - b->start) < max_payload) {
                        size_t size = max_payload > RESPONSE_STREAMED_SIZE ? max_payload : RESPONSE_STREAMED_SIZE;
                        b->start = ngx_palloc(r->pool, size);
                        if (b->start == NULL) {
                            ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                            return;
                        }
                        b->end = b->start + size;
                    }
                    b->pos = b->start;
                    b->last = b->start;
                    b->temporary = 1;
                    b->tag = (ngx_buf_tag_t) &ngx_http_webobjects_module;
                    b->flush = 1;
                    b->last_buf = 0;
                    b->last_in_chain = 0;
                    b->sync = 0;

                    while (in_buf.pos < in_buf.last) {
                        ngx_int_t prc = ngx_wo_parse_chunked(r, &in_buf, &ctx->chunked_ctx);

                        if (prc == NGX_OK) {
                            /* Chunk header parsed: chunk payload starts at in_buf.pos */
                            size_t avail = in_buf.last - in_buf.pos;
                            size_t len = (avail >= (size_t)ctx->chunked_ctx.size)
                                         ? (size_t)ctx->chunked_ctx.size : avail;

                            if (len > 0) {
                                b->last = ngx_cpymem(b->last, in_buf.pos, len);
                                in_buf.pos += len;
                                ctx->chunked_ctx.size -= len;
                            }
                            continue;
                        }

                        if (prc == NGX_DONE) {
                            /* Final 0\r\n\r\n chunk parsed successfully */
                            ctx->chunked_done = 1;
                            more = 0;
                            break;
                        }

                        if (prc == NGX_AGAIN) {
                            /* All bytes in current buffer parsed, more bytes needed for next chunk */
                            break;
                        }

                        /* prc == NGX_ERROR: malformed chunk framing from backend */
                        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                                      "ngx_http_webobjects: upstream sent invalid chunked response");
                        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                        return;
                    }

                    resp->content_valid = 0;

                    if (b->pos == b->last) {
                        if (!more) {
                            b->sync = 1;
                        } else {
                            /* Consumed chunk headers/delimiters without producing payload; recycle buffer */
                            out->next = ctx->free;
                            ctx->free = out;
                            out = NULL;
                        }
                    }
                } else if (!more) {
                    /* end of chunked stream without remaining data */
                    b = ngx_calloc_buf(r->pool);
                    out = ngx_alloc_chain_link(r->pool);
                    if (b == NULL || out == NULL) {
                        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                        return;
                    }
                    b->sync = 1;
                    out->buf = b;
                    out->next = NULL;
                }
            } else {
                /* Non-chunked response (Content-Length or connection-close stream) */
                if (resp->content_valid > 0) {
                    size_t len = (size_t)resp->content_valid;

                    out = ngx_chain_get_free_buf(r->pool, &ctx->free);
                    if (out == NULL) {
                        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                        return;
                    }
                    b = out->buf;
                    if (b->start == NULL || (size_t)(b->end - b->start) < len) {
                        size_t size = len > RESPONSE_STREAMED_SIZE ? len : RESPONSE_STREAMED_SIZE;
                        b->start = ngx_palloc(r->pool, size);
                        if (b->start == NULL) {
                            ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                            return;
                        }
                        b->end = b->start + size;
                    }
                    b->pos = b->start;
                    b->last = ngx_cpymem(b->start, resp->content, len);
                    b->temporary = 1;
                    b->tag = (ngx_buf_tag_t) &ngx_http_webobjects_module;
                    b->flush = 1;
                    b->last_buf = 0;
                    b->last_in_chain = 0;
                    b->sync = 0;
                    resp->content_valid = 0;
                } else {
                    /* end of stream without remaining data */
                    b = ngx_calloc_buf(r->pool);
                    out = ngx_alloc_chain_link(r->pool);
                    if (b == NULL || out == NULL) {
                        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                        return;
                    }
                    b->sync = 1;
                    out->buf = b;
                    out->next = NULL;
                }
            }

            if (out) {
                if (!more) {
                    b->last_buf = (r == r->main) ? 1 : 0;
                    b->last_in_chain = 1;
                }

                rc = ngx_http_output_filter(r, out);
                ngx_chain_update_chains(r->pool, &ctx->free, &ctx->busy, &out,
                                        (ngx_buf_tag_t) &ngx_http_webobjects_module);

                if (rc == NGX_ERROR || c->error) {
                    ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                    return;
                }

                if (!more) {
                    /* nginx flushes remaining buffered data itself after finalize */
                    ngx_http_webobjects_finish(r, ctx, rc);
                    return;
                }

                if (rc == NGX_AGAIN) {
                    /* client is slow: wait until the socket is writable again */
                    clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);
                    r->write_event_handler = ngx_http_webobjects_write_handler;
                    if (!c->write->delayed) {
                        ngx_add_timer(c->write, clcf->send_timeout);
                    }
                    if (ngx_handle_write_event(c->write, clcf->send_lowat) != NGX_OK) {
                        ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                    }
                    return;
                }
            }
        }

        /* If stream ended (e.g. final chunk parsed), finish request */
        if (!more) {
            ngx_http_webobjects_finish(r, ctx, NGX_OK);
            return;
        }

        /* read the next block from the instance */
#if (NGX_THREADS)
        if (ctx->thread_pool) {
            ngx_thread_task_t *task = ngx_thread_task_alloc(r->pool, 0);
            if (task == NULL) {
                ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                return;
            }
            task->ctx = ctx;
            task->handler = ngx_http_webobjects_read_task_handler;
            task->event.handler = ngx_http_webobjects_read_event_handler;
            task->event.data = ctx;
            ctx->task = task;
            ctx->read_count = 0;

            r->read_event_handler = ngx_http_block_reading;
            r->write_event_handler = ngx_http_request_empty_handler;

            if (ngx_thread_task_post(ctx->thread_pool, task) != NGX_OK) {
                ngx_http_webobjects_finish(r, ctx, NGX_ERROR);
                return;
            }
            r->main->blocked++;
            return;
        }
#endif
        {
            long count = resp_getResponseContent(resp, 1);
            if (count > 0) {
                resp->content_read += count;
                resp->content_valid = count;
            }
        }
    }
}

/* Send response headers and start streaming the body */
static void ngx_http_webobjects_send_response(ngx_http_request_t *r, ngx_http_webobjects_ctx_t *ctx)
{
    HTTPResponse *resp = ctx->resp;
    ngx_int_t rc;

    if (!resp) {
        ngx_http_webobjects_finish(r, ctx, NGX_HTTP_BAD_GATEWAY);
        return;
    }

    /* Check if WebObjects backend sent a chunked response */
    const char *te = st_valueFor(resp->headers, "transfer-encoding");
    if (te && (strcasecmp(te, "chunked") == 0 || strstr(te, "chunked") != NULL)) {
        ctx->chunked = 1;
        ngx_memzero(&ctx->chunked_ctx, sizeof(ngx_http_chunked_t));
    }

    r->headers_out.status = resp->status ? resp->status : NGX_HTTP_OK;

    /* Copy headers */
    void *rctx = r;
    st_perform(resp->headers, (st_perform_callback)ngx_copy_response_header, &rctx);

    if (ctx->chunked) {
        /* Chunked responses must not have a Content-Length header in Nginx */
        r->headers_out.content_length_n = -1;
    } else if (r->headers_out.content_length_n < 0
               && !(resp->flags & RESP_LENGTH_INVALID)
               && resp->content_length > 0) {
        r->headers_out.content_length_n = resp->content_length;
    }

    rc = ngx_http_send_header(r);
    if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
        /* no body to send (HEAD, error, ...): same cleanup as before */
        ngx_http_webobjects_finish(r, ctx, rc);
        return;
    }

    r->read_event_handler = ngx_http_block_reading;
    r->write_event_handler = ngx_http_request_empty_handler;

    /* Stream the body block by block (first block was already read by tr_handleRequest) */
    ngx_http_webobjects_pump(r, ctx);
}
