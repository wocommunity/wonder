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
/********
*
*  Copyright (c) 1995  Process Software Corporation
*
*  Copyright (c) 1995  Microsoft Corporation
*
*
*  Module Name  : HttpExt.h
*
*  Abstract :
*
*     This module contains  the structure definitions and prototypes for the
*     version 1.0 HTTP Server Extension interface.
*
******************/

#ifndef _HTTPEXT_H
#define _HTTPEXT_H

#include <windows.h>

#define   HSE_VERSION_MAJOR           1      /* major version of this spec */
#define   HSE_VERSION_MINOR           0      /* minor version of this spec */
#define   HSE_LOG_BUFFER_LEN         80
#define   HSE_MAX_EXT_DLL_NAME_LEN  256

typedef   LPVOID  HCONN;

/* the following are the status codes returned by the Extension DLL */

#define   HSE_STATUS_SUCCESS                       1
#define   HSE_STATUS_SUCCESS_AND_KEEP_CONN         2
#define   HSE_STATUS_PENDING                       3
#define   HSE_STATUS_ERROR                         4

/* The following are the values to request services with the ServerSupportFunction. */
/*  Values from 0 to 1000 are reserved for future versions of the interface */

#define   HSE_REQ_BASE                             0
#define   HSE_REQ_SEND_URL_REDIRECT_RESP           ( HSE_REQ_BASE + 1 )
#define   HSE_REQ_SEND_URL                         ( HSE_REQ_BASE + 2 )
#define   HSE_REQ_SEND_RESPONSE_HEADER             ( HSE_REQ_BASE + 3 )
#define   HSE_REQ_DONE_WITH_SESSION                ( HSE_REQ_BASE + 4 )
#define   HSE_REQ_END_RESERVED                     1000
#define   HSE_REQ_IO_COMPLETION                    (HSE_REQ_END_RESERVED + 5)
#define   HSE_REQ_CLOSE_CONNECTION                 (HSE_REQ_END_RESERVED + 17)

/*
 * Flags for IO Functions, supported for IO Funcs.
 *  TF means ServerSupportFunction( HSE_REQ_TRANSMIT_FILE)
 */

# define HSE_IO_SYNC                      0x00000001   // for WriteClient
# define HSE_IO_ASYNC                     0x00000002   // for WriteClient/TF
# define HSE_IO_DISCONNECT_AFTER_SEND     0x00000004   // for TF
# define HSE_IO_SEND_HEADERS              0x00000008   // for TF

/*
* passed to GetExtensionVersion
 */

typedef struct   _HSE_VERSION_INFO {

   DWORD  dwExtensionVersion;
   CHAR   lpszExtensionDesc[HSE_MAX_EXT_DLL_NAME_LEN];

} HSE_VERSION_INFO, *LPHSE_VERSION_INFO;

/*
* passed to extension procedure on a new request
 */
typedef struct _EXTENSION_CONTROL_BLOCK {

   DWORD     cbSize;                 /* size of this struct. */
   DWORD     dwVersion;              /* version info of this spec */
   HCONN     ConnID;                 /* Context number not to be modified! */
   DWORD     dwHttpStatusCode;       /* HTTP Status code */
   CHAR      lpszLogData[HSE_LOG_BUFFER_LEN]; /* null terminated log info specific to this Extension DLL */

   LPSTR     lpszMethod;             /* REQUEST_METHOD */
   LPSTR     lpszQueryString;        /* QUERY_STRING */
   LPSTR     lpszPathInfo;           /* PATH_INFO */
   LPSTR     lpszPathTranslated;     /* PATH_TRANSLATED */

   DWORD     cbTotalBytes;           /* Total bytes indicated from client */
   DWORD     cbAvailable;            /* Available number of bytes */
   LPBYTE    lpbData;                /* pointer to cbAvailable bytes */

   LPSTR     lpszContentType;        /* Content type of client data */

   MS_BOOL (WINAPI * GetServerVariable) ( HCONN       ConnID,
                                          LPSTR       lpszVariableName,				  				                        LPVOID      lpvBuffer,
                                          LPDWORD     lpdwSize );

   MS_BOOL (WINAPI * WriteClient)  ( HCONN      ConnID,
                                     LPVOID     Buffer,
                                     LPDWORD    lpdwBytes,
                                     DWORD      dwReserved );

   MS_BOOL (WINAPI * ReadClient)  ( HCONN      ConnID,
                                    LPVOID     lpvBuffer,
                                    LPDWORD    lpdwSize );

   MS_BOOL (WINAPI * ServerSupportFunction)( HCONN      ConnID,
                                             DWORD      dwHSERRequest,
                                             LPVOID     lpvBuffer,
                                             LPDWORD    lpdwSize,
                                             LPDWORD    lpdwDataType );

} EXTENSION_CONTROL_BLOCK, *LPEXTENSION_CONTROL_BLOCK;

/*
 *  these are the prototypes that must be exported from the extension DLL
 */

MS_BOOL  __stdcall   GetExtensionVersion( HSE_VERSION_INFO  *pVer );
DWORD __stdcall   HttpExtensionProc(  EXTENSION_CONTROL_BLOCK *pECB );

/* the following type declarations is for the server side */

typedef MS_BOOL  (WINAPI * PFN_GETEXTENSIONVERSION)( HSE_VERSION_INFO  *pVer );
typedef DWORD (WINAPI * PFN_HTTPEXTENSIONPROC )( 
                                                EXTENSION_CONTROL_BLOCK *pECB );




#endif  /* end definition _HTTPEXT_H_ */


