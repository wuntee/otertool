#!/bin/bash

mvn install:install-file -Dfile=sdklib/sdklib-r16.jar -DgroupId=com.android.sdklib -DartifactId=sdklib -Dversion=r16 -Dpackaging=jar
mvn install:install-file -Dfile=smali/smali-1.2.6.jar -DgroupId=antlr -DartifactId=smali -Dversion=1.2.6 -Dpackaging=jar
mvn install:install-file -Dfile=smali/baksmali-1.2.6.jar -DgroupId=antlr -DartifactId=baksmali -Dversion=1.2.6 -Dpackaging=jar
mvn install:install-file -Dfile=swt-3.7-cocoa-macosx/swt.jar -DgroupId=org.eclipse.swt -DartifactId=swt -Dversion=3.7-cocoa-macosx -Dpackaging=jar
mvn install:install-file -Dfile=swt-3.7-gtk-linux-x86/swt.jar -DgroupId=org.eclipse.swt -DartifactId=swt -Dversion=3.7-gtk-linux -Dpackaging=jar
mvn install:install-file -Dfile=swt-3.7-gtk-linux-x86_64/swt.jar -DgroupId=org.eclipse.swt -DartifactId=swt -Dversion=3.7-gtk-linux-64 -Dpackaging=jar
