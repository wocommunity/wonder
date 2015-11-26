#include "stdafx.h"
#include <sys/stat.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif


using namespace std;

string getNextRoot();
string getNextLocal();

#define APPNAME "WOStart"
#define VERSION "1.1"

extern "C" {
  int Java_Main(int argc, char ** argv, char *javaCommand);
}

// -- Application Environment --------------------------------------------

string g_currentDirectory;

// Path to the application Contents directory
string g_appRoot;

// Path to WebObjects installation
string g_WORoot;

// Path to WebObjects Local directory
string g_localRoot;

// Path to Home Root (what does this mean in this context?)
string g_homeRoot("C:\\");

// Application Class
string g_appClass("Application");

// JVM command
string g_jvmCommand("java");

// JVM options
string g_jvmOptions("");

static bool debugoutput = false;

// Report fatal error and exit
//
void fatalError(char *message) {
	cerr << endl << "Fatal Error: " << message << endl;
	exit(1);
}


// Gets the pathname of the directory containing the currently executing
// application.
//
string getAppDirectory()
{ 
	char buf[_MAX_PATH];
	string bs = "\\";
	static string path;
	// get the filename of the current excutable
	if (GetModuleFileName(NULL, buf, _MAX_PATH)) { 
		path.assign(buf);
		
		size_t found = path.rfind(bs);
		if(found != string::npos) {
			string substr = path.substr(0, found);
			path.assign(substr);
		}
	}

	return path;
}

void findandreplace( string & source, const string& find, const string& replace );

void normalisePath(string &path) {
	// swap separator style
	findandreplace(path, "/", "\\");

	// remove trailing seperator
	if (path.length() > 0 &&
		  path.at(path.length()-1) == '\\')
		path.replace(path.length()-1, 1, "");
}

void findandreplace( string & source, const string& find, const string& replace )
{
	size_t j;
	for (;(j = source.find( find )) != string::npos;)
	{
		source.replace( j, find.length(), replace );
	}
}


// Gets the NEXT_ROOT directory
//
string getNextRoot() {
	static string root;
	static bool    initialised = false;

	if (!initialised) {
		// flag that we've tried
		initialised = true;

		HKEY   hKey;

		// try retrieving the WO base installation directory
		if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,
						 "SOFTWARE\\Apple\\WebObjects",
						 0, KEY_QUERY_VALUE, &hKey) == ERROR_SUCCESS) 
		{
	  		DWORD  bufLen = _MAX_PATH;
			char buf[_MAX_PATH];
			buf[0] = 0;

			RegQueryValueEx(hKey, "NEXT_ROOT", NULL, NULL,
							(LPBYTE) buf, &bufLen);
            RegCloseKey(hKey);
			root.assign(buf);
			normalisePath(root);
		}
		
		if (root.length() > 0) {
			if (debugoutput) {
				cout << "WORoot found in Registry: " << root.c_str() << endl;
			}
			return root;
		}

//		cerr << "Warning: Unable to locate WOROOT/NEXT_ROOT using registry" << endl;


		//
		// ok, can't find it in the registry so hunt for the apple directory
		// in the local drives
		//

		// get a bit mask enumerating the local drives
		DWORD   drive_mask = GetLogicalDrives();
		char    letter = L'A';
		string drive  = "A:\\";


		while (drive_mask != 0) {

			// get the drive root
			drive.replace(0,1,1,letter++);

			// skip missing letters
			boolean valid_drive = drive_mask & 1;
			drive_mask >>= 1;
			if (!valid_drive) continue;

			// find out the type
			UINT drive_type = GetDriveType(drive.c_str());

			// skip any drive that isn't fixed or remote (cdroms, removable storage, etc)
			if (drive_type != DRIVE_FIXED && drive_type != DRIVE_REMOTE) continue;

			
			// try looking for the Apple root directory on this drive
			root = drive; 
			root += "Apple";

			cerr << "Looking for WOROOT in " << root.c_str() << endl;


			DWORD fattr = GetFileAttributes(root.c_str());
			if (fattr != INVALID_FILE_ATTRIBUTES &&
				fattr & FILE_ATTRIBUTE_DIRECTORY) {
					if (debugoutput) {
						cout << "WORoot found on drive: " << root.c_str() << endl;
					}
				
					return root;
			}

		}

//		fatalError("Unable to locate WOROOT/NEXT_ROOT - is WebObjects installed?");
//		new default: empty string as original .cmd script does
		root = "";
	}

	return root;
}


string getNextLocal() 
{
	static string local(getNextRoot());

	local += "\\Local";

	return local;
}

std::string StringTostring(const std::string& s)
{
	std::string temp(s.length(),L' ');
	std::copy(s.begin(), s.end(), temp.begin());
	return temp;
}

/*
std::string stringToString(const std::string& s)
{
	std::string temp(s.length(), ' ');
	std::copy(s.begin(), s.end(), temp.begin());
	return temp;
}*/

void setLaunchProperty(string key, string value) {
	cout << " setLaunchProperty " << key << "=" << value << endl;
	if (key.compare("JVM") == 0) {
		g_jvmCommand = value;
	} else if (key.compare("JVMOptions") == 0) {
		g_jvmOptions = value;
	} else if (key.compare("ApplicationClass") == 0) {
		g_appClass = value;
	}
}


void expandPath(string& path) {
	findandreplace(path, "WOROOT",    g_WORoot);
	findandreplace(path, "LOCALROOT", g_localRoot);
	findandreplace(path, "APPROOT",   g_appRoot);
	findandreplace(path, "HOMEROOT",  g_homeRoot);
}

// -- Launch command creation --------------------------------------------

void trim(string& str)
{
  string::size_type pos = str.find_last_not_of(L' ');
  if(pos != string::npos) {
    str.erase(pos + 1);
    pos = str.find_first_not_of(L' ');
    if(pos != string::npos) str.erase(0, pos);
  }
  else str.erase(str.begin(), str.end());
}

void trimQuotes(string& str)
{
  if(str[0] == '"' && str[str.length()-1] == '"')
  {
    str.erase(0, 1);
    str.erase(str.length()-1, str.length());
  }
}

void addToClassPath(string& classpath, vector<string>list)
{
	for(int i=0; i<list.size(); i++) {
		string t = list[i];

		// Expand the path prefixes
		expandPath(t);
		normalisePath(t);

		if (!classpath.empty()) classpath += ";";
			classpath += t;

		cout << " " << t << endl;
	}
}

// Build the classpath from the platform classpath file
//
void initialiseClassPath(string& classpath)
{ 
	string filename(g_appRoot);
	filename += "\\Windows\\CLSSPATH.TXT";
	ifstream ifs(filename.c_str());
	string line;
/*	vector<string> appRootList;
	vector<string> woRootList;
	vector<string> localRootList;
	vector<string> homeRootList;
*/
	vector<string> cpList;

	while(getline(ifs,line))
	{
		string wline = StringTostring(line);
		trim(wline);

		// is it a comment?
		if (wline.length() > 0 && wline.at(0) == L'#') {
			// yes - ok, does it define a launcher property?
			int i = wline.find(" == ");
			if (i != string::npos) {
				string key = wline.substr(1, i - 1);
				trim(key);
				
				string value = wline.substr(i + 4);
				trim(value);
			   	trimQuotes(value);
				
				setLaunchProperty(key, value);
			}

			continue;
		}

		cpList.push_back(wline);
/*
		if(wline.length() > 0) {
			if(wline.find("APPROOT") != string::npos)
				appRootList.push_back(wline);
			if(wline.find("WOROOT") != string::npos)
				woRootList.push_back(wline);
			if(wline.find("LOCALROOT") != string::npos)
				localRootList.push_back(wline);
			if(wline.find("HOMEROOT") != string::npos)
				homeRootList.push_back(wline);
		}*/
	}

	cout << "Construct classpath from CLSSPATH.TXT" << endl;

/*	addToClassPath(classpath, appRootList);
	addToClassPath(classpath, homeRootList);
	addToClassPath(classpath, localRootList);
	addToClassPath(classpath, woRootList);*/
	addToClassPath(classpath, cpList);
}



// Get the property declarations for the various WO Paths
//
void getDirectoryProperties(vector<string>& jargs) {
	string args;

	args = "-DWORootDirectory=";
	args += g_WORoot;
	jargs.push_back(args);

	args = "-DWOLocalRootDirectory=";
	args += g_localRoot;
	jargs.push_back(args);

	args = "-DWOUserDirectory=";
	args += g_currentDirectory;
	jargs.push_back(args);
}

void StringSplit(string str, string delim, vector<string>& results)
{
	int cutAt;
	while( (cutAt = str.find_first_of(delim)) != str.npos )
	{
		if(cutAt > 0)
		{
			results.push_back(str.substr(0,cutAt));
		}
		str = str.substr(cutAt+1);
	}
	if(str.length() > 0)
	{
		results.push_back(str);
	}
}

void buildCommand(vector<string>& jargs, string& classpath, int argc, TCHAR* argv[]) {

	getDirectoryProperties(jargs);

	if(g_jvmOptions.length() > 0) {
//		cout << "JVMOptions " << g_jvmOptions << endl;

		vector<string> jvmOList;
		StringSplit(g_jvmOptions, " ", jvmOList);
		for(int i=0; i<jvmOList.size(); i++) {
			string t = jvmOList[i];
			trim(t);
			jargs.push_back(t);
		}
	}

	vector<string> appArgs;

	// process on the args - skipping arg 0 (command name)
	for (int i = 1; i < argc; i++) {
		TCHAR *arg = argv[i];

		// special handing jvm arguments
		if (arg[0] == '-' && (			
			   (arg[1] == 'X') ||                      // jvm "special" arguments
			   (arg[1] == 'D' && strchr(arg + 2, '=')) // property definitions
			))
		{
			jargs.push_back(argv[i]);
			continue;
		}
		
		appArgs.push_back(argv[i]);
	}

	jargs.push_back("-cp");
	jargs.push_back(classpath);
	jargs.push_back(g_appClass);

	int ii;
	for(ii=0; ii < appArgs.size(); ii++)
	{
		jargs.push_back(appArgs[ii]);
	}	
}

void checkForUpdate(string& appPath) {
    string lockFile = appPath + "\\updateLock";
    
    string contentsNew = appPath + "\\ContentsNew";
    struct _stat statBuf;

    if(_stat(contentsNew.c_str(), &statBuf) == 0)
    {
    	// ContentsNew is there for renaming
    	string contentsOld = appPath + "\\ContentsOld";
	    if(_stat(contentsOld.c_str(), &statBuf) == 0)
	    {
	        // ContentsOld is there, too -> nothing to do
	        return;
	    }
    	
    	string contents = appPath + "\\Contents";
    	// rename Contents -> ContentsOld
    	rename(contents.c_str(), contentsOld.c_str());
    	
    	// rename ContentsNew -> Contents
    	rename(contentsNew.c_str(), contents.c_str());
    } 
}

int wostart_main(int argc, TCHAR* argv[])
{
	try {
		// tell the world about ourselves;
		cout << APPNAME << " " << VERSION << endl;

		if (getenv("_JAVA_LAUNCHER_DEBUG") != 0) {
			debugoutput = true;
		}

		g_WORoot = getNextRoot();
		g_localRoot = getNextLocal();

		
		// where are we being launched from?
		g_currentDirectory = getAppDirectory();

        checkForUpdate(g_currentDirectory);
        
		normalisePath(g_currentDirectory);
		g_appRoot.assign(g_currentDirectory);
		g_appRoot += "\\Contents";

		// get the classpath
		string classpath;
		initialiseClassPath(classpath);

		vector<string> jargs;
		buildCommand(jargs, classpath, argc, argv);

		int js = jargs.size();
		char *new_argv[1000];

		//
		// remove suffixes from command (remove .32.exe, .64.exe, .exe)
		//
		cout << "Command" << endl;
		string command = argv[0];
		size_t found = command.find('.');
		if(found != string::npos) {
			string substr = command.substr(0, found);
			command.assign(substr);
		}

		new_argv[0] = (char *)command.c_str();
		cout << " " << new_argv[0] << endl;
		
		cout << "Arguments" << endl;
		int ii;
		for(ii=0; ii < js; ii++)
		{
			cout << " " << jargs[ii] << endl;
			new_argv[ii+1] = (char *)jargs[ii].c_str();
		}

		// set the current directory
		SetCurrentDirectory(g_currentDirectory.c_str());

		Java_Main(js+1, new_argv, (char *)g_jvmCommand.c_str());
	} 
	catch (exception *e) 
	{
		cerr << "WOLancher exception thrown:\n" << e->what();

		return 1; // failure
	}

	return 0; // ok!
}

SERVICE_STATUS_HANDLE statusHandle;

void SetStatus(DWORD state)
{
    SERVICE_STATUS status = {SERVICE_WIN32_OWN_PROCESS, state, SERVICE_ACCEPT_STOP, NO_ERROR, 0, 0, 0};

    // Set allowed commands and wait time
    if ((state == SERVICE_START_PENDING) || (state == SERVICE_STOP_PENDING))
    {
        status.dwControlsAccepted = 0;
        status.dwWaitHint = 2000;
    }

    // Set status
    SetServiceStatus(statusHandle, &status);
}

void WINAPI ControlHandler(DWORD control)
{
    if (control == SERVICE_CONTROL_STOP)
    {
        SetStatus(SERVICE_STOPPED);
    }
}

PCHAR*
CommandLineToArgvA(
	PCHAR CmdLine,
	int* _argc
	)
{
	PCHAR* argv;
	PCHAR  _argv;
	ULONG   len;
	ULONG   argc;
	CHAR   a;
	ULONG   i, j;

	BOOLEAN  in_QM;
	BOOLEAN  in_TEXT;
	BOOLEAN  in_SPACE;

	len = strlen(CmdLine);
	i = ((len+2)/2)*sizeof(PVOID) + sizeof(PVOID);

	argv = (PCHAR*)GlobalAlloc(GMEM_FIXED,
		i + (len+2)*sizeof(CHAR));

	_argv = (PCHAR)(((PUCHAR)argv)+i);

	argc = 0;
	argv[argc] = _argv;
	in_QM = FALSE;
	in_TEXT = FALSE;
	in_SPACE = TRUE;
	i = 0;
	j = 0;

	while( a = CmdLine[i] ) {
		if(in_QM) {
			if(a == '\"') {
				in_QM = FALSE;
			} else {
				_argv[j] = a;
				j++;
			}
		} else {
			switch(a) {
			case '\"':
				in_QM = TRUE;
				in_TEXT = TRUE;
				if(in_SPACE) {
					argv[argc] = _argv+j;
					argc++;
				}
				in_SPACE = FALSE;
				break;
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				if(in_TEXT) {
					_argv[j] = '\0';
					j++;
				}
				in_TEXT = FALSE;
				in_SPACE = TRUE;
				break;
			default:
				in_TEXT = TRUE;
				if(in_SPACE) {
					argv[argc] = _argv+j;
					argc++;
				}
				_argv[j] = a;
				j++;
				in_SPACE = FALSE;
				break;
			}
		}
		i++;
	}
	_argv[j] = '\0';
	argv[argc] = NULL;

	(*_argc) = argc;
	return argv;
}
	
void WINAPI ServiceMain(DWORD argc, LPTSTR* argv)
{
    // Register handler
    statusHandle = RegisterServiceCtrlHandler("", ControlHandler);
    if (statusHandle)
    {		
		int s_argc;
		LPTSTR *s_argv = CommandLineToArgvA(GetCommandLine(), &s_argc);

		SetStatus(SERVICE_RUNNING);

		wostart_main(s_argc, s_argv);

        SetStatus(SERVICE_STOPPED);
    }
}

int _tmain(int argc, TCHAR* argv[], TCHAR* envp[])
{
    // Service entry point
    SERVICE_TABLE_ENTRY table[] = {{"", ServiceMain}, {NULL, NULL}};
    BOOL serviceDidInit = StartServiceCtrlDispatcher(table);

	if(!serviceDidInit)
		wostart_main(argc, argv);
}

