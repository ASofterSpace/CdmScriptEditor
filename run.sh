#!/bin/bash

# check if we are running a 64-Bit version of Java
javaversion=$(java -version 2>&1)

if [[ $javaversion = *"64-Bit"* ]]; then
	# we are running 64 bit - nice! assign loads of heap!
	java -classpath "bin:emf/*" -Xms16m -Xmx4096m com.asofterspace.cdmScriptEditor.Main "$@"
else
	# we are running 32 bit - oh no! assign only a tinly little bit of heap!
	java -classpath "bin:emf/*" -Xms16m -Xmx1024m com.asofterspace.cdmScriptEditor.Main "$@"
fi
