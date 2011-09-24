#!/bin/sh

#
# MyTunesRSS startup script
#
# Optional command line options, please add them at the end of the command line at the end of
# this file, after the JAR file name.
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
# You might have to add the full path to the java executable (java) if not in path
# Make sure your current directory is the MyTunesRSS installation dir
#

VM_OPTIONS='-Xms256m -Xmx256m'
BOOT_CP='-Xbootclasspath/p:lib/xercesImpl-2.9.1.jar'
XML_PARSER='-Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl'
MYTUNESRSS_PROPS='-Dde.codewave.mytunesrss'

java $VM_OPTIONS $BOOT_CP $XML_PARSER $MYTUNESRSS_PROPS -jar mytunesrss.jar
