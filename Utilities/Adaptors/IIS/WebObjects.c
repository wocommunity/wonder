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

__declspec(dllexport) MS_BOOL GetExtensionVersion (HSE_VERSION_INFO *pVer)
{
   pVer->dwExtensionVersion = CURRENT_WOF_VERSION_MAJOR;
   pVer->dwExtensionVersion = (pVer->dwExtensionVersion << 16 ) | CURRENT_WOF_VERSION_MINOR;

   strcpy(pVer->lpszExtensionDesc, "WebObjects Extension DLL for IIS Servers by Apple Computer, Inc.  version " ADAPTOR_VERSION);
   return TRUE;
}

static void sendResponse(EXTENSION_CONTROL_BLOCK *p, HTTPResponse *resp)
{
   String *resphdrs;
   char status[128];
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
         WOLog(WO_ERR,"Failed to send response headers (%d)", GetLastError());
      } else {
         len = 2;
         if (p->WriteClient(p->ConnID, CRLF, &len, 0) != TRUE)
            WOLog(WO_ERR,"Failed to send \\r\\n (%d)", GetLastError());
         /*
          *	send the content data
          */
         len = resp->content_length;
         if (len && (p->WriteClient(p->ConnID, resp->content, &len, 0) != TRUE))
            WOLog(WO_ERR,"Failed to send content (%d)", GetLastError());
      }
      str_free(resphdrs);
   } /* else? return warning */

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
   WOLog(WO_ERR,"WebObjects",NULL,NULL,"Aborting request - %s",msg);
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

    //WOLog(WO_DBG, "reading buffer for server variable %s", var);
    if (p->GetServerVariable(p->ConnID, var, stackBuf, &bufsz) == TRUE) {
        buf = stackBuf;
    } else {
        //WOLog(WO_DBG, "buffer too small; need %d", bufsz);
        buf = WOMALLOC(bufsz);
        if (p->GetServerVariable(p->ConnID, var, buf, &bufsz) != TRUE) {
            WOFREE(buf);
            WOLog(WO_ERR, "Could not get header.");
            return;
        }
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
                //WOLog(WO_DBG, "found key=\"%s\", value=\"%s\"", key, value ? value : "(NULL)");
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
    copyHeadersServerVariables(iis_http_headers, p, req);
}



/*
 *	the thing that gets called...
 */
__declspec(dllexport) DWORD HttpExtensionProc(EXTENSION_CONTROL_BLOCK *p)
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
    *	get the headers....
    */
   copyHeaders(p, req);

   /*
    *	get form data if any
    *   assume that POSTs with content length will be reformatted to GETs later
    */
   req->content_length = p->cbTotalBytes;
   if (req->content_length > 0)
   {
      char *buffer = WOMALLOC(req->content_length);

      if (p->cbAvailable > 0)
         memcpy(buffer, p->lpbData, p->cbAvailable);

      /*
       * IIS has a weird (or is it convenient?) data queuing mechanism...
       */
      if (req->content_length > p->cbAvailable) {
          DWORD len;
          DWORD totalLength, fetchedLength;

          len = req->content_length - p->cbAvailable;
          totalLength = len;
          fetchedLength = 0;

          while (fetchedLength < totalLength) {
              len = totalLength - fetchedLength;

              if (p->ReadClient (p->ConnID,buffer + p->cbAvailable + fetchedLength, &len) != TRUE) {
                  WOFREE(buffer);
                  req_free(req);
                  return die(p, INV_FORM_DATA, HTTP_BAD_REQUEST);
              }

              fetchedLength += len;
          }
      }
      req->content = buffer;
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
      req->api_handle = p;			/* stash this... */
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


