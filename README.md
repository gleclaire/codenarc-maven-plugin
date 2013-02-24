codenarc-maven-plugin
=====================

Maven Mojo Plug-In to generate reports based on the CodeNarc Analyzer

The documentation for the **CodeNarc Maven Plugin** is here: http://mojo.codehaus.org/codenarc-maven-plugin/

Run all test
mvn -Prun-its clean install


Run selected tests example
mvn -Prun-its -Dinvoker.test=basic-1 clean install

Run tests on CodeNarc test source code that is local instead of from SVN repository
mvn -o -DtestSrc=local -DlocalTestSrc=/opt/codenarc/codenarc-0.17/src/test clean install
 