#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')

codesign -s "B3A56A04187C1B3BDEABE7B2A377D7A6A17F8151" -f --signature-size 16384 ./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app

pushd ./target/mytunesrss-${VERSION}-macosx
rm ../mytunesrss-${VERSION}-macosx.zip
zip -r ../mytunesrss-${VERSION}-macosx.zip mytunesrss-${VERSION}
popd
