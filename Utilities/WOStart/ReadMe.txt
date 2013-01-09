WOStart.exe

This is a modified java.exe for Windows that behaves identically like
the <Appname>.cmd startup script.

Using this has some advandages

- Only one process per App (no sub processes from cmd startup script)
- ProcessName is the name of your app (rather than java.exe)
- No problems due to classpath length

As with java.exe the current VM is read from registry. For now, this works
up to Java6.
