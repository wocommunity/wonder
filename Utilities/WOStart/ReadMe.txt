WOStart.exe

This is a modified java.exe for Windows that behaves identically like
the <Appname>.cmd startup script.

Using this has some advandages

- Only one process per App (no sub processes from cmd startup script)
- ProcessName is the name of your app (rather than java.exe)
- No problems due to classpath length

As with java.exe the current VM is read from registry. For now, this works
up to Java8.



- Compiling

Makefile and make.config are prepared for compiling using Mingw (32 and 64 Bit)
under windows.
To get it compiled with Mingw under Linux you might have to tweak make.config
a little bit.

- Installation

Just copy WOStart.exe into your <AppName>.woa folder as <AppName>.exe and use 
it just like you used the startup script <AppName>.cmd - all command line
arguments are take into account as with the startup script.

This means, the file <AppName>.woa/Contents/Windows/CLSSPATH.TXT is parsed
and the classpath is constructed dynamically (and without any restrictions
in length) based on all entries in this file.
The ApplicationClass described in the comment section in the files head is
taken into account as well as JVMOptions and JVM.
Please note, that for the option JVM, you have to supply a valid path
to a JVM of the very same architechture as the WOStart you use - 
32Bit and 64Bit archtectures do not mix!

- Windows Service

This WOStart also enables you, to install and start your WebObjects application
as windows service. You just have to install it as services using the Windows 
tool "SC". You can supply the command line arguments the very same way.

For Example:

sc create WOApp1 start= auto DisplayName= "WOApp1" binPath= "<path>/WOApp1.woa/WOApp1.exe -Xmx1512m -WOPort 4444"

would install your App "WOApp1" at a given path to start with the given heap setting, 
listening on Port 4444.



