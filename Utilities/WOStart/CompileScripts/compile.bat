set OLDDIR=%CD%
cd %~dp0
cd ..

c:\MSys\bin\sh --login %~dp0\make32.sh
copy WOStart.exe WOStart.32.exe

c:\MSys\bin\sh --login %~dp0\make64.sh
copy WOStart.exe WOStart.64.exe

c:\MSys\bin\sh --login %~dp0\makeclean.sh

cd %OLDDIR%
