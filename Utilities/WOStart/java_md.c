/*
 * @(#)java_md.c	1.42 05/12/21
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


#include <windows.h>
#include <process.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <jni.h>
#include "java.h"
#include "version_comp.h"

#ifdef DEBUG
#define JVM_DLL "jvm_g.dll"
#define JAVA_DLL "java_g.dll"
#else
#define JVM_DLL "jvm.dll"
#define JAVA_DLL "java.dll"
#endif

/*
 * Prototypes.
 */
static jboolean GetPublicJREHome(char *path, jint pathsize);
static jboolean GetJVMPath(const char *jrepath, const char *jvmtype,
			   char *jvmpath, jint jvmpathsize);
static jboolean GetJREPath(char *path, jint pathsize, char *javaCommand);

const char *
GetArch()
{

#ifdef _M_AMD64
    return "amd64";
#elif defined(_M_IA64)
    return "ia64";
#else
    return "i386";
#endif
}

/*
 *
 */
void
CreateExecutionEnvironment(int *_argc,
			   char ***_argv,
			   char jrepath[],
			   jint so_jrepath,
			   char jvmpath[],
			   jint so_jvmpath,
			   char **original_argv,
			   char *javaCommand) {
   char * jvmtype;

    /* Find out where the JRE is that we will be using. */
    if (!GetJREPath(jrepath, so_jrepath, javaCommand)) {
	ReportErrorMessage("Error: could not find Java 2 Runtime Environment.",
			   JNI_TRUE);
	exit(2);
    }

    /* Find the specified JVM type */
    if (ReadKnownVMs(jrepath, (char*)GetArch(), JNI_FALSE) < 1) {
	ReportErrorMessage("Error: no known VMs. (check for corrupt jvm.cfg file)",
			   JNI_TRUE);
	exit(1);
    }
    jvmtype = CheckJvmType(_argc, _argv, JNI_FALSE);

    jvmpath[0] = '\0';
    if (!GetJVMPath(jrepath, jvmtype, jvmpath, so_jvmpath)) {
	char * message=NULL;
	const char * format = "Error: no `%s' JVM at `%s'.";
	message = (char *)MemAlloc((strlen(format)+strlen(jvmtype)+
				    strlen(jvmpath)) * sizeof(char));
	sprintf(message,format, jvmtype, jvmpath);
	ReportErrorMessage(message, JNI_TRUE);
	exit(4);
    }
    /* If we got here, jvmpath has been correctly initialized. */

}

/*
 * Find path to JRE based on .exe's location or registry settings.
 */
jboolean
GetJREPath(char *path, jint pathsize, char *javaCommand)
{
    char javadll[MAXPATHLEN];
    struct stat s;

    if (GetJREHome(path, pathsize, javaCommand)) {
	/* Is JRE co-located with the application? */
	sprintf(javadll, "%s\\bin\\" JAVA_DLL, path);
	if (stat(javadll, &s) == 0) {
	    goto found;
	}

	/* Does this app ship a private JRE in <apphome>\jre directory? */
	sprintf(javadll, "%s\\jre\\bin\\" JAVA_DLL, path);
	if (stat(javadll, &s) == 0) {
	    strcat(path, "\\jre");
	    goto found;
	}
    }

    /* Look for a public JRE on this machine. */
    if (GetPublicJREHome(path, pathsize)) {
	goto found;
    }

    fprintf(stderr, "Error: could not find " JAVA_DLL "\n");
    return JNI_FALSE;

 found:
    if (_launcher_debug)
      printf("JRE path is %s\n", path);
    return JNI_TRUE;
}

/*
 * Given a JRE location and a JVM type, construct what the name the
 * JVM shared library will be.  Return true, if such a library
 * exists, false otherwise.
 */
static jboolean
GetJVMPath(const char *jrepath, const char *jvmtype,
	   char *jvmpath, jint jvmpathsize)
{
    struct stat s;
    if (strchr(jvmtype, '/') || strchr(jvmtype, '\\')) {
	sprintf(jvmpath, "%s\\" JVM_DLL, jvmtype);
    } else {
	sprintf(jvmpath, "%s\\bin\\%s\\" JVM_DLL, jrepath, jvmtype);
    }
    if (stat(jvmpath, &s) == 0) {
	return JNI_TRUE;
    } else {
	return JNI_FALSE;
    }
}

/*
 * Load a jvm from "jvmpath" and initialize the invocation functions.
 */
jboolean
LoadJavaVM(const char *jvmpath, InvocationFunctions *ifn)
{
    HINSTANCE handle;

    if (_launcher_debug) {
	printf("JVM path is %s\n", jvmpath);
    }

    /* Load the Java VM DLL */
    if ((handle = LoadLibrary(jvmpath)) == 0) {
	ReportErrorMessage2("Error loading: %s", (char *)jvmpath, JNI_TRUE);
	return JNI_FALSE;
    }

    /* Now get the function addresses */
    ifn->CreateJavaVM =
	(void *)GetProcAddress(handle, "JNI_CreateJavaVM");
    ifn->GetDefaultJavaVMInitArgs =
	(void *)GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs");
    if (ifn->CreateJavaVM == 0 || ifn->GetDefaultJavaVMInitArgs == 0) {
	ReportErrorMessage2("Error: can't find JNI interfaces in: %s",
			    (char *)jvmpath, JNI_TRUE);
	return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Get the path to the file that has the usage message for -X options.
 */
void
GetXUsagePath(char *buf, jint bufsize)
{
    GetModuleFileName(GetModuleHandle(JVM_DLL), buf, bufsize);
    *(strrchr(buf, '\\')) = '\0';
    strcat(buf, "\\Xusage.txt");
}

/*
 * javaCommand is separate parameter, supply a path to java.exe
 * If javaCommand is "c:\foo\bin\java", then put "c:\foo" into buf.
 */
jboolean
GetJREHome(char *buf, jint bufsize, char *javaCommand)
{
    char *cp;
	
	if(javaCommand == NULL)
		return JNI_FALSE;

	strcpy(buf, javaCommand);
	if(strrchr(buf, '\\') == 0)
		return JNI_FALSE;
	
    *strrchr(buf, '\\') = '\0'; /* remove .exe file name */

    if ((cp = strrchr(buf, '\\')) == 0) {
		/* This happens if the application is in a drive root, and
		 * there is no bin directory. */
		buf[0] = '\0';
		return JNI_FALSE;
    }

    *cp = '\0';  /* remove the bin\ part */
    return JNI_TRUE;
}

#ifdef JAVAW
__declspec(dllimport) char **__initenv;

int WINAPI
WinMain(HINSTANCE inst, HINSTANCE previnst, LPSTR cmdline, int cmdshow)
{
    int   ret;

    __initenv = _environ;
    ret = main(__argc, __argv);

    return ret;
}
#endif

/*
 * Helpers to look in the registry for a public JRE.
 */
		    /* Same for 1.5.0, 1.5.1, 1.5.2 etc. */
#define DOTRELEASE5  "1.5"
#define DOTRELEASE6  "1.6"
#define DOTRELEASE7  "1.7"
#define DOTRELEASE8  "1.8"
#define JRE_KEY	    "Software\\JavaSoft\\Java Runtime Environment"

static jboolean
GetStringFromRegistry(HKEY key, const char *name, char *buf, jint bufsize)
{
    DWORD type, size;

    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0
	&& type == REG_SZ
	&& (size < (unsigned int)bufsize)) {
	if (RegQueryValueEx(key, name, 0, 0, buf, &size) == 0) {
	    return JNI_TRUE;
	}
    }
    return JNI_FALSE;
}

static jboolean
GetPublicJREHome(char *buf, jint bufsize)
{
    HKEY key, subkey;
    char version[MAXPATHLEN];

    /* Find the current version of the JRE */
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_KEY, 0, KEY_READ, &key) != 0) {
	fprintf(stderr, "Error opening registry key '" JRE_KEY "'\n");
	return JNI_FALSE;
    }

    if (!GetStringFromRegistry(key, "CurrentVersion",
			       version, sizeof(version))) {
	fprintf(stderr, "Failed reading value of registry key:\n\t"
		JRE_KEY "\\CurrentVersion\n");
	RegCloseKey(key);
	return JNI_FALSE;
    }

    if (strcmp(version, DOTRELEASE5) != 0 &&
		strcmp(version, DOTRELEASE6) != 0 &&
		strcmp(version, DOTRELEASE7) != 0 &&
		strcmp(version, DOTRELEASE8) != 0) 
	{
	fprintf(stderr, "Registry key '" JRE_KEY "\\CurrentVersion'\nhas "
		"value '%s', but '" DOTRELEASE5 "' upto '" DOTRELEASE8 "' is required.\n", version);
	RegCloseKey(key);
	return JNI_FALSE;
    }

    /* Find directory where the current version is installed. */
    if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0) {
	fprintf(stderr, "Error opening registry key '"
		JRE_KEY "\\%s'\n", version);
	RegCloseKey(key);
	return JNI_FALSE;
    }

    if (!GetStringFromRegistry(subkey, "JavaHome", buf, bufsize)) {
	fprintf(stderr, "Failed reading value of registry key:\n\t"
		JRE_KEY "\\%s\\JavaHome\n", version);
	RegCloseKey(key);
	RegCloseKey(subkey);
	return JNI_FALSE;
    }

    if (_launcher_debug) {
	char micro[MAXPATHLEN];
	if (!GetStringFromRegistry(subkey, "MicroVersion", micro,
				   sizeof(micro))) {
	    printf("Warning: Can't read MicroVersion\n");
	    micro[0] = '\0';
	}
	printf("Version major.minor.micro = %s.%s\n", version, micro);
    }

    RegCloseKey(key);
    RegCloseKey(subkey);
    return JNI_TRUE;
}

/*
 * Support for doing cheap, accurate interval timing.
 */
static jboolean counterAvailable = JNI_FALSE;
static jboolean counterInitialized = JNI_FALSE;
static LARGE_INTEGER counterFrequency;

jlong CounterGet()
{
    LARGE_INTEGER count;

    if (!counterInitialized) {
	counterAvailable = QueryPerformanceFrequency(&counterFrequency);
	counterInitialized = JNI_TRUE;
    }
    if (!counterAvailable) {
	return 0;
    }
    QueryPerformanceCounter(&count);
    return (jlong)(count.QuadPart);
}

jlong Counter2Micros(jlong counts)
{
    if (!counterAvailable || !counterInitialized) {
	return 0;
    }
    return (counts * 1000 * 1000)/counterFrequency.QuadPart;
}

void ReportErrorMessage(char * message, jboolean always) {
#ifdef JAVAW
  if (message != NULL) {
    MessageBox(NULL, message, "Java Virtual Machine Launcher",
	       (MB_OK|MB_ICONSTOP|MB_APPLMODAL));
  }
#else
  if (always) {
    fprintf(stderr, "%s\n", message);
  }
#endif
}

void ReportErrorMessage2(char * format, char * string, jboolean always) {
  /*
   * The format argument must be a printf format string with one %s
   * argument, which is passed the string argument.
   */
#ifdef JAVAW
  size_t size;
  char * message;
  size = strlen(format) + strlen(string);
  message = (char*)MemAlloc(size*sizeof(char));
  sprintf(message, (const char *)format, string);
 
  if (message != NULL) {
    MessageBox(NULL, message, "Java Virtual Machine Launcher",
	       (MB_OK|MB_ICONSTOP|MB_APPLMODAL));
  }
#else
  if (always) {
    fprintf(stderr, (const char *)format, string);
    fprintf(stderr, "\n");
  }
#endif
}

void  ReportExceptionDescription(JNIEnv * env) {
#ifdef JAVAW
  /*
   * This code should be replaced by code which opens a window with
   * the exception detail message.
   */
  (*env)->ExceptionDescribe(env);
#else
  (*env)->ExceptionDescribe(env);
#endif
}


/*
 * Return JNI_TRUE for an option string that has no effect but should
 * _not_ be passed on to the vm; return JNI_FALSE otherwise. On
 * windows, there are no options that should be screened in this
 * manner.
 */
jboolean RemovableMachineDependentOption(char * option) {
  return JNI_FALSE;
}

void PrintMachineDependentOptions() {
  return;
}

jboolean
ServerClassMachine() {
  jboolean result = JNI_FALSE;
  return result;
}

/*
 * Determine if there is an acceptable JRE in the registry directory top_key.
 * Upon locating the "best" one, return a fully qualified path to it.
 * "Best" is defined as the most advanced JRE meeting the constraints
 * contained in the manifest_info. If no JRE in this directory meets the
 * constraints, return NULL.
 *
 * It doesn't matter if we get an error reading the registry, or we just
 * don't find anything interesting in the directory.  We just return NULL
 * in either case.
 */
static char *
ProcessDir(manifest_info* info, HKEY top_key) {
    DWORD   index = 0;
    HKEY    ver_key;
    char    name[MAXNAMELEN];
    int	    len;
    char    *best = NULL;

    /*
     * Enumerate "<top_key>/SOFTWARE/JavaSoft/Java Runtime Environment"
     * searching for the best available version.
     */
    while (RegEnumKey(top_key, index, name, MAXNAMELEN) == ERROR_SUCCESS) {  
	index++;
	if (acceptable_release(name, info->jre_version))
	    if ((best == NULL) || (exact_version_id(name, best) > 0)) {
		if (best != NULL)
		    free(best);
		best = strdup(name);
	    }
    }

    /*
     * Extract "JavaHome" from the "best" registry directory and return
     * that path.  If no appropriate version was located, or there is an
     * error in extracting the "JavaHome" string, return null.
     */
    if (best == NULL)
	return (NULL);
    else {
	if (RegOpenKeyEx(top_key, best, 0, KEY_READ, &ver_key)
	  != ERROR_SUCCESS) {
	    free(best);
	    if (ver_key != NULL)
		RegCloseKey(ver_key);
	    return (NULL);
	}
	free(best);
	len = MAXNAMELEN;
	if (RegQueryValueEx(ver_key, "JavaHome", NULL, NULL, (LPBYTE)name, &len)
	  != ERROR_SUCCESS) {
	    if (ver_key != NULL)
		RegCloseKey(ver_key);
	    return (NULL);
	}
	if (ver_key != NULL)
	    RegCloseKey(ver_key);
	return (strdup(name));
    }
}

/*
 * This is the global entry point. It examines the host for the optimal
 * JRE to be used by scanning a set of registry entries.  This set of entries
 * is hardwired on Windows as "Software\JavaSoft\Java Runtime Environment"
 * under the set of roots "{ HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE }".
 * 
 * This routine simply opens each of these registry directories before passing
 * control onto ProcessDir().
 */
char *
LocateJRE(manifest_info* info) {
    HKEY    key = NULL;
    char    *path;
    int	    key_index;
    HKEY    root_keys[2] = { HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE };

    for (key_index = 0; key_index <= 1; key_index++) {
	if (RegOpenKeyEx(root_keys[key_index], JRE_KEY, 0, KEY_READ, &key)
	  == ERROR_SUCCESS)
	    if ((path = ProcessDir(info, key)) != NULL) {
		if (key != NULL)
		    RegCloseKey(key);
		return (path);
	    }
	if (key != NULL)
	    RegCloseKey(key);
    }
    return NULL;
}

/*
 * Local helper routine to isolate a single token (option or argument)
 * from the command line.
 *
 * This routine accepts a pointer to a character pointer.  The first
 * token (as defined by MSDN command-line argument syntax) is isolated
 * from that string.
 *
 * Upon return, the the input character pointer pointed to by the parameter s
 * is updated to point to the remainding, unscanned, portion of the string,
 * or to a null character if the entire string has been consummed.
 *
 * This function returns a pointer to a null-terminated string which
 * contains the isolated first token, or to the null character if no
 * token could be isolated.
 *
 * Note the side effect of modifying the input string s by the insertion
 * of a null character, making it two strings.
 *
 * See "Parsing C Command-Line Arguments" in the MSDN Library for the
 * parsing rule details.  The rule summary from that specification is:
 *
 *  * Arguments are delimited by white space, which is either a space or a tab.
 * 
 *  * A string surrounded by double quotation marks is interpreted as a single
 *    argument, regardless of white space contained within. A quoted string can
 *    be embedded in an argument. Note that the caret (^) is not recognized as
 *    an escape character or delimiter.
 *   
 *  * A double quotation mark preceded by a backslash, \", is interpreted as a
 *    literal double quotation mark (").
 *   
 *  * Backslashes are interpreted literally, unless they immediately precede a
 *    double quotation mark.
 *   
 *  * If an even number of backslashes is followed by a double quotation mark,
 *    then one backslash (\) is placed in the argv array for every pair of
 *    backslashes (\\), and the double quotation mark (") is interpreted as a
 *    string delimiter.
 *   
 *  * If an odd number of backslashes is followed by a double quotation mark,
 *    then one backslash (\) is placed in the argv array for every pair of
 *    backslashes (\\) and the double quotation mark is interpreted as an
 *    escape sequence by the remaining backslash, causing a literal double
 *    quotation mark (") to be placed in argv.
 */
static char*
nextarg(char** s) {
    char    *p = *s;
    char    *head;
    int     slashes = 0;
    int     inquote = 0;

    /*
     * Strip leading whitespace, which MSDN defines as only space or tab.
     * (Hence, no locale specific "isspace" here.)
     */
    while (*p != (char)0 && (*p == ' ' || *p == '\t'))
	p++;
    head = p;                   /* Save the start of the token to return */

    /*
     * Isolate a token from the command line.
     */
    while (*p != (char)0 && (inquote || !(*p == ' ' || *p == '\t'))) {
	if (*p == '\\' && *(p+1) == '"' && slashes % 2 == 0)
	    p++;
	else if (*p == '"')
	    inquote = !inquote;
	slashes = (*p++ == '\\') ? slashes + 1 : 0;
    }

    /*
     * If the token isolated isn't already terminated in a "char zero",
     * then replace the whitespace character with one and move to the
     * next character.
     */
    if (*p != (char)0)
	*p++ = (char)0;

    /*
     * Update the parameter to point to the head of the remaining string
     * reflecting the command line and return a pointer to the leading
     * token which was isolated from the command line.
     */
    *s = p;
    return (head);
}

/*
 * Local helper routine to return a string equivalent to the input string
 * s, but with quotes removed so the result is a string as would be found
 * in argv[].  The returned string should be freed by a call to free().
 *
 * The rules for quoting (and escaped quotes) are:
 *
 *  1 A double quotation mark preceded by a backslash, \", is interpreted as a
 *    literal double quotation mark (").
 *
 *  2 Backslashes are interpreted literally, unless they immediately precede a
 *    double quotation mark.
 *
 *  3 If an even number of backslashes is followed by a double quotation mark,
 *    then one backslash (\) is placed in the argv array for every pair of
 *    backslashes (\\), and the double quotation mark (") is interpreted as a
 *    string delimiter.
 *
 *  4 If an odd number of backslashes is followed by a double quotation mark,
 *    then one backslash (\) is placed in the argv array for every pair of
 *    backslashes (\\) and the double quotation mark is interpreted as an
 *    escape sequence by the remaining backslash, causing a literal double
 *    quotation mark (") to be placed in argv.
 */
static char*
unquote(const char *s) {
    const char *p = s;          /* Pointer to the tail of the original string */
    char *un = (char*)MemAlloc(strlen(s) + 1);  /* Pointer to unquoted string */
    char *pun = un;             /* Pointer to the tail of the unquoted string */

    while (*p != '\0') {
	if (*p == '"') {
	    p++;
	} else if (*p == '\\') {
	    const char *q = p + strspn(p,"\\");
	    if (*q == '"')
		do {
		    *pun++ = '\\';
		    p += 2;
		 } while (*p == '\\' && p < q);
	    else
		while (p < q)
		    *pun++ = *p++;
	} else {
	    *pun++ = *p++;
	}
    }
    *pun = '\0';
    return un;
}

/*
 * Given a path to a jre to execute, this routine checks if this process
 * is indeed that jre.  If not, it exec's that jre.
 *
 * We want to actually check the paths rather than just the version string
 * built into the executable, so that given version specification will yield
 * the exact same Java environment, regardless of the version of the arbitrary
 * launcher we start with.
 */
void
ExecJRE(char *jre, char **argv) {
    int     len;
    char    *progname, *s;
    char    path[MAXPATHLEN + 1];

    /*
     * Determine the executable we are building (or in the rare case, running).
     */
#ifdef JAVA_ARGS  /* javac, jar and friends. */
    progname = "java";
#else             /* java, oldjava, javaw and friends */
#ifdef PROGNAME
    progname = PROGNAME;
#else
    progname = *argv;
    if ((s = strrchr(progname, FILE_SEPARATOR)) != 0) {
	progname = s + 1;
    }
#endif /* PROGNAME */
#endif /* JAVA_ARGS */

    /*
     * Resolve the real path to the currently running launcher.
     */
    len = GetModuleFileName(NULL, path, MAXPATHLEN + 1);
    if (len == 0 || len > MAXPATHLEN) {
	ReportErrorMessage2(
	"Unable to resolve path to current %s executable.",
	progname, JNI_TRUE);
	exit(1);
    }

    if (_launcher_debug) {
	printf("ExecJRE: old: %s\n", path);
	printf("ExecJRE: new: %s\n", jre);
    }

    /*
     * If the path to the selected JRE directory is a match to the initial
     * portion of the path to the currently executing JRE, we have a winner!
     * If so, just return. (strnicmp() is the Windows equiv. of strncasecmp().)
     */
    if (strnicmp(jre, path, strlen(jre)) == 0)
	return;                 /* I am the droid you were looking for */

    /*
     * If this isn't the selected version, exec the selected version.
     */
    (void)strcat(strcat(strcpy(path, jre), "\\bin\\"), progname);
    (void)strcat(path, ".exe");

    /*
     * Although Windows has an execv() entrypoint, it doesn't actually
     * overlay a process: it can only create a new process and terminate
     * the old process.  Therefore, any processes waiting on the initial
     * process wake up and they shouldn't.  Hence, a chain of pseudo-zombie
     * processes must be retained to maintain the proper wait semantics.
     * Fortunately the image size of the launcher isn't too large at this
     * time.
     *
     * If it weren't for this semantic flaw, the code below would be ...
     *
     *     execv(path, argv);
     *     ReportErrorMessage2("Exec of %s failed\n", path, JNI_TRUE);
     *     exit(1);
     *
     * The incorrect exec semantics could be addressed by:
     *
     *     exit((int)spawnv(_P_WAIT, path, argv));
     *
     * Unfortunately, a bug in Windows spawn/exec impementation prevents
     * this from completely working.  All the Windows POSIX process creation
     * interfaces are implemented as wrappers around the native Windows
     * function CreateProcess().  CreateProcess() takes a single string
     * to specify command line options and arguments, so the POSIX routine
     * wrappers build a single string from the argv[] array and in the
     * process, any quoting information is lost.
     *
     * The solution to this to get the original command line, to process it
     * to remove the new multiple JRE options (if any) as was done for argv
     * in the common SelectVersion() routine and finally to pass it directly
     * to the native CreateProcess() Windows process control interface.
     */
    {
	char    *cmdline;
	char    *p;
	char    *np;
	char    *ocl;
	char    *ccl;
	char    *unquoted;
	BOOL    ret;
	DWORD   exitCode;
	STARTUPINFO si;
	PROCESS_INFORMATION pi;

	/*
	 * The following code block gets and processes the original command
	 * line, replacing the argv[0] equivalent in the command line with
	 * the path to the new executable and removing the appropriate
	 * Multiple JRE support options. Note that similar logic exists
	 * in the platform independent SelectVersion routine, but is
	 * replicated here due to the syntax of CreateProcess().
	 *
	 * The magic "+ 4" characters added to the command line length are
	 * 2 possible quotes around the path (argv[0]), a space after the
	 * path and a terminating null character.
	 */
	ocl = GetCommandLine();
	np = ccl = strdup(ocl);
	p = nextarg(&np);           /* Discard argv[0] */
	cmdline = (char *)MemAlloc(strlen(path) + strlen(np) + 4);
	if (strchr(path, (int)' ') == NULL && strchr(path, (int)'\t') == NULL)
	    cmdline = strcpy(cmdline, path);
	else
	    cmdline = strcat(strcat(strcpy(cmdline, "\""), path), "\"");

	while (*np != (char)0) {                /* While more command-line */
	    p = nextarg(&np);
	    if (*p != (char)0) {                /* If a token was isolated */
		unquoted = unquote(p);
		if (*unquoted == '-') {         /* Looks like an option */
		    if (strcmp(unquoted, "-classpath") == 0 ||
		      strcmp(unquoted, "-cp") == 0) {   /* Unique cp syntax */
			cmdline = strcat(strcat(cmdline, " "), p);
			p = nextarg(&np);
			if (*p != (char)0)      /* If a token was isolated */
			    cmdline = strcat(strcat(cmdline, " "), p);
		    } else if (strncmp(unquoted, "-version:", 9) != 0 &&
		      strcmp(unquoted, "-jre-restrict-search") != 0 &&
		      strcmp(unquoted, "-no-jre-restrict-search") != 0) {
			cmdline = strcat(strcat(cmdline, " "), p);
		    }
		} else {                        /* End of options */
		    cmdline = strcat(strcat(cmdline, " "), p);
		    cmdline = strcat(strcat(cmdline, " "), np);
		    free((void *)unquoted);
		    break;
		}
		free((void *)unquoted);
	    }
	}
	free((void *)ccl);

	if (_launcher_debug) {
	    np = ccl = strdup(cmdline);
	    p = nextarg(&np);
	    printf("ReExec Command: %s (%s)\n", path, p);
	    printf("ReExec Args: %s\n", np);
	    free((void *)ccl);
	}
	(void)fflush(stdout);
	(void)fflush(stderr);

	/*
	 * The following code is modeled after a model presented in the
	 * Microsoft Technical Article "Moving Unix Applications to
	 * Windows NT" (March 6, 1994) and "Creating Processes" on MSDN
	 * (Februrary 2005).  It approximates UNIX spawn semantics with
	 * the parent waiting for termination of the child.
	 */
	memset(&si, 0, sizeof(si));
	si.cb =sizeof(STARTUPINFO);
	memset(&pi, 0, sizeof(pi));

	if (!CreateProcess((LPCTSTR)path,       /* executable name */
	  (LPTSTR)cmdline,                      /* command line */
	  (LPSECURITY_ATTRIBUTES)NULL,          /* process security attr. */
	  (LPSECURITY_ATTRIBUTES)NULL,          /* thread security attr. */
	  (BOOL)TRUE,                           /* inherits system handles */
	  (DWORD)0,                             /* creation flags */
	  (LPVOID)NULL,                         /* environment block */
	  (LPCTSTR)NULL,                        /* current directory */
	  (LPSTARTUPINFO)&si,                   /* (in) startup information */
	  (LPPROCESS_INFORMATION)&pi)) {        /* (out) process information */
	    ReportErrorMessage2("CreateProcess(%s, ...) failed",
	      path, JNI_TRUE);
	      exit(1);
	}

	if (WaitForSingleObject(pi.hProcess, INFINITE) != WAIT_FAILED) {
	    if (GetExitCodeProcess(pi.hProcess, &exitCode) == FALSE)
		exitCode = 1;
	} else {
	    ReportErrorMessage("WaitForSingleObject() failed.", JNI_TRUE);
	    exitCode = 1;
	}

	CloseHandle(pi.hThread);
	CloseHandle(pi.hProcess);

	exit(exitCode);
    }

}

/*
 * Wrapper for platform dependent unsetenv function.
 */
int
UnsetEnv(char *name)
{
    int ret;
    char *buf = MemAlloc(strlen(name) + 2);
    buf = strcat(strcpy(buf, name), "=");
    ret = _putenv(buf);
    free(buf);
    return (ret);
}

/*
 * The following just map the common UNIX name to the Windows API name,
 * so that the common UNIX name can be used in shared code.
 */
int
strcasecmp(const char *s1, const char *s2)
{
    return (stricmp(s1, s2));
}

int
strncasecmp(const char *s1, const char *s2, size_t n)
{
    return (strnicmp(s1, s2, n));
}
