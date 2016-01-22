Additional installation steps for a local JAR, which isn't not member of a maven repo. 

JaNaG - Java Name Generator

This library is used to make sensitive user data anonymous for a domain/database export.

1.) Load the JaNaG_CML.jar from http://sourceforge.net/projects/janag.berlios/

2.) Import this JAR to your local maven repo:

mvn install:install-file -Dfile=JaNaG_CML.jar -DgroupId=de.beimax.janag -DartifactId=name-generator -Dversion=1.0 -Dpackaging=jar
