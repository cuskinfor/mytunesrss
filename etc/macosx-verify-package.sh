#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')

for APP in ./target/mytunesrss-${VERSION}-macosx ./target/mytunesrss-${VERSION}-macosx-appstore
do
    spctl --assess --type execute $APP/mytunesrss-${VERSION}/MyTunesRSS.app
    spctl --assess --type install $APP/MyTunesRSS-${VERSION}.pkg
done
