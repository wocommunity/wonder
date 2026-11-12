# WOStart

Version 1.2

This is a modified `java.exe` for Windows that behaves identically like the `<Appname>.cmd` startup script.

Using this has some advantages:

- Only one process per App (no sub processes from cmd startup script)
- ProcessName is the name of your app (rather than `java.exe`)
- No problems due to classpath length

As with `java.exe` the current VM is read from the registry. This works for Java 8 as well as for all newer versions (Java 9, 10, 11, 17, 21, ...). WOStart checks the new JDK / JRE registry keys (`Software\JavaSoft\JDK` and `Software\JavaSoft\JRE`) first and no longer looks for specific versions, which the new Java update scheme does not allow to do reliably anymore.

## Compiling

`Makefile` and `make.config` are prepared for compiling using Mingw (64 Bit) under Windows.
To get it compiled with Mingw under Linux you might have to tweak `make.config` a little bit.

A Windows binary is also built automatically in CI (mingw64), see `.github/workflows/wostart.yml`.

## Installation

Just copy `WOStart.exe` into your `<AppName>.woa` folder as `<AppName>.exe` and use it just like you used the startup script `<AppName>.cmd` - all command line arguments are taken into account as with the startup script.

This means, the file `<AppName>.woa/Contents/Windows/CLSSPATH.TXT` is parsed and the classpath is constructed dynamically (and without any restrictions in length) based on all entries in this file.
The `ApplicationClass` described in the comment section in the file's head is taken into account as well as `JVMOptions` and `JVM`.
Please note, that for the option `JVM`, you have to supply a valid path to a JVM of the very same architecture as the WOStart you use.
Environment variables (e.g. `%JAVA_HOME%`) in the JVM path are resolved before the JVM is started.

## Windows Service

This WOStart also enables you, to install and start your WebObjects application as windows service. You just have to install it as services using the Windows tool `SC`. You can supply the command line arguments the very same way.

For example:

```
sc create WOApp1 start= auto DisplayName= "WOApp1" binPath= "<path>/WOApp1.woa/WOApp1.exe -Xmx1512m -WOPort 4444"
```

would install your App `WOApp1` at a given path to start with the given heap setting, listening on Port 4444.

## Changes since 1.1

- Support for Java 9 / 10 / 11 and later: the registry lookup no longer checks for specific JRE versions (1.5 - 10); the new JDK and JRE registry keys are checked first.
- JVM options `--add-exports` and `--add-opens` are recognized and passed to the JVM before all other JVM arguments, as required by Java 11 upwards.
- `-x`, `-agentlib` and `-javaagent` are treated as JVM arguments, so debug agents and similar can be supplied from `CLSSPATH.TXT`.
- Environment variables (e.g. `%JAVA_HOME%`) in the JVM path are resolved before the JVM is started.
- Version number raised to 1.2.
