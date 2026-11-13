#!/usr/bin/env python3
"""
Dummy WebObjects Application Server for Adaptor testing.
Listens on 127.0.0.1:8999, handles concurrent connections,
consumes POST payloads (e.g. 1MB), and returns valid WebObjects HTTP responses.
"""

import sys
import socket
import threading
import argparse

def handle_client(conn, addr, verbose=False):
    if verbose:
        print(f"[DummyApp] Connected from {addr}")
    rfile = conn.makefile('rb', buffering=65536)
    try:
        while True:
            # Read request line
            line = rfile.readline()
            if not line:
                break
            line_str = line.decode('iso-8859-1').strip()
            if not line_str:
                continue
            
            parts = line_str.split()
            if len(parts) < 2:
                break
            method, path = parts[0], parts[1]

            # Read headers
            content_length = 0
            keep_alive = True
            request_id = None
            while True:
                hdr_line = rfile.readline()
                if not hdr_line or hdr_line in (b'\r\n', b'\n'):
                    break
                hdr_str = hdr_line.decode('iso-8859-1').strip()
                colon = hdr_str.find(':')
                if colon != -1:
                    key = hdr_str[:colon].strip().lower()
                    val = hdr_str[colon+1:].strip()
                    if key == 'content-length':
                        try:
                            content_length = int(val)
                        except ValueError:
                            content_length = 0
                    elif key == 'connection':
                        if val.lower() == 'close':
                            keep_alive = False
                    elif key == 'x-webobjects-request-id':
                        request_id = val

            # Read body if present
            bytes_read = 0
            while bytes_read < content_length:
                chunk_to_read = min(65536, content_length - bytes_read)
                chunk = rfile.read(chunk_to_read)
                if not chunk:
                    break
                bytes_read += len(chunk)

            if verbose:
                print(f"[DummyApp] {method} {path} body={bytes_read}/{content_length} bytes, req_id={request_id}")

            # Send WebObjects HTTP response
            body = b"<html><body>OK from Dummy WebObjects</body></html>\r\n"
            resp_headers = [
                b"HTTP/1.1 200 OK\r\n",
                b"Content-Type: text/html\r\n",
                f"Content-Length: {len(body)}\r\n".encode('ascii'),
                b"x-webobjects-loadaverage: 0.25\r\n",
                f"Connection: {'keep-alive' if keep_alive else 'close'}\r\n".encode('ascii'),
            ]
            if request_id:
                resp_headers.append(f"x-webobjects-request-id: {request_id}\r\n".encode('ascii'))
            resp_headers.append(b"\r\n")

            conn.sendall(b"".join(resp_headers) + body)

            if not keep_alive:
                break
    except Exception as e:
        if verbose:
            print(f"[DummyApp] Client error: {e}")
    finally:
        try:
            rfile.close()
            conn.close()
        except Exception:
            pass
        if verbose:
            print(f"[DummyApp] Connection closed for {addr}")

def run_server(port=8999, verbose=False):
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(('127.0.0.1', port))
    server.listen(128)
    print(f"[DummyApp] Listening on 127.0.0.1:{port}")
    sys.stdout.flush()

    try:
        while True:
            conn, addr = server.accept()
            t = threading.Thread(target=handle_client, args=(conn, addr, verbose), daemon=True)
            t.start()
    except KeyboardInterrupt:
        print("[DummyApp] Shutting down...")
    finally:
        server.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Dummy WebObjects App Server")
    parser.add_argument("--port", type=int, default=8999, help="Port to listen on")
    parser.add_argument("--verbose", action="store_true", help="Verbose logging")
    args = parser.parse_args()
    run_server(args.port, args.verbose)
