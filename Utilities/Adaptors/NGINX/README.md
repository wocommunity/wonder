# NGINX WebObjects Adaptor Module (`ngx_http_webobjects_module`)

This module natively embeds the shared C core of the Apple / Project Wonder WebObjects Adaptor (`Adaptor/`) into **NGINX**. It provides full reverse-proxying, load-balancing, and session-affinity capabilities for WebObjects application deployments:

- **Dynamic Load Balancing:** Intelligent distribution based on instance metrics and `x-webobjects-loadaverage`.
- **Session Affinity & Pinning:** Tracks active sessions via `woinst` cookies and URL path components.
- **Dynamic Configuration & Instance Discovery:** Polls `wotaskd` (`WOConfig.xml` via HTTP/HTTPS or local filesystem) with automatic updates and multi-host failover.
- **Large Body Streaming:** Efficient buffering and streaming of POST/PUT request bodies (>1 MB).
- **Session Refusal Handling:** Honors `refuseNewSessions` with automatic redirection to alternate active instances.
- **Rich Environment & SSL Headers:** Forwards standard CGI variables and SSL parameters to backend instances (`SERVER_PORT`, `REMOTE_PORT`, `HTTPS`, `SSL_TLS_SNI`, `SSL_PROTOCOL_VERSION`, `SSL_CIPHER`, `SCRIPT_URL`, `SCRIPT_URI`, `SERVER_PROTOCOL`).
- **Administrative Page:** Full support for `WOAdaptorInfo` diagnostics with configurable authentication or public access.

---

## 1. Architecture: NGINX Thread Pool Integration (CRITICAL)

NGINX is built on an asynchronous, event-driven architecture (`epoll`/`kqueue`) with a single event-loop thread per worker process. In contrast, the WebObjects Adaptor C core communicates synchronously with application instances using **blocking TCP sockets** (`connect()`, `send()`, `recv()`).

> [!CAUTION]
> **Without an NGINX Thread Pool, the module runs in synchronous blocking fallback mode!**
> If `webobjects_thread_pool` is omitted in `nginx.conf`, `tr_handleRequest()` will execute directly on the NGINX event-loop worker thread. 
> A single slow or long-running request will **freeze the entire NGINX worker process** in `recv()`. While blocked, that worker cannot accept new connections or process ANY other traffic — **even requests to completely different WebObjects applications or static files will be blocked!**
> 
> In production, you **MUST** configure a thread pool (`thread_pool` in the top-level `main` context and `webobjects_thread_pool` in the WebObjects `location`).

### How Thread Pool Offloading Works:

1. **Non-blocking Request Intake:** NGINX accepts client connections on the event loop and reads the request body asynchronously.
2. **Asynchronous Task Offloading:** The WebObjects transaction is dispatched to a background worker thread via `ngx_thread_task_post()`.
3. **Synchronous Adaptor Core Execution:** The worker thread executes `tr_handleRequest()` in parallel with other worker threads and handles backend socket I/O to the WebObjects instance without blocking the event loop.
4. **Non-blocking Response Delivery:** Upon completion, the thread notifies the main event loop, which delivers the buffered response chain back to the client using non-blocking output filters.

---

## 2. Compilation and Installation

### Method A: As a Dynamic Module with NGINX Sources (Recommended)

Download the source code of the exact NGINX version running on your system, configure the module with `--with-threads`, and compile:

```bash
cd /path/to/nginx-1.26.3

./configure \
    --with-compat \
    --with-threads \
    --with-http_ssl_module \
    --add-dynamic-module=/path/to/wonder/Utilities/Adaptors/NGINX

make modules
```

> [!IMPORTANT]
> The `--with-threads` flag is **required** to enable NGINX thread pool support (`NGX_THREADS`). Without it, thread pool directives are ignored and the module runs in synchronous blocking mode.

#### Optional Compile Flags (e.g., `SUPPORT_REFUSENEWSESSION_ATTR`)
To enable optional features such as `SUPPORT_REFUSENEWSESSION_ATTR` (which restores the legacy `refuseNewSessions` XML instance attribute for static or mixed deployments):

* **Via `--with-cc-opt` (Standard NGINX configure option):**
  ```bash
  ./configure \
      --with-compat \
      --with-threads \
      --with-http_ssl_module \
      --add-dynamic-module=/path/to/wonder/Utilities/Adaptors/NGINX \
      --with-cc-opt="-DSUPPORT_REFUSENEWSESSION_ATTR"
  ```
* **Or via environment variable:**
  ```bash
  SUPPORT_REFUSENEWSESSION_ATTR=1 ./configure \
      --with-compat \
      --with-threads \
      --with-http_ssl_module \
      --add-dynamic-module=/path/to/wonder/Utilities/Adaptors/NGINX
  ```

Copy the compiled module into your NGINX modules directory:
```bash
sudo cp objs/ngx_http_webobjects_module.so /etc/nginx/modules/
```

---

### Method B: Using the Standalone Makefile

If you have NGINX source or development headers available:

```bash
cd Utilities/Adaptors/NGINX
make NGINX_SRC=/path/to/nginx-1.26.3
```

With optional compile flags:
```bash
make NGINX_SRC=/path/to/nginx-1.26.3 SUPPORT_REFUSENEWSESSION_ATTR=1
# or:
make NGINX_SRC=/path/to/nginx-1.26.3 EXTRA_CFLAGS="-DSUPPORT_REFUSENEWSESSION_ATTR"
```

---

## 3. Configuration Directives

| Directive | Context | Description |
|---|---|---|
| `webobjects on \| off` | `location` | Enables or disables WebObjects request processing for this location. |
| `webobjects_thread_pool <name>` | `http`, `server`, `location` | **(Crucial for Multi-Threading)** Name of the NGINX thread pool used to offload blocking backend socket I/O. Without this, requests execute synchronously and block the event loop. |
| `webobjects_config <url> [interval]` | `http`, `server`, `location` | Configuration URL (e.g. `"http://127.0.0.1:1085" 10;`, multiple URLs `"http://host1:81/WOConfig.xml,http://host2:81/WOConfig.xml" 5;`, or trailing comma syntax `"http://host:81/WOConfig.xml,5";`). |
| `webobjects_log <path> [level]` | `http`, `server`, `location` | Path to WebObjects log file and logging level (`DEBUG`, `INFO`, `WARN`, `ERR`). Note: If not set to `ALWAYS_LOG`, logging is activated on demand when `/tmp/logWebObjects` exists. |
| `webobjects_document_root <path>` | `http`, `server`, `location` | Path to the web server document root directory. |
| `webobjects_alias <path>` | `http`, `server`, `location` | WebObjects request alias (default: `/cgi-bin/WebObjects`). |
| `webobjects_user <name> [password]` | `http`, `server`, `location` | Username and optional password for `WOAdaptorInfo` access. Set user to `public` to allow unrestricted public access without password prompt. |
| `webobjects_options <string>` | `http`, `server`, `location` | Additional adaptor options (e.g. `username=public`, custom shared memory state file, etc.). |

---

## 4. Production Example (`nginx.conf`)

Here is a complete, production-ready `nginx.conf` demonstrating proper multi-threaded thread pool setup:

```nginx
# Load dynamic module at the very top of nginx.conf
load_module modules/ngx_http_webobjects_module.so;

worker_processes auto;

# =============================================================================
# STEP 1: Define dedicated thread pool in main context (OUTSIDE the http block)
# =============================================================================
# threads:   Number of worker threads (default 32, increase for high concurrency)
# max_queue: Maximum queue length for pending tasks before returning 500
thread_pool wo_pool threads=32 max_queue=65536;

events {
    worker_connections 4096;
    multi_accept on;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    tcp_nopush      on;
    tcp_nodelay     on;
    keepalive_timeout 65;

    server {
        listen 80;
        listen [::]:80;
        listen 443 ssl;
        listen [::]:443 ssl;
        http2 on;

        server_name apps.example.com;

        # SSL Configuration
        ssl_certificate     /etc/ssl/certs/example.com.crt;
        ssl_certificate_key /etc/ssl/private/example.com.key;
        ssl_protocols       TLSv1.2 TLSv1.3;
        ssl_ciphers         HIGH:!aNULL:!MD5;

        # =========================================================================
        # STEP 2: Forward WebObjects requests and bind them to the thread pool
        # =========================================================================
        location /cgi-bin/WebObjects {
            webobjects on;
            webobjects_config "http://wotaskd1:1085,http://wotaskd2:1085" 10;
            webobjects_log "/var/log/nginx/webobjects.log" info;

            # MANDATORY for true multi-threaded operation:
            webobjects_thread_pool wo_pool;

            # Optional: Allow public access to WOAdaptorInfo without basic auth prompt
            webobjects_user public;

            client_max_body_size 100M;
            client_body_buffer_size 1M;
        }

        # -------------------------------------------------------------------------
        # Serve static WebObjects web resources directly via NGINX (high performance)
        # -------------------------------------------------------------------------
        location /WebObjects {
            alias /opt/Apple/Library/WebObjects/WebServerResources;
            expires 30d;
            add_header Cache-Control "public, no-transform";
            access_log off;
        }

        error_page 500 502 503 504 /50x.html;
        location = /50x.html {
            root html;
        }
    }
}
```

---

## 5. Troubleshooting: Concurrency & Blocked Requests

### Symptom: Long-running requests block other requests (even for different applications)
If a slow request to `AppA` causes requests to `AppB` (or other instances) to hang or time out:

1. **Check NGINX compile options:**
   Run `nginx -V` and ensure `--with-threads` is present in the `configure arguments:`. If missing, NGINX cannot run thread pools.
2. **Check for `thread_pool` and `webobjects_thread_pool` in `nginx.conf`:**
   Ensure both:
   - `thread_pool <name> threads=...;` is present in the **main context** (outside `http { ... }`, near `worker_processes`).
   - `webobjects_thread_pool <name>;` is present inside `location /cgi-bin/WebObjects { ... }`.
   If omitted, the module logs a warning on startup:
   ```text
   [warn] ngx_http_webobjects: no 'webobjects_thread_pool' configured for WebObjects location!
   ```
3. **Thread Pool Sizing:**
   If your application experiences bursts of concurrent long-running requests, the thread pool queue may fill up. Increase the thread count:
   ```nginx
   thread_pool wo_pool threads=64 max_queue=65536;
   ```
4. **Worker Processes:**
   Ensure `worker_processes auto;` is set in `nginx.conf` so NGINX utilizes all CPU cores.
