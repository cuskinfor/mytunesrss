#!/bin/sh

# get version number
VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')

# remove debug gwt files
rm -rf target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app/Contents/Resources/Java/webapps/ADMIN/VAADIN/widgetsets/WEB-INF
rm -rf target/mytunesrss-${VERSION}-system-independent/mytunesrss-${VERSION}/webapps/ADMIN/VAADIN/widgetsets/WEB-INF
rm -rf target/mytunesrss-${VERSION}-windows/mytunesrss-${VERSION}/data/webapps/ADMIN/VAADIN/widgetsets/WEB-INF

# (re-)build system independent zip
pushd ./target/mytunesrss-${VERSION}-system-independent
rm ../mytunesrss-${VERSION}-system-independent.zip
zip -r ../mytunesrss-${VERSION}-system-independent.zip mytunesrss-${VERSION}
popd

# codesign mac os x version and (re-)build zip
codesign -fs "Codewave Software" ./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app
pushd ./target/mytunesrss-${VERSION}-macosx
rm ../mytunesrss-${VERSION}-macosx.zip
zip -r ../mytunesrss-${VERSION}-macosx.zip mytunesrss-${VERSION}
popd
