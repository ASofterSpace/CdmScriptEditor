#!/bin/bash

cd src/com/asofterspace

rm -rf toolbox

mkdir toolbox
cd toolbox

mkdir codeeditor
mkdir configuration
mkdir io
mkdir gui
mkdir utils
mkdir web

cd ../../../..

cp ../Toolbox-Java/src/com/asofterspace/toolbox/*.java src/com/asofterspace/toolbox
cp ../Toolbox-Java/src/com/asofterspace/toolbox/codeeditor/*.* src/com/asofterspace/toolbox/codeeditor
cp ../Toolbox-Java/src/com/asofterspace/toolbox/configuration/*.* src/com/asofterspace/toolbox/configuration
cp ../Toolbox-Java/src/com/asofterspace/toolbox/io/*.* src/com/asofterspace/toolbox/io
cp ../Toolbox-Java/src/com/asofterspace/toolbox/gui/*.* src/com/asofterspace/toolbox/gui
cp ../Toolbox-Java/src/com/asofterspace/toolbox/utils/*.* src/com/asofterspace/toolbox/utils
cp ../Toolbox-Java/src/com/asofterspace/toolbox/web/*.* src/com/asofterspace/toolbox/web

rm -rf bin

mkdir bin
mkdir emf

cd src

find . -name "*.java" > sourcefiles.list

javac -cp "../emf/*" -d ../bin @sourcefiles.list
