# This file is part of TTDViewer.
# TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
# TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.

# Tool chain
JAVA = java

JAVAC = javac
JAVAC_OPTS = -deprecation

JAVADOC = javadoc
JAVADOC_OPTS = -private

JAR = jar

ZIP = zip

XMLLINT = xmllint
XMLLINT_OPTS = --noout

# Folders
SRC = src
OUTPUT = classes
RELEASE = release
MAIN = TTDViewer
DOC = doc

# Ok, always doing a 'clean' is not really the point of Makefile
all: validate clean
	@mkdir -p $(OUTPUT)
	$(JAVAC) $(JAVAC_OPTS) -sourcepath $(SRC) -classpath $(OUTPUT) -d $(OUTPUT) -s $(OUTPUT) $(SRC)/*.java
	hg parents --template="{date|shortdate} " > $(OUTPUT)/rev.txt
	# hg identify also checks for local modifications
	hg identify -i >> $(OUTPUT)/rev.txt
	cp -u $(SRC)/*.xml $(SRC)/*.xsd $(OUTPUT)

validate:
	$(XMLLINT) $(XMLLINT_OPTS) --schema $(SRC)/recolor.xsd $(SRC)/recolor.xml

run: all
	cd $(OUTPUT); $(JAVA) $(MAIN); cd ..

clean:
	rm -f $(OUTPUT)/*.class

jar: all
	@mkdir -p $(RELEASE)
	@echo "$(JAVA) -jar $(MAIN).jar %1 %2 %3 %4 %5 %6 %7 %8 %9" > $(RELEASE)/$(MAIN).bat
	@echo "#!/bin/sh" > $(RELEASE)/$(MAIN).sh
	@echo '$(JAVA) -jar `readlink $$0 | xargs dirname`/$(MAIN).jar "$$@"' >> $(RELEASE)/$(MAIN).sh
	@chmod 644 $(RELEASE)/$(MAIN).bat
	@chmod 755 $(RELEASE)/$(MAIN).sh
	@cp COPYING readme.txt $(RELEASE)
	$(JAR) cfe  $(RELEASE)/$(MAIN).jar $(MAIN) -C $(OUTPUT) .

jar_run: jar
	cd $(RELEASE); ./$(MAIN).sh; cd ..

release: jar
	rm -f $(MAIN).zip; $(ZIP) -j $(MAIN).zip $(RELEASE)/*

doc:
	@mkdir -p $(DOC)
	$(JAVADOC) $(JAVADOC_OPTS) -sourcepath $(SRC) -classpath $(OUTPUT) -d $(DOC) $(SRC)/*.java

help:
	@echo 'Targets:'
	@echo '  all       Build'
	@echo '  run       Build & Run'
	@echo '  jar       Build & Bundle'
	@echo '  jar_run   Build & Bundle & Run'
	@echo '  release   Build & Bundle & Zip'
	@echo '  validate  Only validate XML'
	@echo '  doc       Build documentation'
	@echo '  clean     Remove temporary files'
	@echo '  help      Print this message'

.PHONY: all run clean validate help jar run_jar release doc
