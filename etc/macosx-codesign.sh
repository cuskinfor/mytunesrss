#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx.dir/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx.dir\/mytunesrss-.*/\1/')

codesign -fs "Codewave Software" ./target/mytunesrss-${VERSION}-macosx.dir/mytunesrss-${VERSION}/MyTunesRSS.app

pushd ./target/mytunesrss-${VERSION}-macosx.dir
zip ../mytunesrss-${VERSION}-macosx.zip mytunesrss-${VERSION}
popd
