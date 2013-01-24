codenarc-maven-plugin
=====================

Maven Mojo Plug-In to generate reports based on the CodeNarc Analyzer

The documentation for the **CodeNarc Maven Plugin** is here: http://mojo.codehaus.org/codenarc-maven-plugin/

Run all test
mvn -Prun-its clean install


Run selected tests example
mvn -Prun-its -Dinvoker.test=basic-1 clean install
