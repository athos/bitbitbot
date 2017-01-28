#!/bin/sh

set -e

REVISION=$(git rev-parse --short HEAD)

git checkout -B heroku

lein clean
lein cljsbuild once app

if [ -d "target" ]; then
    git add -f app.js target
fi

set +e
ret=$(git status | grep -q 'no changes added to commit'; echo $?)
set -e

if [ $ret -eq 0 ] ; then
    echo "Nothing to push to heroku"
else
    git commit -m "Generate JS code from CI (for commit ${REVISION})"
    git push origin heroku
    echo "Pushed to heroku"
fi
