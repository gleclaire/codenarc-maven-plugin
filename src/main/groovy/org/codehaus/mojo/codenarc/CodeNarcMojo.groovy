package org.codehaus.mojo.codenarc
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.List;

import org.apache.maven.reporting.AbstractMavenReport
import org.apache.maven.project.MavenProject
import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.factory.ArtifactFactory
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.resolver.ArtifactResolver
import org.apache.maven.doxia.siterenderer.Renderer
import org.apache.maven.doxia.tools.SiteTool
import org.apache.maven.plugin.MojoFailureException
import org.codehaus.plexus.resource.ResourceManager
import org.codehaus.plexus.resource.loader.FileResourceLoader

/**
 * Create a CodeNarc Report.
 *
 * @goal codenarc
 * @phase site
 * @requiresDependencyResolution compile
 * @requiresProject
 *
 * @author <a href="mailto:gleclaire@codehaus.org">Garvin LeClaire</a>
 * @version $Id: CodeNarcMojo.groovy gleclaire $
 */
class CodeNarcMojo extends AbstractMavenReport {
  /**
   * The name of the Plug-In.
   *
   */
  static final String PLUGIN_NAME = "codenarc"

  /**
   * The name of the property resource bundle (Filesystem).
   *
   */
  static final String BUNDLE_NAME = "codenarc"

  /**
   * The key to get the name of the Plug-In from the bundle.
   *
   */
  static final String NAME_KEY = "report.codenarc.name"

  /**
   * The key to get the description of the Plug-In from the bundle.
   *
   */
  static final String DESCRIPTION_KEY = "report.codenarc.description"

  /**
   * The handle for the resource bundle.
   *
   */
  ResourceBundle bundle

  /**
   * Maven Project
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  MavenProject project

  /**
   * Location where generated html will be created.
   *
   * @parameter default-value="${project.reporting.outputDirectory}"
   * @required
   */

  File outputDirectory

  /**
   * Specifies the directory where the xml output will be generated.
   *
   * @parameter default-value="${project.build.directory}"
   * @required
   */
  File xmlOutputDirectory

  /**
   * Doxia Site Renderer.
   *
   * @component
   * @required
   * @readonly
   */
  Renderer siteRenderer

  /**
   * SiteTool.
   *
   * @component role="org.apache.maven.doxia.tools.SiteTool"
   * @required
   * @readonly
   */
  protected SiteTool siteTool

  /**
   * @component
   * @required
   * @readonly
   */
  ResourceManager resourceManager

  /**
   * Skip entire check.
   *
   * @parameter expression="${codenarc.skip}" default-value="false"
   */
  boolean skip

  /**
   * Maximum Java heap size in megabytes  (default=512).
   *
   * @parameter expression="${codenarc.maxHeap}" default-value="512"
   */
  int maxHeap

  /**
   * The CodeNarc rulesets to use. See the <a href="http://codenarc.sourceforge.net/codenarc-rule-index.html">CodeNarc Rule Index</a>
   *  for a list of some included. Defaults to the "rulesets/basic.xml,rulesets/exceptions.xml,rulesets/imports.xml"
   *
   * @parameter expression="${codenarc.rulesetfiles}"
   */
  String rulesetfiles = "rulesets/basic.xml,rulesets/exceptions.xml,rulesets/imports.xml"

  /**
   * The comma-separated list of Ant-style file patterns specifying files that must be included.
   *  See the <a href="http://codenarc.sourceforge.net/codenarc-command-line.html">CodeNarc Command-Line Parameters</a>
   *  for the default behavior.
   *
   * @parameter expression="${codenarc.includes}"
   */
  String includes

  /**
   * The comma-separated list of Ant-style file patterns specifying files that must be excluded.
   *  See the <a href="http://codenarc.sourceforge.net/codenarc-command-line.html">CodeNarc Command-Line Parameters</a>
   *  for the default behavior.
   *
   * @parameter expression="${codenarc.excludes}"
   */
  String excludes

  /**
   * The CodeNarc log4j config file.
   * Each path may be optionally prefixed by any of the valid java.net.URL prefixes, such as "file:"
   * (to load from a relative or absolute filesystem path), or "http:".
   *
   * @parameter expression="${codenarc.log4jConfigFile}"
   */
  String log4jConfigFile

  /**
   * Specifies the location of the source directory to be used for CodeNarc.
   *
   * @parameter default-value="${project.build.sourceDirectory}"
   * @required
   */
  File sourceDirectory

  /**
   * The directories containing the sources to be compiled.
   *
   * @parameter expression="${project.compileSourceRoots}"
   * @required
   * @readonly
   * @since 0.18.1-1
   */
  List compileSourceRoots

  /**
   * The directories containing the test-sources to be compiled.
   *
   * @parameter expression="${project.testCompileSourceRoots}"
   * @required
   * @readonly
   * @since 0.18.1-1
   */
  List testSourceRoots

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
   * @required
   * @readonly
   */
  protected ArtifactFactory factory

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
   * @required
   * @readonly
   */
  protected ArtifactResolver artifactResolver

  /**
   * List of Remote Repositories used by the resolver
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List remoteRepositories

  /**
   * Location of the local repository.
   *
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository localRepository

  /**
   * The maximum number of priority 1 violations allowed before
   * failing the build.
   *
   * @parameter expression="${codenarc.maxPriority1Violations}" default-value = "-1"
   * @since 0.17-2
   */
  int maxPriority1Violations

  /**
   * The maximum number of priority 2 violations allowed before
   * failing the build.
   *
   * @parameter expression="${codenarc.maxPriority2Violations}" default-value = "-1"
   * @since 0.17-2
   */
  int maxPriority2Violations

  /**
   * The maximum number of priority 3 violations allowed before
   * failing the build.
   *
   * @parameter expression="${codenarc.maxPriority3Violations}" default-value = "-1"
   * @since 0.17-2
   */
  int maxPriority3Violations

  /**
   * Default log4j file content if one dosn't exist.
   *
   */
  def log4jContents = """# Set root logger level to DEBUG and only append to the CONSOLE.
log4j.rootLogger=DEBUG, CONSOLE

log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender

# CONSOLE uses PatternLayout.
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=%d{ISO8601} %c{1} [%t] %p - %m%n

# FileAppender appender uses PatternLayout.
log4j.appender.default.R=org.apache.log4j.FileAppender
log4j.appender.default.R.append=true
log4j.appender.default.R.file=./codenarc.log
log4j.appender.R.layout=org.apache.log4j.PatternLayout
log4j.appender.R.layout.ConversionPattern=%d{ISO8601} %c{1} [%t] %p - %m%n

"""

  /**
   * The CodeNarc version to use for the plugin
   *
   * @parameter expression="${codenarc.codenarc.version}" default-value="0.22"
   */
  String codeNarcVersion

  /**
   * The log4j version to use for the plugin
   *
   * @parameter expression="${codenarc.log4j.version}" default-value="1.2.13"
   */
  String log4jVersion

  /**
   * The Groovy version to use for the plugin
   *
   * @parameter expression="${codenarc.groovy.version}" default-value="1.7.5"
   */
  String groovyVersion

  /**
   * Executes the generation of the report.
   *
   * Callback from Maven Site Plugin or from AbstractMavenReport.execute() => generate().
   *
   * @param locale
   *            the locale the report should be generated for
   * @see org.apache.maven.reporting.AbstractMavenReport #executeReport(java.util.Locale)
   *
   */
  protected void executeReport( Locale locale ) {
    log.debug( "codeNarcVersion ==> ${codeNarcVersion}" )
    log.debug( "log4jVersion ==> ${log4jVersion}" )
    log.debug( "groovyVersion ==> ${groovyVersion}" )

    def items = [
            [groupId: 'org.codenarc', artifactId: 'CodeNarc', version: codeNarcVersion],
            [groupId: 'log4j', artifactId: 'log4j', version: log4jVersion],
            [groupId: 'org.codehaus.groovy', artifactId: 'groovy-all', version: groovyVersion]
    ]


    compileSourceRoots.each() { compileSourceRoot -> log.debug( "compileSourceRoot ==> ${compileSourceRoot}" ) }


    resourceManager.addSearchPath( FileResourceLoader.ID, project.getFile().getParentFile().getAbsolutePath() )
    resourceManager.addSearchPath( "url", "" )

    resourceManager.setOutputDirectory( new File( project.getBuild().getDirectory() ) )

    log.debug( "resourceManager outputDirectory is " + resourceManager.outputDirectory )

    def xmlReportFileName = "${xmlOutputDirectory}/CodeNarc.xml"
    File outputFile = new File( xmlReportFileName )

    def ant = new AntBuilder()
    Artifact pomArtifact

    File tempLogFile

    def codenarcClasspath = ant.path( id: "codenarc.classpath" ) {

      items.each() {
        item ->

          pomArtifact = this.factory.createArtifact( item[ 'groupId' ], item[ 'artifactId' ], item[ 'version' ], "", "jar" )

          artifactResolver.resolve( pomArtifact, this.remoteRepositories, this.localRepository )

          log.debug( "pomArtifact => ${pomArtifact.file}" )
          pathelement( location: pomArtifact.file )
      }


      tempLogFile = new File( "${project.getBuild().getDirectory()}/log4j.properties" )

      if ( log4jConfigFile ) {
        tempLogFile.text = log4jConfigFile.text
      } else {

        tempLogFile.text = log4jContents
        log4jConfigFile = tempLogFile.toURL()
      }

      log.debug( "log4jConfigFile => ${tempLogFile}" )
      pathelement( location: tempLogFile )

    }

    codenarcClasspath.list().each {
      log.debug( "codenarc.classpath entry => ${it}" )
    }


    ant.java( classname: "org.codenarc.CodeNarc", classpathref: "codenarc.classpath", fork: "true", failonerror: "false", clonevm: "false", maxmemory: "${maxHeap}m" ) {

      log.debug( "log4jConfigFile => ${log4jConfigFile}" )
      sysproperty( key: "log4j.configuration", value: "${cleanPath(log4jConfigFile)}" )

      def antBasedir = "-basedir=${cleanPath(sourceDirectory)}"
      log.debug( "antBasedir => ${antBasedir}" )
      arg( value: antBasedir )

      def antTitle = '-title="' + this.project.name + '"'
      log.debug( "antTitle => ${antTitle}" )
      arg( value: antTitle )

      def antReport = "-report=xml:" + xmlReportFileName
      log.debug( "antReport => ${antReport}" )
      arg( value: antReport )

      def antRuleSets = "-rulesetfiles=" + cleanPath(rulesetfiles)
      log.debug( "antRuleSets => ${antRuleSets}" )
      arg( value: antRuleSets )

      if ( includes ) {
        def antIncludes = "-includes=" + cleanPath(includes)
        log.debug( "antIncludes => ${antIncludes}" )
        arg( value: antIncludes )
      }

      if ( excludes ) {
        def antExcludes = "-excludes=" + cleanPath(excludes)
        log.debug( "antExcludes => ${antExcludes}" )
        arg( value: antExcludes )
      }
    }

    if ( !outputDirectory.exists() ) {
      if ( !outputDirectory.mkdirs() ) {
        fail( "Cannot create HTML output directory" )
      }
    }

    if ( outputFile.exists() ) {
      log.info( "Generating CodeNarc HTML" )

      CodeNarcReportGenerator generator = new CodeNarcReportGenerator( getSink(), getBundle( locale ), this.project.getBasedir(), siteTool )

      generator.setLog( log )

      generator.setCodeNarcResults( new XmlSlurper().parse( outputFile ) )

      generator.setOutputDirectory( new File( outputDirectory.getAbsolutePath() ) )

      generator.generateReport()

      log.info( "totalPriority1Violations is " + generator.totalPriority1Violations )
      log.info( "totalPriority2Violations is " + generator.totalPriority2Violations )
      log.info( "totalPriority3Violations is " + generator.totalPriority3Violations )

      if ( ( maxPriority1Violations > -1 ) && ( generator.totalPriority1Violations > maxPriority1Violations ) ) {
        throw new MojoFailureException( "totalPriority1Violations exceeded threshold of ${maxPriority1Violations} errors with " + generator.totalPriority1Violations )
      }

      if ( ( maxPriority2Violations > -1 ) && ( generator.totalPriority2Violations > maxPriority2Violations ) ) {
        throw new MojoFailureException( "totalPriority2Violations exceeded threshold of ${maxPriority2Violations} errors with " + generator.totalPriority2Violations )
      }

      if ( ( maxPriority3Violations > -1 ) && ( generator.totalPriority3Violations > maxPriority3Violations ) ) {
        throw new MojoFailureException( "totalPriority3Violations exceeded threshold of ${maxPriority3Violations} errors with " + generator.totalPriority3Violations )
      }
    }
  }

  /**
   * Checks whether prerequisites for generating this report are given.
   *
   * @return true if report can be generated, otherwise false
   * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
   */
  boolean canGenerateReport( ) {

    def canGenerate = false

    log.info( "sourceDirectory is ${sourceDirectory}" )

    if ( !skip && sourceDirectory.exists() ) {
      canGenerate = true
    }

    log.debug( "canGenerate is ${canGenerate}" )

    return canGenerate
  }

  /**
   * Returns the plugins description for the "generated reports" overview page.
   *
   * @param locale
   *            the locale the report should be generated for
   *
   * @return description of the report
   * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
   */
  String getDescription( Locale locale ) {
    return getBundle( locale ).getString( DESCRIPTION_KEY )
  }

  /**
   * Returns the plugins name for the "generated reports" overview page and the menu.
   *
   * @param locale
   *            the locale the report should be generated for
   *
   * @return name of the report
   * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
   */
  String getName( Locale locale ) {
    return getBundle( locale ).getString( NAME_KEY )
  }

  /**
   * Returns report output file name, without the extension.
   *
   * Called by AbstractMavenReport.execute() for creating the sink.
   *
   * @return name of the generated page
   * @see org.apache.maven.reporting.MavenReport#getOutputName()
   */
  String getOutputName( ) {
    return PLUGIN_NAME
  }

  protected MavenProject getProject( ) {
    return this.project
  }

  /**
   * Returns the report output directory.
   *
   * Called by AbstractMavenReport.execute() for creating the sink.
   *
   * @return full path to the directory where the files in the site get copied to
   * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
   */
  protected String getOutputDirectory( ) {
    return outputDirectory.getAbsolutePath()
  }

  protected Renderer getSiteRenderer( ) {
    return this.siteRenderer
  }

  ResourceBundle getBundle( locale ) {

    this.bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale, CodeNarcMojo.class.getClassLoader() )

    log.debug( "Mojo Locale is " + this.bundle.getLocale().getLanguage() )

    return bundle
  }

  protected String cleanPath(File filePathToClean){
    if (!filePathToClean){
      return null
    }
    cleanPath(filePathToClean.path)
  }

  protected String cleanPath(String pathToClean){
    pathToClean?.replace('\\','/')
  }

}
