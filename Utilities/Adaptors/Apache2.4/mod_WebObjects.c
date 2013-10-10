/*

 Copyright 2000 - 2007 Apple, Inc. All Rights Reserved.

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
 *	See the Apache source for Apache copyright information.
 *
 *	WebObjects server adaptor Apache 2.2.x API module.
 *	This port to Apache 2.2.x by Travis Cripps July, 2006 (tcrippsatgmaildotcom)
 *
 *	This adaptor forwards WebObjects requests to the WebObjects application
 *	server from within the Apache server.
 *
 *      To configure the adaptor, the following "httpd.conf"/"apache.conf"
 *      directives are used:
 *      WebObjectsDocumentRoot          path relative to server doc root
 *      WebObjectsAlias                 WebObjects request alias
 *      WebObjectsConfig                uri[,interval-in-seconds]
 *      WebObjectsAdaptorInfo           admin app name or "NULL" to disable
 *      WebObjectsAdminUsername         WOAdaptorInfo user name or "disabled" to disable
 *      WebObjectsAdminPassword         WOAdaptorInfo password
 *      WebObjectsLog                   path-to-WebObjects.log, logging level
 *      WebObjectsOptions               additional adaptor options
 *
 */

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


#if	defined(WIN32)
#include <sys/types.h>
#include <sys/stat.h>
#include <io.h>
#include <time.h>
#endif

#include <httpd.h>
#include <http_config.h>
#include <http_request.h>
#include <http_log.h>
#include <http_main.h>
#include <http_core.h>		/* this is not recommended by Apache */
#include <http_protocol.h>
#include <string.h>


#include <apr.h>
#include <ap_compat.h>
#include <apr_strings.h>
#include <apr_tables.h>

#ifdef APACHE_SECURITY_ENABLED
#include <apr_optional.h>
#include <apr_base64.h>
#include <mod_ssl.h>
#endif


#define WEBOBJECTSALIAS 				"/cgi-bin/" WEBOBJECTS
#define	WEBOBJECTS_MAGIC_TYPE			"application/x-httpd-webobjects"
#define WebObjectsDocRoot				"WebObjectsDocumentRoot"
#define WebObjectsAlias					"WebObjectsAlias"
#define WebObjectsConfig				"WebObjectsConfig"
#define WebObjectsAdaptorInfo			"WebObjectsAdaptorInfo"
#define WebObjectsAdminUsername			"WebObjectsAdminUsername"
#define WebObjectsAdminPassword			"WebObjectsAdminPassword"
#define WebObjectsLog					"WebObjectsLog"
#define WebObjectsOptions				"WebObjectsOptions"

/*
 *	this is to enable logging to Apache's log
 *	we set it whenever we see a server rec
 */
server_rec *_webobjects_server;

static int initCalled = 0;
static int adaptorEnabled = 0;
char *WA_adaptorName = "Apache";


/*
 * This module
 */
extern module AP_MODULE_DECLARE_DATA WebObjects_module;

/*
 * This modules per-server configuration structure.
 */
typedef struct _WebObjects_config {
    const char *root;				/* normally Documents/WebObjects or htdocs/WebObjects */
    const char *WebObjects_alias;	/* normally "WebObjects" */
    strtbl *options;				/* configuration options */
} WebObjects_config;



#ifdef APACHE_SECURITY_ENABLED
/* ******* client cert support ********* */

// NOTE: Thanks to the mod_python folks for the wrapper function idea. - TC

/* Optional functions imported from mod_ssl when loaded: */
static APR_OPTIONAL_FN_TYPE(ssl_var_lookup) *optfn_ssl_var_lookup = NULL;
static APR_OPTIONAL_FN_TYPE(ssl_is_https) *optfn_is_https = NULL;

/**
 * mod_ssl ssl_is_https() wrapper
 * This function tests the request to see if it's https.
 * param r - the incoming request record
 * return 1 if the request is https
 */
static int req_is_https(request_rec *r)
{
	int is_https;

	if (!optfn_is_https) {
		optfn_is_https = APR_RETRIEVE_OPTIONAL_FN(ssl_is_https);
	}

	is_https = optfn_is_https && optfn_is_https(r->connection);

	return is_https;
}


/**
 * mod_ssl ssl_var_lookup() wrapper
 * This function tests the request to see if it's https.
 * param r - the incoming request record
 * param var_name - the name of the value to look up from the ssl module
 * return a char* if the value is found; NULL if not
 */
static const char *req_ssl_var_lookup(request_rec *r, char *var_name)
{
	if (!optfn_ssl_var_lookup) {
		optfn_ssl_var_lookup = APR_RETRIEVE_OPTIONAL_FN(ssl_var_lookup);
	}

	if (optfn_ssl_var_lookup) {
		const char *val;
		val = optfn_ssl_var_lookup(r->pool,
								r->server,
								r->connection,
								r,
								var_name);
		if (val) {
			return val;
		}
	}

	/* variable not found, or mod_ssl is not loaded */
	return NULL;
}


/* ********** client cert support ***********/
#endif /* APACHE_SECURITY_ENABLED */





/*
 * This function is called when the "WebObjectsDocumentRoot" configuration directive is parsed.
 */
static const char *setDocroot(cmd_parms *cmd, void *key, const char *value)
{
    // get the module configuration (this is the structure created by WebObjects_create_config())
    WebObjects_config *s_cfg = ap_get_module_config(cmd->server->module_config, &WebObjects_module);

    // make a duplicate of the argument's value using the command parameters pool.
    s_cfg->root = (char *)value;

    // success
    return NULL;
}

/*
 * This function is called when the "WebObjectsAlias" configuration directive is parsed.
 */
static const char *setScriptAlias(cmd_parms *cmd, void *key, const char *value)
{
    // get the module configuration (this is the structure created by WebObjects_create_config())
    WebObjects_config *s_cfg = ap_get_module_config(cmd->server->module_config, &WebObjects_module);

    // make a duplicate of the argument's value using the command parameters pool.
    s_cfg->WebObjects_alias = (char *)value;

    // success
    return NULL;
}

static const char *setOption(cmd_parms *cmd, void *key, const char *value) {

    // get the module configuration (this is the structure created by WebObjects_create_config())
    WebObjects_config *wc = ap_get_module_config(cmd->server->module_config, &WebObjects_module);

    /* copy because the config file data goes away on some platforms */
    st_add(wc->options, cmd->info, value, STR_COPYKEY|STR_COPYVALUE);

    // success
    return NULL;

}

static const char *setOption2(cmd_parms *cmd, void *keys, const char *v1, const char *v2) {
    char key1[128], *key2;

    // get the module configuration (this is the structure created by WebObjects_create_config())
    WebObjects_config *wc = ap_get_module_config(cmd->server->module_config, &WebObjects_module);

    strcpy(key1, cmd->info);
    key2 = strchr(key1, ',');
    *key2++ = '\0';

    /* copy because the config file data goes away on some platforms */
    st_add(wc->options, key1, v1, STR_COPYKEY|STR_COPYVALUE);
    st_add(wc->options, key2, v2, STR_COPYKEY|STR_COPYVALUE);

    return NULL;
}

static const char *setOption3(cmd_parms *cmd, void *keys, const char *v1, const char *v2, const char *v3) {
    char key1[128], *key2, *key3;

    // get the module configuration (this is the structure created by WebObjects_create_config())
    WebObjects_config *wc = ap_get_module_config(cmd->server->module_config, &WebObjects_module);

    strcpy(key1, cmd->info);
    key2 = strchr(key1, ',');
    *key2++ = '\0';
    key3 = strchr(key2, ',');
    *key3++ = '\0';

    /* copy because the config file data goes away on some platforms */
    st_add(wc->options, key1, v1, STR_COPYKEY|STR_COPYVALUE);
    st_add(wc->options, key2, v2, STR_COPYKEY|STR_COPYVALUE);
    st_add(wc->options, key3, v3, STR_COPYKEY|STR_COPYVALUE);

    return NULL;
}


/*
 *	see add_common_vars() for a peek at how mod_cgi sets up the environment
 *	array
 */
static int copyTableEntries(void *req, const char *key, const char *val) {
    if(strcmp(key, "SSL_SERVER_CERT") != 0 &&
        strcmp(key, "SSL_CLIENT_CERT") != 0)
        req_addHeader((HTTPRequest *)req, key, val, 0);
    return 1;
}


/**
 * Copies all the http(s) headers "of interest" from the incoming to the outgoing request.
 */
static void copyHeaders(request_rec *r, HTTPRequest *req) {
    server_rec *s = r->server;
    conn_rec *c = r->connection;
    apr_table_t *hdrs = r->headers_in;

    /* extra headers from http://httpd.apache.org/dev/apidoc/apidoc_request_rec.html */
    apr_table_t *proc_env = r->subprocess_env;
    apr_table_t *nts = r->notes;

    char *port;
    const char *rem_logname;

    /*
     *	some we can copy blindly
     */
    apr_table_do(copyTableEntries, (void *)req, hdrs, NULL);

    /* copy extra headers */
    apr_table_do(copyTableEntries, (void *)req, proc_env, NULL);
    apr_table_do(copyTableEntries, (void *)req, nts, NULL);


#ifdef APACHE_SECURITY_ENABLED
	/******client cert support***** */

	if (req_is_https(r)) {
		const char *hdrValue;
		const char *cert;
		int certLen = 0;

		WOLog(WO_DBG,"We have ssl");

		hdrValue = 0;
		cert = 0;

		hdrValue = req_ssl_var_lookup(r, "SSL_CLIENT_S_DN");
		req_addHeader(req, "SSL_CLIENT_CERT_CN", hdrValue, 0);

		// Get the protocol version from r and add it to the req.
		hdrValue = req_ssl_var_lookup(r, "SSL_PROTOCOL");
		req_addHeader(req, "SSL_PROTOCOL_VERSION", hdrValue, 0);

		// Get the ssl cert.
		cert = req_ssl_var_lookup(r, "SSL_CLIENT_CERT");
		/* the cert length */
		certLen = strlen(cert);

		// Make sure the cert is a valid length; < 1 is probably not valid.
		if (certLen < 1) {
			WOLog(WO_ERR, "invalid certificate length: %d ", certLen);
		} else {
			// Alloc space for the base64 encoded form of the cert.
			char *encodedHdr = apr_pcalloc(r->pool, apr_base64_encode_len(certLen));

			// Convert the cert to base64 and add it to the headers if successful.
			if ( apr_base64_encode(encodedHdr, cert, certLen) ) {
				req_addHeader(req, "SSL_CLIENT_CERT", encodedHdr, 0);
			}
			//WOLog(WO_DBG, "This is what I got: %s\n", encodedHdr);
		}

    } // end if (req_is_https)

    /* *****client cert support******* */
#endif /* APACHE_SECURITY_ENABLED */

    /*
     *	collect up the server specific headers
     */
#if defined(APACHE_SERVERSIGNATURE)
    req_addHeader(req, "SERVER_SIGNATURE", ap_psignature("", r), 0);
#endif
    req_addHeader(req, "SERVER_SOFTWARE", ap_get_server_banner(), 0);
    req_addHeader(req, "SERVER_NAME", s->server_hostname, 0);

    port = (char *)WOMALLOC(32);
    if (port) {
        apr_snprintf(port, sizeof(port), "%d", ap_get_server_port(r));
        req_addHeader(req, "SERVER_PORT", port, STR_FREEVALUE);
    }

    req_addHeader(req, "REMOTE_HOST",
                  (const char *)ap_get_remote_host(c, r->per_dir_config, REMOTE_NAME, NULL), 0);
    req_addHeader(req, "REMOTE_ADDR", r->useragent_ip, 0);
    req_addHeader(req, "DOCUMENT_ROOT", (char *)ap_document_root(r), 0);
    req_addHeader(req, "SERVER_ADMIN", s->server_admin, 0);
    req_addHeader(req, "SCRIPT_FILENAME", r->filename, 0);

    port = (char *)WOMALLOC(32);
    if (port) {
        apr_snprintf(port, 32, "%d", ntohs(r->useragent_addr->sa.sin.sin_port));
        req_addHeader(req, "REMOTE_PORT", port, STR_FREEVALUE);
    }

    if (r->user != NULL) {
		req_addHeader(req, "REMOTE_USER", r->user, 0);
    }

    if (r->ap_auth_type != NULL) {
		req_addHeader(req, "AUTH_TYPE", r->ap_auth_type, 0);
    }

    rem_logname = (char *)ap_get_remote_logname(r);
    if (rem_logname != NULL) {
		req_addHeader(req, "REMOTE_IDENT", rem_logname, 0);
    }

    /*
     *	Apache custom responses.  If we have redirected, add special headers
     */
	if (r->prev) {
		if (r->prev->args) {
			req_addHeader(req, "REDIRECT_QUERY_STRING", r->prev->args, 0);
		}

		if (r->prev->uri) {
			req_addHeader(req, "REDIRECT_URL", r->prev->uri, 0);
		}
	}

    return;
}



static void gethdr(const char *key, const char *value, void *req) {
    request_rec *r = (request_rec *)req;

    if ((r->content_type == NULL) && (strcasecmp(CONTENT_TYPE, key) == 0)) {
        r->content_type = (char *)apr_pstrdup(r->pool, value);
    } else {
        apr_table_add(r->headers_out, key, value);
    }
}


static void sendResponse(request_rec *r, HTTPResponse *resp) {
    char status[500];

    /*
     *	collect up the headers
     */
    st_perform(resp->headers, gethdr, r);

    apr_snprintf(status, sizeof(status), "%u %s", resp->status, resp->statusMsg);
    r->status_line = status;
    r->status = resp->status;
    if (r->content_type == NULL) {
	r->content_type = "text/html";
    }

   if((resp->flags & RESP_LENGTH_EXPLICIT) == RESP_LENGTH_EXPLICIT)
	    ap_set_content_length(r, resp->content_length);


    /*
     *	actually transmit the response to the client
     */
    /* This should cause Apache to send the headers */
    ap_rflush(r);

    /* resp->content_valid will be 0 for HEAD requests and empty responses */
    if ( (!r->header_only) && (resp->content_valid) )
    {
        while (resp->content_read < resp->content_length &&
            (resp->flags & RESP_LENGTH_INVALID) != RESP_LENGTH_INVALID)
        {
            int count;

            ap_rwrite(resp->content, resp->content_valid, r);
            if (r->connection->aborted) {
                break;
            }

            count = resp_getResponseContent(resp, 1);
            if(count > 0)
            {
             // 2009/06/09: handle situations where content_length is wrong or
             //             unset.  Read as much data as possible from the
             //             WebObjects application and send the data to the
             //             client-side.
            resp->content_read += count;
            resp->content_valid = count;
            }
        }

        ap_rwrite(resp->content, resp->content_valid, r);
    }

    return;
}



static int die_resp(request_rec *r, HTTPResponse *resp) {
    sendResponse(r, resp);
    resp_free(resp);
    return DECLINED;
}

static int die(request_rec *r, const char *msg, int status) {
    HTTPResponse *resp;

    WOLog(WO_ERR, "Sending error response: %s", msg);
    resp = resp_errorResponse(msg, status);
    return die_resp(r, resp);
}


/* Read up to dataSize bytes into the buffer at dataBuffer. */
/* Returns the number of bytes read, or -1 on error. */
static int readContentData(HTTPRequest *req, void *dataBuffer, int dataSize, int mustFill) {
    request_rec *r = (request_rec *)req->api_handle;
    int len_remaining = dataSize;
    /* set up to get content data */
    int len_read, total_len_read = 0;
    char *data = (char *)dataBuffer;

    while ((len_remaining > 0 && mustFill) || (total_len_read == 0)) {
        len_read = ap_get_client_block(r, data, len_remaining);

        if (len_read <= 0) {
            return -1;
        }

        total_len_read += len_read;
        data += len_read;
        len_remaining -= len_read;
    }

    if (total_len_read == 0) {
	WOLog(WO_WARN, "readContentData(): returning zero bytes of content data");
    }

    return total_len_read;
}



/*
 * A declaration of the configuration directives that are supported by this module.
 * The apr_table_t of commands we provide
 */
static const command_rec WebObjects_cmds[] =
{
    // Document Root
    AP_INIT_TAKE1(
                  WebObjectsDocRoot,
                  setDocroot,
                  NULL,
                  RSRC_CONF,
                  "WebObjectsDocumentRoot <string> -- The path to the document root of your web server, which should contain a directory named 'WebObjects'"
                  ),
    // Alias
    AP_INIT_TAKE1(
                  WebObjectsAlias,
                  setScriptAlias,
                  NULL,
                  RSRC_CONF,
                  "WebObjectsAlias <string> -- The WebObjects request alias."
                  ),
    AP_INIT_TAKE2(
                  WebObjectsConfig,
                  setOption2,
                  WOCONFIG "," WOCNFINTVL,
                  RSRC_CONF,
                  "WebObjectsConfig <string> -- The WebObjects configuration URI and the read interval."
                  ),
    AP_INIT_TAKE1(
                  WebObjectsAdminUsername,
                  setOption,
                  WOUSERNAME,
                  RSRC_CONF,
                  "WebObjectsAdminUsername <string> -- The WOAdaptorInfo username or 'NULL'."
                  ),
    AP_INIT_TAKE1(
                  WebObjectsAdminPassword,
                  setOption,
                  WOPASSWORD,
                  RSRC_CONF,
                  "WebObjectsAdminPassword <string> -- The WOAdaptorInfo password or 'NULL'."
                  ),
    AP_INIT_TAKE2(
                  WebObjectsLog,
                  setOption2,
                  WOLOGPATH "," WOLOGLEVEL,
                  RSRC_CONF,
                  "WebObjectsLog <string> -- The path for the WebObjects log file, and the log level."
                  ),
    AP_INIT_TAKE1(
                  WebObjectsOptions,
                  setOption,
                  WOOPTIONS,
                  RSRC_CONF,
                  "WebObjectsOptions <string> -- Any additional adaptor options."
                  ),
    {NULL}
};




/*
 * Creates the per-server configuration records.
 */
static void *WebObjects_create_config(apr_pool_t *p, server_rec *s)
{
    WebObjects_config *wc;

    // allocate space for the configuration structure from the provided pool p.
    wc = (WebObjects_config *) apr_pcalloc(p, sizeof(WebObjects_config));

    // set the default value for the document root.
    wc->root = NULL;

    // set the default value for the alias.
    wc->WebObjects_alias = (char *)WEBOBJECTS;

    // set the options
    wc->options = st_new(8);

    // return the new server configuration structure.
    return (void *) wc;
}



/*
 *
 *	A good time to read configuration files, etc....
 *
 *	We get the WebObjects.xml file path from the configuration.  The default is
 *	to assume it's in the apache conf directory.
 *
 */
static int WebObjects_post_config(apr_pool_t *pconf, apr_pool_t *plog,
                                   apr_pool_t *ptemp, server_rec *s)  {
    WebObjects_config *wc;

    if(!initCalled) {
        _webobjects_server = s;
        wc = (WebObjects_config *)ap_get_module_config(s->module_config, &WebObjects_module);

        /*
         *	allow other bits of the adaptor to pick up the options
         */

        if (init_adaptor(wc->options) == 0)
        {
            WOLog(WO_INFO, "WebObjects_post_config(): WebObjects adaptor initialization succeeded.");
            adaptorEnabled = 1;
        } else {
            WOLog(WO_ERR, "WebObjects_post_config(): Adaptor initialization failed. All requests will be declined.");
            return DECLINED;
        }

        initCalled = 1;
    }

    return OK;
}


/*
 *	called when Apache forks a new child server
 */
static void WebObjects_child_init(apr_pool_t *p, server_rec *s) {
    _webobjects_server = s;

    /*
     *	anything else?
     *    ac_readConfiguration();	force the configs to be checked
     */
    return;
}


/*
 *	WebObjectsAlias name translation.
 *
 *	Look for our key & if found, schedule our handler...
 */
int WebObjects_translate(request_rec *r) {
    WebObjects_config *wc;
    WOURLComponents url;
    WOURLError urlerr;

    // get the module configuration (this is the structure created by WebObjects_create_config())
    wc = ap_get_module_config(r->server->module_config, &WebObjects_module);

    WOLog(WO_DBG, "<WebObjects Apache Module> new translate: %s", r->uri);
    if (strncmp(wc->WebObjects_alias, r->uri, strlen(wc->WebObjects_alias)) == 0 || (r->handler != NULL && strcasecmp(r->handler, WEBOBJECTS) == 0)) {
#ifndef _MSC_VER // SWK changed url = WOURLComponents_Initializer; to memset(&url,0,sizeof(WOURLComponents));
        url = WOURLComponents_Initializer;
#else
		memset(&url,0,sizeof(WOURLComponents));
#endif
        urlerr = WOParseApplicationName(&url, r->uri);
        if (urlerr != WOURLOK && !((urlerr == WOURLInvalidApplicationName) && ac_authorizeAppListing(&url))) {
            WOLog(WO_DBG, "<WebObjects Apache Module> translate - DECLINED: %s", r->uri);
            return DECLINED;
        }
        if (!adaptorEnabled)
        {
            WOLog(WO_ERR, "WebObjects_translate(): declining request due to initialization failure");
            return DECLINED;
        }


        /*
         *	we'll take it - mark our handler...
         */
        r->handler = (char *)apr_pstrdup(r->pool, WEBOBJECTS);
        r->filename = (char *)apr_pstrdup(r->pool, r->uri);

        return OK;
    }

    WOLog(WO_DBG, "<WebObjects Apache Module> translate - DECLINED: %s", r->uri);

    return DECLINED;
}


/*
 * This function is registered as a handler for HTTP methods and will
 * therefore be invoked for all GET requests (and others).
 */
static int WebObjects_handler (request_rec *r)
{
    WebObjects_config *conf;
    HTTPRequest *req;
    HTTPResponse *resp = NULL;
#ifndef _MSC_VER // SWK changed url = WOURLComponents_Initializer;
    WOURLComponents wc = WOURLComponents_Initializer;
#else
    WOURLComponents wc;
#endif
    const char *reqerr;
    int retval;
    const char *docroot;
    WOURLError urlerr;
#ifdef _MSC_VER // SWK changed url = WOURLComponents_Initializer;
	memset(&wc,0,sizeof(WOURLComponents));
#endif
    /* 	We have to do a check here to see if our handler should process the request.
	The WebObjects_translate phase should  have marked the request to be handled
	by WebObjects if it matched the Adaptor's WebObjects alias.
	Is this the best way to do this?  Can another module override our r->handler setting?
	*/
	if (NULL == r->handler || strcasecmp(r->handler, WEBOBJECTS) != 0) {
		WOLog(WO_DBG, "WebObjects_handler declined! %s", r->uri);
		return DECLINED;
    }

    _webobjects_server = r->server;

    WOLog(WO_INFO, "<WebObjects Apache Module> new request: %s", r->uri);
    if (!adaptorEnabled) {
        WOLog(WO_ERR, "WebObjects_handler(): declining request due to initialization failure");
        return DECLINED;
    }

    int shouldProcessUrl = 1;
    const char *envApplicationName = apr_table_get(r->subprocess_env, "WOApplicationName");
    if (NULL != envApplicationName) {
        wc.prefix.start = 0;
        wc.prefix.length = 0;
        wc.applicationName.start = envApplicationName;
        wc.applicationName.length = strlen(envApplicationName);
        wc.requestHandlerPath.start = r->uri;
        wc.requestHandlerPath.length = strlen(r->uri);
        wc.webObjectsVersion.start = v4_url;
        wc.webObjectsVersion.length = URLVersionLen;

        shouldProcessUrl = 0;
        urlerr = WOURLOK;
    }
    else {
        urlerr = WOParseApplicationName(&wc, r->uri);
    }
    if (urlerr == WOURLOK) {
        WOLog(WO_DBG, "App Name: %s (%d)", wc.applicationName.start, wc.applicationName.length);
    } else {
        const char *_urlerr;
        _urlerr = WOURLstrerror(urlerr);
        WOLog(WO_INFO, "URL Parsing Error: %s", _urlerr);
        if (urlerr == WOURLInvalidApplicationName) {
            if (ac_authorizeAppListing(&wc)) {
                resp = WOAdaptorInfo(NULL, &wc);
                if (resp) {
                    sendResponse(r, resp);
                    resp_free(resp);
                    return OK;
                }
                die(r, _urlerr, HTTP_SERVER_ERROR);
            }
            die(r, _urlerr, HTTP_NOT_FOUND);
        }
        return die(r, _urlerr, HTTP_BAD_REQUEST);
    }

    /* See http://www.apache.org/docs/misc/client_block_api.html for doc on this stuff. */
    retval = ap_setup_client_block(r, REQUEST_CHUNKED_ERROR);
    if (retval != 0) {
	return retval;
    }


    /*
     *	build the request ....
     */
    req = req_new(r->method, NULL);
    req->shouldProcessUrl = shouldProcessUrl;
    req->api_handle = r;				/* stash this in case it's needed */

    /*
     *	validate the method
     */
    reqerr = req_validateMethod(req);
    if (reqerr) {
        req_free(req);
        return die(r, reqerr, HTTP_BAD_REQUEST);
    }

    /*
     *	copy the headers..
     */
    copyHeaders(r, req);

    /*
     *	get form data if any
     *   assume that POSTs with content length will be reformatted to GETs later
     */
    if ((req->content_length > 0) && ap_should_client_block(r) ) {
        req_allocateContent(req, req->content_length, 1);
        req->getMoreContent = (req_getMoreContentCallback)readContentData;

        if (req->content_buffer_size == 0) {
	    return die(r, ALLOCATION_FAILURE, HTTP_SERVER_ERROR);
	}

        if (readContentData(req, req->content, req->content_buffer_size, 1) == -1) {
            req_free(req);
            return die(r, WOURLstrerror(WOURLInvalidPostData), HTTP_BAD_REQUEST);
        }
    }

    /* Always get the query string */
    wc.queryString.start = r->args;
    wc.queryString.length = r->args ? strlen(r->args) : 0;

    /*
     *	find path to webobjects apps
     */
    conf = (WebObjects_config *)ap_get_module_config(r->per_dir_config, &WebObjects_module);
    docroot = (conf && (conf->root != NULL)) ? conf->root : (char *)ap_document_root(r);

    /*
     *	message the application & collect the response
     *
     *	note that handleRequest free()'s the 'req' for us
     */

    resp = tr_handleRequest(req, r->uri, &wc, r->protocol, docroot);

    /* send the response if we have one */
    if (resp != NULL) {
        sendResponse(r, resp);
        resp_free(resp);
        retval = OK;
    } else {
	retval = DECLINED;
    }

    req_free(req);

#if defined(FINDLEAKS)
    showleaks();		/* reveal any leaks in the adaptor */
#endif

    return retval;
}


/*
 * This function is a callback and it declares what other functions
 * should be called for request processing and configuration requests.
 * This callback function declares the Handlers for other events.
 */
static void WebObjects_register_hooks (apr_pool_t *p)
{
    ap_hook_post_config(WebObjects_post_config, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_child_init(WebObjects_child_init, NULL, NULL, APR_HOOK_FIRST);
    ap_hook_translate_name(WebObjects_translate, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_handler(WebObjects_handler, NULL, NULL, APR_HOOK_MIDDLE);
}


/*
 * Declare and populate the module's data structure.  The
 * name of this structure ('WebObjects_module') is important - it
 * must match the name of the module.  This structure is the
 * only "glue" between the httpd core and the module.
 */
module AP_MODULE_DECLARE_DATA WebObjects_module =
{
    STANDARD20_MODULE_STUFF, 	// standard stuff; no need to mess with this.
    NULL, 			// create per-directory configuration structures - we do not.
    NULL, 			// merge per-directory - no need to merge if we are not creating anything.
    WebObjects_create_config, 	// create per-server configuration structures.
    NULL, 			// merge per-server.
    WebObjects_cmds, 		// configuration directive handlers
    WebObjects_register_hooks 	// request handlers
};
