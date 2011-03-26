#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')

codesign -fs "Codewave Software" ./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app

pushd ./target/mytunesrss-${VERSION}-macosx
rm ../mytunesrss-${VERSION}-macosx.zip
zip -r ../mytunesrss-${VERSION}-macosx.zip mytunesrss-${VERSION}
popd
