#
# $Id: Makefile 4462 2013-06-23 12:00:28Z kredel $
#
# Makefile for the interface of JAS to meditor
# by Raphael Jolly, Heinz kredel
#

#JASPATH=$(HOME)/jas
JASPATH=..
LIBPATH=$(HOME)/java/lib
MEDITORPATH=$(HOME)/java/meditor3.0

CLASSPATH=$(LIBPATH)/jython.jar:$(MEDITORPATH)/meditor.jar

# incompatible with jython version
#BOOTCLASSPATH4="/usr/java/j2sdk1.4.0/jre/lib/rt.jar"
#	javac -g -source 1.4 -target 1.4 -classpath $(CLASSPATH) -bootclasspath $(BOOTCLASSPATH4) -sourcepath src -d classes src/edu/jas/editorengine/*.java

.SUFFIXES : .class .java .jar 
.PHONY    : clean

FILES=$(wildcard src/edu/jas/editorengine/*.java)
#echo $(FILES) $(JASPATH)/examples/jas.py
CLASSES=$(subst src,classes,$(subst java,class,$(FILES)))
#echo $(CLASSES)

all: jas-meditor.jar

$(CLASSES): $(FILES)
	-mkdir classes
	javac -g -source 1.4 -target 1.4 -classpath $(CLASSPATH) -sourcepath src -d classes src/edu/jas/editorengine/*.java

jas-meditor.jar: $(CLASSES) $(JASPATH)/examples/jas.py manifest.mf services/jscl.editor.EngineFactory
	-mkdir classes/META-INF
	-mkdir classes/META-INF/services
	cp -a manifest.mf classes/META-INF/MANIFEST.MF
	cp -a services/jscl.editor.EngineFactory classes/META-INF/services/
	jar cfM jas-meditor.jar -C classes .
	jar uf jas-meditor.jar -C $(JASPATH)/examples jas.py
	cp -f jas-meditor.jar $(LIBPATH)
	cp -f jas-meditor.jar ..
	-rm -rf classes

clean:
	rm -rf classes
