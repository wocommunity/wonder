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
/* the following additional functionalities are specific to this WEB server implementation */

#define    HSE_GET_COUNTER_FOR_GET_METHOD            1001
#define    HSE_GET_COUNTER_FOR_POST_METHOD           1002
#define    HSE_GET_COUNTER_FOR_HEAD_METHOD           1003
#define    HSE_GET_COUNTER_FOR_ALL_METHODS           1004

/***
dwHSERequest  =    HSE_GET_COUNTER_FOR_GET_METHOD 
 lpvBuffer --- contains a null terminated string of the server name as returned by the CGI variable SERVER_NAME.
lpdwSize  --- size of the string pointed by lpvBuffer
 
lpdwDataType -- contains a DWORD of the total number of GET methods served by the server mentioned in lpvBuffer.
                           
dwHSERequest  =    HSE_GET_COUNTER_FOR_POST_METHOD 
        Exactly same as above except that this function returns the number of POST methods serviced by the server mentioned in lpvBuffer.

dwHSERequest  =    HSE_GET_COUNTER_FOR_HEAD_METHOD 
        Exactly same as above except that this function returns the number of HEAD methods serviced by the server mentioned in lpvBuffer.

dwHSERequest  =    HSE_GET_COUNTER_FOR_ALL_METHODS
        Exactly same as above except that this function returns the total number of  requests serviced by the server mentioned in lpvBuffer.

*****/
