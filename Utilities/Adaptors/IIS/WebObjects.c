/*

Copyright © 2000 Apple Computer, Inc. All Rights Reserved.

The contents of this file constitute Original Code as defined in and are
subject to the Apple Public Source License Version 1.1 (the 'License').
You may not use this file except in compliance with the License. 
Please obtain a copy of the License at http://www.apple.com/publicsource 
and read it before usingthis file.

This Original Code and all software distributed under the License are
distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT. 
Please see the License for the specific language governing rights 
and limitations under the License.


*/
/*
 *	WebObjects server adaptor Internet Information Server API module.
 *
 */

/** client certificate support
 If you want to access the encoded bytes of the certificate, you'll need to get a newer version of httpext.h, and have wincrypt.h as well.
 You should then be able to uncomment lines surrounded by client certificate support comments, recompile with _WIN32_WINNT = 0x400 defined,
 and run.
 Warning before you get started. The certificate will be encoded in some format that will require you to do significant work on your end.
 All the decoded bits will already be in the headers.
 end client certificate support **/

#if defined(MINGW)
#define MS_BOOL WINBOOL
#endif

#include "config.h"
#include "womalloc.h"
#include "request.h"
#include "response.h"
#include "log.h"
#include "transaction.h"
#include "appcfg.h"
#include "strtbl.h"
#include "MoreURLCUtilities.h"
#include "errors.h"
#include "httperrors.h"
#include "listing.h"
#include "wastring.h"

#include   "prvr_ext.h"
#include   "httpext.h"


#include <windows.h>
#include <winbase.h>
#include <string.h>
#include <stdio.h>
#include <malloc.h>

#include "iis_http_headers.h"


#define CGI_SCRIPT_NAME	"SCRIPT_NAME"
#define	CGI_PATH_INFO	"PATH_INFO"
#define	CGI_SERVER_PROTOCOL	"SERVER_PROTOCOL"
#define	CGI_DOCUMENT_ROOT	"DOCUMENT_ROOT"
/** client certificate support
#define MAX_CERT_SIZE 8192
client certificate support **/

#define	CRLF	"\r\n"

// 2009/04/29: how many zero bytes data packages at a stretch should be ignored 
//             before we assume a ReadClient problem (0 would be faster, but could
//             be dangerous - please check if a zero bytes result is possible in a 
//             "normal" operation mode).
#define MAGIC_LEN_ZERO_LIMIT 100

// 2009/06/08: wait a little bit to give IIS time to perform all required
//             clean-up operations.  Please feel free to set this value to 
//             '0' if you think (or even know) that a sleep operation isn't 
//             necessary after an HSE_REQ_CLOSE_CONNECTION event.
#define HSE_REQ_CLOSE_CONNECTION_CLEANUP_SLEEP 250

char *WA_adaptorName = "IIS";


static strtbl *read_registry_config();
static int adaptorEnabled;

/*
 * Called the first time we get loaded into the server
 */
MS_BOOL WINAPI DllMain( HANDLE hinst, ULONG reason, LPVOID ptr)
{
   strtbl *config_options;

   switch( reason ) {
      case DLL_PROCESS_ATTACH:
         config_options = read_registry_config();
         adaptorEnabled = init_adaptor(config_options)==0;
         break;
      case DLL_THREAD_ATTACH:
      case DLL_THREAD_DETACH:
      case DLL_PROCESS_DETACH:
         break;
   }
   return TRUE;
}

__declspec(dllexport) MS_BOOL __stdcall GetExtensionVersion (HSE_VERSION_INFO *pVer)
{
   pVer->dwExtensionVersion = CURRENT_WOF_VERSION_MAJOR;
   pVer->dwExtensionVersion = (pVer->dwExtensionVersion << 16 ) | CURRENT_WOF_VERSION_MINOR;

   strcpy(pVer->lpszExtensionDesc, "WebObjects Extension DLL for IIS Servers by Apple Computer, Inc.  version " ADAPTOR_VERSION);
   return TRUE;
}

/**
 * 2009/06/09:
 * Closes client socket connection.
 */
static MS_BOOL closeClientConnection(EXTENSION_CONTROL_BLOCK *p, HTTPResponse *resp)
{
   MS_BOOL result = FALSE;

   // closing is easy, because the adaptor operates in SYNC mode and
   // we have not to handle asynchronous read/write operations!
   WOLog(WO_INFO, "Closing client socket connection...");
   if(p->ServerSupportFunction(p->ConnID, HSE_REQ_CLOSE_CONNECTION,
                               NULL, NULL, NULL))
   {
      // Note found on MSDN:
      //   'HSE_REQ_CLOSE_CONNECTION closes the client socket connection
      //   immediately, but IIS takes a small amount of time to handle the
      //   threads in the thread pool before the connection can be completely
      //   removed.'
      // Therefore: wait a little bit to give IIS time to perform all required
      // clean-up operations.
      //
      // Note for a future version: Is sleep necessary/wise or is it
      // counterproductive?  And if closing is wise: which is the optimal
      // value for the sleep call?
      DWORD cleanupSleepPeriod = HSE_REQ_CLOSE_CONNECTION_CLEANUP_SLEEP;
      if(cleanupSleepPeriod > 0)
      {
         WOLog(WO_INFO, "... and wait %d ms for client socket termination.", cleanupSleepPeriod);
         Sleep(cleanupSleepPeriod);
      }
      result = TRUE;
   }
   else
   {
      WOLog(WO_ERR, "Cannot close client socket connection - error code %d.",
            GetLastError());
   }

   return result;
}

static void sendResponse(EXTENSION_CONTROL_BLOCK *p, HTTPResponse *resp)
{
   String *resphdrs;
   char status[128];
   int browserStatus = 0;
   DWORD len;

   p->dwHttpStatusCode = resp->status;
   sprintf(status, "%d %s",resp->status, resp->statusMsg);

   /*
    *	send the headers (collected into one buffer)
    */
   resphdrs = resp_packageHeaders(resp);
   if (resphdrs)
   {
      len = resphdrs->length;
      /* ccording to the Microsoft web site, HSE_REQ_SEND_RESPONSE_HEADER is depreciated */
      /* HSE_REQ_SEND_RESPONSE_HEADER_EX is preferred. */
      /* However, moving to HSE_REQ_SEND_RESPONSE_HEADER_EX will not work if we must support IIS 3.0. */
      if (p->ServerSupportFunction(p->ConnID, HSE_REQ_SEND_RESPONSE_HEADER,
                                   status, &len, (LPDWORD)resphdrs->text) != TRUE)
      {
         browserStatus = -1;
         WOLog(WO_ERR,"Failed to send response headers (%d)", GetLastError());
      } else {
         len = 2;
         // 2009/06/10: The 4th. parameter is only allowed to be '0' if
         //             this code would belong to a filter plugin.  But
         //             this code realizes an IIS extension and therefore
         //             only HSE_IO_SYNC and ..._ASYNC is allowed!
         if (p->WriteClient(p->ConnID, CRLF, &len, HSE_IO_SYNC) != TRUE)
         {
            browserStatus = -1;
            WOLog(WO_ERR,"Failed to send \\r\\n (%d)", GetLastError());
         }

         /* resp->content_valid will be 0 for HEAD requests and empty responses */
         if (resp->content_valid) {
            int count;
            while (resp->content_read < resp->content_length &&
                   (resp->flags & RESP_LENGTH_INVALID) != RESP_LENGTH_INVALID &&
                   browserStatus == 0) {
                len = resp->content_valid;
                // 2009/06/10: The 4th. parameter is only allowed to be '0' if
                //             this code would belong to a filter plugin.  But
                //             this code realizes an IIS extension and therefore
                //             only HSE_IO_SYNC and ..._ASYNC is allowed!
                if (len && (p->WriteClient(p->ConnID, resp->content, &len, HSE_IO_SYNC) != TRUE))
                {
                   browserStatus = -1;
                   WOLog(WO_ERR,"Failed to send content (%d)", GetLastError());
                }

                if(browserStatus == 0)
                {
                   // read next response chunk from the WebObjects application
                   count = resp_getResponseContent(resp, 1);
                   if(count > 0)
                   {
                       // 2009/06/09: handle situations where content_length is wrong or
                       //             unset.  Read as much data as possible from the
                       //             WebObjects application and send the data to the
                       //             client-side.
                      resp->content_read += count;
                      resp->content_valid = count;
                   }
                   if(count != 0)
                   {
                      // 2009/04/30: error while reading response content (this can happen
                      //             if the instance dies during sending the response - e.g.
                      //             during a file download - or if the content_length is
                      //             wrong/unset).  Stop the loop to avoid endless looping!
                      WOLog(WO_WARN, "sendResponse(): received an incomplete data package.  Please look for a dead instance or adjust content-length value.");
                   }
                }
            }
            if(browserStatus == 0)
            {
               len = resp->content_valid;
               // 2009/06/10: The 4th. parameter is only allowed to be '0' if
               //             this code would belong to a filter plugin.  But
               //             this code realizes an IIS extension and therefore
               //             only HSE_IO_SYNC and ..._ASYNC is allowed!
               if (len && (p->WriteClient(p->ConnID, resp->content, &len, HSE_IO_SYNC) != TRUE))
               {
                  browserStatus = -1;
                  WOLog(WO_ERR,"Failed to send content (%d)", GetLastError());
               }
            }
         }
      }
      str_free(resphdrs);
   } /* else? return warning */

   if(resp->content_read < resp->content_length)
   {
      // 2009/06/08: in case of an unset/wrong content length value, we
      //             must close the client socket connection to signalize
      //             the client application the end-of-stream.
      closeClientConnection(p, resp);

      if(resp->keepConnection != 0)
      {
         // 2009/04/30: it is possible, that the user (=browser) cancels the last started
         //             request.  The existing mechanism of the nbsocket.c implementation
         //             (starting a reset operation that cleans/consumes the remaining
         //             content of the socket buffer) fails sometimes in such situations.
         //             E.g. during an huge file download, the reset operation doesn't consume
         //             the complete download stream (such a behaviour would be very expensive!),
         //             but only the local socket buffer.  If such a connection is reused
         //             in another request-response-cycle, the adaptor/browser gets unexpected
         //             data garbage.  This can lead to situations, where the adaptor marks an
         //             existing and fully functional instance as death.  Therefore: dump
         //             such connections!
         WOLog(WO_INFO, "Forget the existing connection.");
         resp->keepConnection = 0;
         resp->flags |= RESP_CLOSE_CONNECTION;
         // after calling the resp_free function, the connection doesn't longer exist!
      }
   }

   return;
}

static DWORD die_resp(EXTENSION_CONTROL_BLOCK *p, HTTPResponse *resp)
{
   sendResponse(p, resp);
   resp_free(resp);
   return HSE_STATUS_SUCCESS;
}

static DWORD die(EXTENSION_CONTROL_BLOCK *p, const char *msg, int status)
{
   HTTPResponse *resp;
   WOLog(WO_ERR,"Sending aborting request - %s",msg);
   resp = resp_errorResponse(msg, status);
   return die_resp(p, resp);
}

/* You're responsible for freeing the header returned ! */
static char *getHeader(EXTENSION_CONTROL_BLOCK *p, const char *key)
{
#define BUFSZ 2048
   char temp_hdr[BUFSZ];
   DWORD	len = BUFSZ;
   char *hdr = NULL;

   if (p->GetServerVariable(p->ConnID, (char *)key,  temp_hdr, &len) == TRUE) {
      if (len!=0) {
         /* len includes the terminating 0 */
         hdr = WOMALLOC((len)*sizeof(char));
         strncpy(hdr, temp_hdr, len);
         hdr[len-1]='\0';
         return hdr;
      }
   }
   return NULL;
}

static void copyHeaderForServerVariable(char *var, EXTENSION_CONTROL_BLOCK *p, HTTPRequest *req) {
    char *buf, *value, stackBuf[2048];
    DWORD bufsz = sizeof(stackBuf), pos;

    WOLog(WO_DBG, "reading buffer for server variable %s", var);
    if (p->GetServerVariable(p->ConnID, var, stackBuf, &bufsz) == TRUE) {
        buf = stackBuf;
    } else {
        if(GetLastError() == 122) // ERROR_INSUFFICIENT_BUFFER
        {
            WOLog(WO_DBG, "buffer too small; need %d", bufsz);
            buf = WOMALLOC(bufsz);
            if (p->GetServerVariable(p->ConnID, var, buf, &bufsz) != TRUE)
            {
                WOFREE(buf);
                WOLog(WO_ERR, "Could not get header.");
                return;
            }
        } else
            return; // header not set
    }

    if (buf) {
        //WOLog(WO_DBG, "got raw buffer: %s", buf);
        pos = 0;
        while (pos < bufsz) {
            while (pos < bufsz && buf[pos] < 33)
                pos++;
            if (pos < bufsz)  {
                /* got start of new value */
                value = &buf[pos];
                do {
                    while (pos < bufsz && buf[pos] != '\r')
                        pos++;
                    if (pos + 2 < bufsz && buf[pos+1] == '\n' && (buf[pos+2] == ' ' || buf[pos+2] == '\t')) {
                        /* got a multiline header; change CRLF to whitespace and keep parsing */
                        buf[pos] = ' ';
                        buf[pos+1] = ' ';
                    } else {
                        buf[pos] = 0;
                    }
                } while (pos < bufsz && buf[pos] != 0);
                WOLog(WO_DBG, "found value=\"%s\"", value ? value : "(NULL)");
                if (value)
                    req_addHeader(req, var, value, STR_COPYKEY|STR_COPYVALUE);
                else
                    req_addHeader(req, var, "", STR_COPYKEY);
            }
        }
        if (buf != stackBuf)
            WOFREE(buf);
    }

   return;
}

/** client certificate support
static char* make_cert_one_line(BYTE* bytes, int len) {
    char *str = (char*)malloc(sizeof(char)*len + 1);
    int i;
    for(i = 0 ; i < len ; i++) {
        //WOLog(WO_DBG, "Got byte %d", bytes[i]);
        if (bytes[i] == '\n') {
            str[i] = ' ';
        } else {
            str[i] = bytes[i];
        }
    }
    return str;
}
 end client certificate support **/

static void copyHeadersAllRaw(EXTENSION_CONTROL_BLOCK *p, HTTPRequest *req) {
    char *buf, *key, *value, stackBuf[2048];
    DWORD bufsz = sizeof(stackBuf), pos;

    /** client certificate support 
    BYTE* CertificateBuf = (BYTE*)calloc(MAX_CERT_SIZE, sizeof(BYTE));
    CERT_CONTEXT_EX ccex;
    ccex.cbAllocated = MAX_CERT_SIZE;
    ccex.CertContext.pbCertEncoded = CertificateBuf;
    end client certificate support **/

    if (p->GetServerVariable(p->ConnID, "ALL_RAW", stackBuf, &bufsz) == TRUE) {
        buf = stackBuf;
    } else {
        WOLog(WO_DBG, "buffer too small; need %d", bufsz);
        buf = WOMALLOC(bufsz);
        if (p->GetServerVariable(p->ConnID, "ALL_RAW", buf, &bufsz) != TRUE) {
            WOFREE(buf);
            WOLog(WO_ERR, "Could not get headers.");
            return;
        }
    }

    if (buf) {
        //WOLog(WO_DBG, "got raw buffer: %s", buf);
        pos = 0;
        while (pos < bufsz) {
            while (pos < bufsz && buf[pos] < 31)
                pos++;
            if (pos < bufsz)  {
                /* got start of new header */
                key = &buf[pos];
                value = NULL;
                /* search for ':' */
                while (pos < bufsz && buf[pos] != ':')
                    pos++;
                /* change ':' to 0 to terminate key */
                buf[pos] = 0;
                pos++;
                /* look for start of value */
                while (pos < bufsz && (buf[pos] == ' ' || buf[pos] == '\t'))
                    pos++;
                if (pos < bufsz && buf[pos] > 31) {
                    /* got start of value */
                    value = &buf[pos];
                    do {
                        while (pos < bufsz && buf[pos] != '\r')
                            pos++;
                        if (pos + 2 < bufsz && buf[pos+1] == '\n' && (buf[pos+2] == ' ' || buf[pos+2] == '\t')) {
                            /* got a multiline header; change CRLF to whitespace and keep parsing */
                            buf[pos] = ' ';
                            buf[pos+1] = ' ';
                        } else {
                            buf[pos] = 0;
                        }
                    } while (pos < bufsz && buf[pos] != 0);
                }

                // do not pass connection setting from client to WO app as this interferes
                //  with our own connection pooling
                if (strcasecmp(key, CONNECTION) == 0)
                    continue;
                
                WOLog(WO_DBG, "found key=\"%s\", value=\"%s\"", key, value ? value : "(NULL)");
                if (value)
                    req_addHeader(req, key, value, STR_COPYKEY|STR_COPYVALUE);
                else
                    req_addHeader(req, key, "", STR_COPYKEY);
            }
        }
        if (buf != stackBuf)
            WOFREE(buf);
    }

    /** client certificate support

    if (p->ServerSupportFunction(p->ConnID, HSE_REQ_GET_CERT_INFO_EX, (LPVOID)&ccex, 0, 0) == FALSE) {
        WOLog(WO_DBG, "Didn't get a certificate, oh well.");
    } else {
        // ccex now contains valid client certificate information.
        DWORD clen = ccex.CertContext.cbCertEncoded;
        char* cstr = make_cert_one_line((BYTE*)CertificateBuf, clen);
        //Don't bother copying the key or value
        req_addHeader(req, "SSL_CLIENT_CERT", cstr, 0);
        free(CertificateBuf);
        free(cstr);
    }
    end client certificate support **/

    return;
}

static void copyHeadersServerVariables(char *variables[], EXTENSION_CONTROL_BLOCK *p, HTTPRequest *req) {
    //WOLog(WO_DBG, "about to copy server variables");
    while (*variables) {
        copyHeaderForServerVariable(*variables++, p, req);
    }
}

static void copyHeaders(EXTENSION_CONTROL_BLOCK *p, HTTPRequest *req) {
    // Get the headers returned by ALL_RAW
    copyHeadersAllRaw(p, req);
    // Get everything else
    copyHeadersServerVariables((char **)iis_http_headers, p, req); // mstoll 13.10.2005 cast added
}



static int readContentData(HTTPRequest *req, void *dataBuffer, int dataSize, int mustFill)
{
    EXTENSION_CONTROL_BLOCK *p = (EXTENSION_CONTROL_BLOCK *)req->api_handle;
    DWORD len_remaining = dataSize;
    DWORD total_len_read = 0;
    char *buffer = (char *)dataBuffer;
    MS_BOOL readStatus;
    unsigned int lenZeroCounter = 0;

    DWORD len;
    if(p->cbAvailable > req->total_len_read)
    {
        len = p->cbAvailable - req->total_len_read;
        if(len > dataSize) len = dataSize;
        memcpy(buffer, p->lpbData + req->total_len_read, len);
        
        total_len_read += len;
        len_remaining -= len;
    }

    /*
     * IIS has a weird (or is it convenient?) data queuing mechanism...
     */
    while(len_remaining > 0 &&
          (mustFill || total_len_read == 0))
    {
        len = len_remaining;
        readStatus = p->ReadClient (p->ConnID,buffer + total_len_read, &len);
        if(readStatus == TRUE)
        {
           // 2009/04/29: avoid endless loops, because the ReadClient function
           //             will return TRUE but with zero bytes read if the
           //             socket on which the server is listening to the client
           //             is closed!!!
           lenZeroCounter =
              ((len == 0)? (lenZeroCounter + 1) : 0);
           if(lenZeroCounter > MAGIC_LEN_ZERO_LIMIT)
           {
               readStatus = FALSE;
           }
        }

        if(readStatus != TRUE)
        {
           if(lenZeroCounter > MAGIC_LEN_ZERO_LIMIT)
           {
              WOLog(WO_ERR,"ReadClient failed (client socket closed?).");
           }
           else
           {
              WOLog(WO_ERR,"ReadClient failed");
           }
           die(p, INV_FORM_DATA, HTTP_BAD_REQUEST);
           return -1;
        }

        total_len_read += len;
        len_remaining -= len;
    }

    // still required? - BEGIN
    if (req_HeaderForKey(req, CONTENT_LENGTH) == NULL) {
       char *length;
       length = (char *)WOMALLOC(32);
       if (length)
       {
          sprintf(length,"%d",req->content_length);
          req_addHeader(req, CONTENT_LENGTH, length, STR_FREEVALUE);
       }
       if (p->lpszContentType != NULL)
          req_addHeader(req, CONTENT_TYPE, p->lpszContentType, 0);
    }
    // still required? - END

    req->total_len_read += total_len_read;
    return total_len_read;
}



/*
 *	the thing that gets called...
 */
__declspec(dllexport) DWORD __stdcall HttpExtensionProc(EXTENSION_CONTROL_BLOCK *p)
{
   HTTPRequest *req;
   HTTPResponse *resp = NULL;
   WOURLComponents wc = WOURLComponents_Initializer;
   const char *reqerr;
   const char *qs;
   char *script_name;
   char *server_protocol;
   char *uri;
   WOURLError urlerr;

   if (!adaptorEnabled)
   {
      WOLog(WO_ERR, "WebObjects adaptor disabled.");
      return HSE_STATUS_ERROR;
   }
   
   /*
    *	extract WebObjects request components from URI
    */
   script_name = getHeader(p, CGI_SCRIPT_NAME);
   uri = WOMALLOC(strlen(p->lpszPathInfo) + strlen(script_name) + 1);
   strcpy(uri, script_name);
   strcat(uri, p->lpszPathInfo);
   WOLog(WO_INFO,"<WebObjects ISAPI> new request: %s", uri);
   WOFREE(script_name);

   urlerr = WOParseApplicationName(&wc, uri);

   if (urlerr != WOURLOK) {
      const char *_urlerr;
      _urlerr = WOURLstrerror(urlerr);
      WOLog(WO_INFO,"URL Parsing Error: %s", _urlerr);
      if (urlerr == WOURLInvalidApplicationName) {
          if (ac_authorizeAppListing(&wc)) {
              resp = WOAdaptorInfo(NULL, &wc);
              WOFREE(uri); /* this has to be freed before a return in this function */
              return die_resp(p, resp);
          } else {
              WOFREE(uri); /* this has to be freed before a return in this function */
              return die(p, _urlerr, HTTP_NOT_FOUND);
          }
      }
      WOFREE(uri); /* this has to be freed before a return in this function */
      return die(p, _urlerr, HTTP_BAD_REQUEST);
   }

   /*
    *	build the request...
    */
   req = req_new(p->lpszMethod, NULL);
   req->api_handle = p;

   /*
    *	get the headers....
    */
   copyHeaders(p, req);

   /*
    *	validate the method
    */
   reqerr = req_validateMethod(req);
   if (reqerr) {
      req_free(req);
      WOFREE(uri); /* this has to be freed before a return in this function */
      return die(p,reqerr, HTTP_BAD_REQUEST);
   }

   /*
    *	get form data if any
    *   assume that POSTs with content length will be reformatted to GETs later
    */
   req->content_length = p->cbTotalBytes;
   if (req->content_length > 0)
   {
      req_allocateContent(req, req->content_length, 1);
      req->getMoreContent = (req_getMoreContentCallback)readContentData;
      req->total_len_read = 0;

      if (req->content_buffer_size == 0)
      {
          WOFREE(uri); /* this has to be freed before a return in this function */
          req_free(req);
          return die(p, ALLOCATION_FAILURE, HTTP_SERVER_ERROR);
      }
      if (readContentData(req, req->content, req->content_buffer_size, 1) == -1) {
         WOFREE(uri); /* this has to be freed before a return in this function */
         req_free(req);
         return die(p, WOURLstrerror(WOURLInvalidPostData), HTTP_BAD_REQUEST);
      }
   }

   /* Always get the query string */
   qs = p->lpszQueryString;
   wc.queryString.start = qs;
   wc.queryString.length = qs ? strlen(qs) : 0;

   /*
    *	message the application & collect the response
    *
    *	note that handleRequest free()'s the 'req' for us
    */
   if (resp == NULL) {
      /* if no error so far... */
      server_protocol = getHeader(p, CGI_SERVER_PROTOCOL);
      resp = tr_handleRequest(req, uri, &wc, server_protocol, NULL);
      WOFREE(server_protocol);
   }

   if (resp != NULL) {
      sendResponse(p, resp);
      resp_free(resp);
   }

   WOFREE(uri); /* this has to be freed before a return in this function */
   req_free(req);
#if defined(FINDLEAKS)
      showleaks();
#endif
      return HSE_STATUS_SUCCESS;
}


/*
 *	get the stuff from the registry
 */
typedef struct _regthing {
   const char * const regKey;
   const char * const myKey;
} regthing;

static const regthing options[] = {
    { "WOUSERNAME", WOUSERNAME },
    { "WOPASSWORD", WOPASSWORD },
    { "CONF_INTERVAL", WOCNFINTVL },
    { "CONF_URL", WOCONFIG },
    { "LOG_FLAG", WOLOGFLAG },
    { "LOG_PATH", WOLOGPATH },
    { "WEBOBJECTS_OPTIONS", WOOPTIONS },
    { NULL, NULL }
};
#define	MAX_VAL_LENGTH	4096

static strtbl *read_registry_config()
{
   const regthing *info;
   strtbl *config;
   char value[MAX_VAL_LENGTH];

   config = st_new(12);
   for (info=options; info->regKey != NULL; info++) {
      if (WOReadKeyFromConfiguration(info->regKey, value, MAX_VAL_LENGTH))
         st_add(config,info->myKey,value,STR_COPYVALUE|STR_FREEVALUE);
   }
   return config;
}


