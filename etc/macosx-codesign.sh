#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')

codesign -s "E6450995C010E3E3BD2437C047FBA03856A9CF2D" -f --signature-size 8192 ./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app

pushd ./target/mytunesrss-${VERSION}-macosx
rm ../mytunesrss-${VERSION}-macosx.zip
zip -r ../mytunesrss-${VERSION}-macosx.zip mytunesrss-${VERSION}
popd
