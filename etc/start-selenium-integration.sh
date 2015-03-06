#!/usr/bin/env bash

mkdir -p $1
cp -R ../../../selenium/etc/prefs-cache-h2/* $1
mkdir -p ../../selenium/etc/iPhoto/images
cp -R ../../../selenium/etc/iPhoto/images/* ../../selenium/etc/iPhoto/images
java -Xmx256m -XX:HeapDumpPath=$1 -Xbootclasspath/p:lib/xercesImpl-2.9.1.jar -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl -Dde.codewave.mytunesrss -jar mytunesrss.jar -cacheDataPath $1 -prefsDataPath $1 -headless -shutdownPort 12345 > /dev/null 2>&1 &

sleep 3

until tail -n 50 $1/MyTunesRSS.log | grep -q "47110 STARTING"; do
    echo -n "."
    sleep 1
done

echo " MyTunesRSS user interface is listening."
