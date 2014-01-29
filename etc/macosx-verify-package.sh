#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')
APP=./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app

spctl --assess --type execute $APP
spctl --assess --type install ./target/MyTunesRSS-${VERSION}.pkg
