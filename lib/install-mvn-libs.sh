#!/bin/bash

mvn install:install-file -Dfile=sdklib/sdklib.jar -DgroupId=com.android.sdklib -DartifactId=sdklib -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=smali/smali-1.2.6.jar -DgroupId=antlr -DartifactId=smali -Dversion=1.2.6 -Dpackaging=jar
mvn install:install-file -Dfile=smali/baksmali-1.2.6.jar -DgroupId=antlr -DartifactId=baksmali -Dversion=1.2.6 -Dpackaging=jar
mvn install:install-file -Dfile=swt-3.7-cocoa-macosx/swt.jar -DgroupId=org.eclipse.swt -DartifactId=swt -Dversion=3.7-cocoa-macosx -Dpackaging=jar
mvn install:install-file -Dfile=swt-3.7-gtk-linux/swt.jar -DgroupId=org.eclipse.swt -DartifactId=swt -Dversion=3.7-gtk-linux -Dpackaging=jar
