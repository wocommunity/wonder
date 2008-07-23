/*

Copyright © 2000-2007 Apple, Inc. All Rights Reserved.

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
#ifndef _XMLCDEFINES_
#define _XMLCDEFINES_

/* For the WebObjects adaptor, use 8 bit characters. */
#define _XMLC_8BITCHAR_

#ifndef _XMLC_8BITCHAR_
typedef short unsigned int 	XMLCCharacter;
#else /* _XMLC_8BITCHAR_ */
typedef char 	XMLCCharacter;
#endif /* _XMLC_8BITCHAR_ */
typedef unsigned long 	XMLCUInt;
typedef unsigned long 	XMLCEncoding;
typedef unsigned char 	XMLCBoolean;
typedef unsigned long 	XMLCParseError;
typedef unsigned long 	XMLCToken;
typedef unsigned long 	XMLCTokenType;

#endif /* _XMLCDEFINES_ */
