REM
REM MyTunesRSS startup script
REM
REM Optional command line options, please add them at the end of the command line at the end of
REM this file, after the JAR file name.
REM
REM Admin host (otherwise MyTunesRSS will listen on all interfaces)
REM -adminHost 127.0.0.1
REM
REM Admin port (otherwise a rendomly chosen free port will be used)
REM -adminPort 12345
REM
REM Run without any GUI components (e.g. no admin server port notification at startup)
REM -headless
REM
REM Preferences data path (otherwise the default is used)
REM -prefsDataPath /var/mytunesrss/prefs
REM
REM Cache data path (otherwise the default is used)
REM -cacheDataPath /var/mytunesrss/cache
REM
REM Shutdown port, send the text "SHUTDOWN" to this port to shutdown MyTunesRSS gracefully. Listens on 127.0.0.1 only.
REM -shutdownPort 12345
REM
REM Shutdown a local instance running on the specified port.
REM -shutdown 12345
REM
REM Reset the database to the default database and create a new one. Should only be used explicitly in case MyTunesRSS
REM does not start anymore due to a fatal database problem which cannot be fixed otherwise. Should not be used in
REM startup scripts since the database would be reset each time MyTunesRSS starts.
REM -resetDatabase
REM
REM You might have to add the full path to the java executable (java) if not in path
REM Make sure your current directory is the MyTunesRSS installation dir
REM

SET VM_OPTIONS='-Xmx512m -XX:HeapDumpPath=.'
SET BOOT_CP='-Xbootclasspath/p:lib/xercesImpl-2.11.0.jar'
SET MYTUNESRSS_PROPS='-Dde.codewave.mytunesrss'

java %VM_OPTIONS% %BOOT_CP% %MYTUNESRSS_PROPS% -jar mytunesrss.jar
