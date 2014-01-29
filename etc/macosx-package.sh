#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')

for APP in ./target/mytunesrss-${VERSION}-macosx ./target/mytunesrss-${VERSION}-macosx-appstore
do
    codesign -s "3rd Party Mac Developer Application: Michael Descher" -f --signature-size 16384 --deep $APP/mytunesrss-${VERSION}/MyTunesRSS.app
    productbuild --component $APP/mytunesrss-${VERSION}/MyTunesRSS.app /Applications/ --sign "3rd Party Mac Developer Installer: Michael Descher" --product $APP/mytunesrss-${VERSION}/MyTunesRSS.app/Contents/Info.plist $APP/MyTunesRSS-${VERSION}.pkg
done
