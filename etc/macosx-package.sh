#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')
APP=./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app

# app signining
codesign -s "Developer ID Application: Michael Descher (64Q697M329)" -f --deep $APP

# pkg building
productbuild --component $APP /Applications/ --sign "Developer ID Installer: Michael Descher (64Q697M329)" --product $APP/Contents/Info.plist ./target/MyTunesRSS-${VERSION}.pkg

# verification
spctl -v --assess --type install ./target/MyTunesRSS-${VERSION}.pkg
spctl -v --assess --type execute $APP
