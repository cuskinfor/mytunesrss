#!/bin/sh

#
# MyTunesRSS startup script
#
# Optional command line options, please add them at the end of the command line at the end of
# this file, after the JAR file name.
#
# Admin host (otherwise MyTunesRSS will listen on all interfaces)
# -adminHost 127.0.0.1
#
# Admin port (otherwise a rendomly chosen free port will be used)
# -adminPort 12345
#
# Run without any GUI components (e.g. no admin server port notification at startup)
# -headless
#
# Preferences data path (otherwise the default is used)
# -prefsDataPath /var/mytunesrss/prefs
#
# Cache data path (otherwise the default is used)
# -cacheDataPath /var/mytunesrss/cache
#
# Shutdown port, send the text "SHUTDOWN" to this port to shutdown MyTunesRSS gracefully. Listens on 127.0.0.1 only.
# -shutdownPort 12345
#
# Shutdown a local instance running on the specified port.
# -shutdown 12345
#
# You might have to add the full path to the java executable (java) if not in path
# Make sure your current directory is the MyTunesRSS installation dir
#

VM_OPTIONS='-Xmx512m -XX:HeapDumpPath=.'
BOOT_CP='-Xbootclasspath/p:lib/xercesImpl-2.9.1.jar'
XML_PARSER='-Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl'
MYTUNESRSS_PROPS='-Dde.codewave.mytunesrss'

java $VM_OPTIONS $BOOT_CP $XML_PARSER $MYTUNESRSS_PROPS -jar mytunesrss.jar
