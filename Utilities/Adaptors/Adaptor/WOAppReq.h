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
#ifndef WOAPPREQ_H_INCLUDED
#define WOAPPREQ_H_INCLUDED
/*
 *	the struct used to pass information regarding this request 
 */


typedef	enum {
	err_none = 0,		/* everything's ok */
	err_notFound,		/* application name not found */
	err_noInstance,		/* no available instances */
	err_connect,		/* can't connect */
	err_send,		/* error occured while sending request */
	err_response,		/* invalid response recieved */
        err_read,               /* error reading content data from browser */
} RequestError;

/*
 *	information regarding the app
 */
typedef struct _WOAppReq {
   char name[WA_MAX_APP_NAME_LENGTH];	/*	relative to ../WebObjects */
   char host[WA_MAX_HOST_NAME_LENGTH];	/*	target host */
   void *hostent;			/*	(struct hostent *) for target host */
   int port;				/*	listen port */
   char instance[WA_MAX_INSTANCE_NUMBER_LENGTH];	/*	instance (if load balancing) */
   RequestError error;			/*	some error occured */
   unsigned char urlVersion;		/*	wof version 2, 3 or 4 url syntax */
   const char *docroot; 		/* 	doc root for this request */
   void *request;			/*	(HTTPRequest *)  */
   void *response;			/*	(HTTPResponse *) */
   struct _scheduler *scheduler;	/*      the scheduler to use for picking an instance for this request */
   char attemptedInstances[WA_MAX_APP_INSTANCE_COUNT+7/8]; /* bit array of which instances have been tried (and failed) */
   unsigned int schedulerFailed;	/* 	if the scheduler fails to select an instance for this request */
   char redirect_url[WA_MAX_URL_LENGTH];	/* in case of error */
} WOAppReq;

#endif
