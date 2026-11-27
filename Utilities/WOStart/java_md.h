/*
 * @(#)java_md.h	1.13 04/01/12
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#ifndef JAVA_MD_H
#define JAVA_MD_H

#include <jni.h>
#include <windows.h>
#include <io.h>
#include "manifest_info.h"

#define PATH_SEPARATOR	';'
#define FILESEP		"\\"
#define FILE_SEPARATOR	'\\'
#define MAXPATHLEN      MAX_PATH
#define MAXNAMELEN	MAX_PATH

#ifdef JAVA_ARGS
/*
 * ApplicationHome is prepended to each of these entries; the resulting
 * strings are concatenated (separated by PATH_SEPARATOR) and used as the
 * value of -cp option to the launcher.
 */
#ifndef APP_CLASSPATH
#define APP_CLASSPATH        { "\\lib\\tools.jar", "\\classes" }
#endif
#endif

/*
 * Support for doing cheap, accurate interval timing.
 */
extern jlong CounterGet(void);
extern jlong Counter2Micros(jlong counts);

#ifdef JAVAW
#define main _main
extern int _main(int argc, char **argv);
#endif

/*
 * Function prototypes.
 */
char *LocateJRE(manifest_info* info);
void ExecJRE(char *jre, char **argv);
int UnsetEnv(char *name);

/*
 * strcasecmp()/strncasecmp() are POSIX names that are not part of the
 * Windows API; msvcrt provides _stricmp()/_strnicmp() instead. mingw-w64
 * header already define strcasecmp/strncasecmp (as inline wrappers or as
 * #defines to _stricmp/_strnicmp); for toolchains that do not provide
 * them, alias the POSIX names to the msvcrt functions. Using #defines
 * here (instead of own wrapper functions) avoids redefining _stricmp at
 * link time on toolchains where strcasecmp is a #define itself.
 */
#ifndef strcasecmp
#define strcasecmp _stricmp
#endif
#ifndef strncasecmp
#define strncasecmp _strnicmp
#endif

#endif
