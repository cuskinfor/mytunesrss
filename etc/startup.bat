REM
REM MyTunesRSS startup script
REM
REM Optional command line options, please add them at the end of the command line at the end of
REM this file, after the JAR file name.
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
REM You might have to add the full path to the java executable (java) if not in path
REM Make sure your current directory is the MyTunesRSS installation dir
REM

SET VM_OPTIONS='-Xms256m -Xmx256m'
SET BOOT_CP='-Xbootclasspath/p:lib/xercesImpl-2.8.1.jar;lib/codewave-zis-1.1.jar'
SET XML_PARSER='-Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl'
SET MYTUNESRSS_PROPS='-Dde.codewave.mytunesrss'

java %VM_OPTIONS% %BOOT_CP% %XML_PARSER% %MYTUNESRSS_PROPS% -jar mytunesrss.jar
