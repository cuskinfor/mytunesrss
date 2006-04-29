@echo off
call setenv.bat
start javaw %JAVA_OPTS% -jar %~ds0MyTunesRSS.jar
