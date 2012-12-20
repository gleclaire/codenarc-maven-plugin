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

import groovy.util.slurpersupport.GPathResult
import org.apache.maven.doxia.sink.Sink
import org.apache.maven.doxia.tools.SiteTool
import org.apache.maven.plugin.logging.Log

/**
 * The reporter controls the generation of the CodeNarc report.
 * 
 * @author <a href="mailto:gleclaire@codehaus.org">Garvin LeClaire</a>
 * @version $Id: CodeNarcReportGenerator.groovy Z gleclaire $
 */
class CodeNarcReportGenerator {

    /**
     * The key to get the value if the line number is not available.
     *
     */
    static final String NOLINE_KEY = "report.codenarc.noline"

    /**
     * The key to get the report title from the bundle.
     *
     */
    static final String REPORT_TITLE_KEY = "report.codenarc.reporttitle"

    /**
     * The key to get the report link title of the Plug-In from the bundle.
     *
     */
    static final String LINKTITLE_KEY = "report.codenarc.linktitle"

    /**
     * The key to get the report link of the Plug-In from the bundle.
     *
     */
    static final String LINK_KEY = "report.codenarc.link"

    /**
     * The key to get the name of the Plug-In from the bundle.
     *
     */
    static final String NAME_KEY = "report.codenarc.name"

    /**
     * The key to get the version title for CodeNarc from the bundle.
     *
     */
    static final String VERSIONTITLE_KEY = "report.codenarc.versiontitle"

    /**
     * The key to get the rule title from the bundle.
     *
     */
    static final String RULE_KEY = "report.codenarc.rule"

    /**
     * The key to get the summary title from the bundle.
     *
     */
    static final String SUMMARY_KEY = "report.codenarc.summary"

    /**
     * The key to get the package summary title from the bundle.
     *
     */
    static final String PACKAGE_SUMMARY_KEY = "report.codenarc.packageSummary"

    /**
     * The key to get the files title of the Plug-In from the bundle.
     *
     */
    static final String FILES_KEY = "report.codenarc.files"

    /**
     * The key to get the column title for the line.
     *
     */
    static final String COLUMN_LINE_KEY = "report.codenarc.column.line"

    /**
     * The key to get the column title for the bug.
     *
     */
    static final String COLUMN_BUG_KEY = "report.codenarc.column.bug"

    /**
     * The key to get the column title for the bugs.
     *
     */
    static final String COLUMN_BUGS_KEY = "report.codenarc.column.bugs"

    /**
     * The key to get the column title for the category.
     *
     */
    static final String COLUMN_CATEGORY_KEY = "report.codenarc.column.category"

    /**
     * The key to get the column title for the priority.
     *
     */
    static final String COLUMN_PRIORITY_KEY = "report.codenarc.column.priority"

    /**
     * The key to get the column title for the priority.
     *
     */
    static final String COLUMN_PRIORITY_KEY1 = "report.codenarc.column.priority1"

    /**
     * The key to get the column title for the priority.
     *
     */
    static final String COLUMN_PRIORITY_KEY2 = "report.codenarc.column.priority2"

    /**
     * The key to get the column title for the priority.
     *
     */
    static final String COLUMN_PRIORITY_KEY3 = "report.codenarc.column.priority3"

    /**
     * The key to get the column title for the details.
     *
     */
    static final String COLUMN_DETAILS_KEY = "report.codenarc.column.details"

    /**
     * The key to column title for the Class.
     *
     */
    static final String COLUMN_PACKAGE_KEY = "report.codenarc.column.package"

    /**
     * The key to column title for the files.
     *
     */
    static final String COLUMN_FILES_KEY = "report.codenarc.column.files"

    /**
     * The key to column title for the Bug files.
     *
     */
    static final String COLUMN_BUG_FILES_KEY = "report.codenarc.column.bfiles"

    /**
     * The key to column title for the rule names.
     *
     */
    static final String COLUMN_RULES_KEY = "report.codenarc.column.rule"

    /**
     * The key to column title for the description.
     *
     */
    static final String COLUMN_DESCRIPTION_KEY = "report.codenarc.column.description"

    /**
     * The key to column title for the source line message.
     *
     */
    static final String COLUMN_SOURCE_KEY = "report.codenarc.column.sourceLine"

    /**
     * The character to separate URL tokens.
     *
     */
    static final String URL_SEPARATOR = "/"


    /**
     * The sink to write the report to.
     *
     */
    Sink sink

    /**
     * The bundle to get the messages from.
     *
     */
    ResourceBundle bundle

    /**
     * The logger to write logs to.
     *
     */
    Log log

    /**
     * Location where generated html will be created.
     *
     */

    File outputDirectory


    /**
     * "org.apache.maven.doxia.tools.SiteTool"
     *
     */
    SiteTool siteTool



    File basedir

    GPathResult CodeNarcResults
    
    
    def totalPriority1Violations, totalPriority2Violations, totalPriority3Violations = 0


    /**
     * Default constructor.
     *
     * @param sink
     *            The sink to generate the report.
     * @param bundle
     *            The resource bundle to get the messages from.
     * @param basedir
     *            The project base directory.
     * @param siteTool
     *            Doxia SiteTool Handle.
     */
    CodeNarcReportGenerator(Sink sink, ResourceBundle bundle, File basedir, SiteTool siteTool) {


        assert sink
        assert bundle
        assert basedir
        assert siteTool

        this.sink = sink
        this.bundle = bundle
        this.basedir = basedir
        this.siteTool = siteTool
    }

    /**
     * Prints the top header sections of the report.
     */
    private void doHeading() {

        // the title of the report
        sink.section1()
        sink.sectionTitle1()
        sink.text(getReportTitle())
        sink.sectionTitle1_()

        // information about CodeNarc
        sink.paragraph()
        sink.text(bundle.getString(LINKTITLE_KEY) + " ")
        sink.link(bundle.getString(LINK_KEY))
        sink.text(bundle.getString(NAME_KEY))
        sink.link_()
        sink.paragraph_()

        sink.paragraph()
        sink.text(bundle.getString(VERSIONTITLE_KEY) + " ")
        sink.italic()
        sink.text(CodeNarcResults.@version.text())
        sink.italic_()
        sink.paragraph_()

        sink.paragraph()
        //    sink.text(bundle.getString(VERSIONTITLE_KEY) + " ")
        sink.text("Report Time: ")
        sink.italic()
        sink.text(CodeNarcResults.Report.@timestamp.text())
        sink.italic_()
        sink.paragraph_()

        sink.section1_()

    }

    /**
     * Gets the report title.
     *
     * @return The report title.
     *
     */
    protected String getReportTitle() {
        return bundle.getString(REPORT_TITLE_KEY)
    }


    /**
     * Print the Summary Section.
     */
    protected void printSummary() {
        sink.section1()

        // the summary section
        sink.sectionTitle1()
        sink.text(bundle.getString(SUMMARY_KEY))
        sink.sectionTitle1_()

        sink.table()
        sink.tableRow()

        // total files
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_FILES_KEY))
        sink.tableHeaderCell_()

        // total bugfiles
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_BUG_FILES_KEY))
        sink.tableHeaderCell_()

        // total bugs
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_BUGS_KEY))
        sink.tableHeaderCell_()

        // Priority 1 errors
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PRIORITY_KEY1))
        sink.tableHeaderCell_()

        // Priority 2 errors
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PRIORITY_KEY2))
        sink.tableHeaderCell_()

        // Priority 3 errors
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PRIORITY_KEY3))
        sink.tableHeaderCell_()

        sink.tableRow_()

        sink.tableRow()

        // files
        sink.tableCell()
        sink.text(CodeNarcResults.PackageSummary.@totalFiles.text())
        sink.tableCell_()

        // files with bugs
        sink.tableCell()
        sink.text(CodeNarcResults.PackageSummary.@filesWithViolations.text())
        sink.tableCell_()

        // total bugs
        sink.tableCell()
        sink.text((CodeNarcResults.PackageSummary.@priority1.toInteger() + CodeNarcResults.PackageSummary.@priority2.toInteger() + CodeNarcResults.PackageSummary.@priority3.toInteger()).toString())
        sink.tableCell_()

        // Priority 1 errors
        sink.tableCell()
        sink.text(CodeNarcResults.PackageSummary.@priority1.text())
        sink.tableCell_()

        // Priority 2 errors
        sink.tableCell()
        sink.text(CodeNarcResults.PackageSummary.@priority2.text())
        sink.tableCell_()

        // Priority 3 errors
        sink.tableCell()
        sink.text(CodeNarcResults.PackageSummary.@priority3.text())
        sink.tableCell_()

        sink.tableRow_()
        sink.table_()

        sink.section1_()
        
        totalPriority1Violations = CodeNarcResults.PackageSummary.@priority1.toInteger()
        totalPriority2Violations = CodeNarcResults.PackageSummary.@priority2.toInteger()
        totalPriority3Violations = CodeNarcResults.PackageSummary.@priority3.toInteger()
    }

    /**
     * Print the File Summary Section.
     */
    protected void printPackageSummary() {
        sink.section1()

        // the Files section
        sink.sectionTitle1()
        sink.text(bundle.getString(PACKAGE_SUMMARY_KEY))
        sink.sectionTitle1_()

        /**
         * Class Summary
         */

        sink.table()
        sink.tableRow()

        // packages
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PACKAGE_KEY))
        sink.tableHeaderCell_()

        // total files
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_FILES_KEY))
        sink.tableHeaderCell_()

        // total bugfiles
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_BUG_FILES_KEY))
        sink.tableHeaderCell_()

        // bugs
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_BUGS_KEY))
        sink.tableHeaderCell_()

        // Priority 1 errors
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PRIORITY_KEY1))
        sink.tableHeaderCell_()

        // Priority 2 errors
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PRIORITY_KEY2))
        sink.tableHeaderCell_()

        // Priority 3 errors
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_PRIORITY_KEY3))
        sink.tableHeaderCell_()

        sink.tableRow_()

        CodeNarcResults.Package.each() {cnPackage ->

            if (cnPackage.children().size() >0 ) {
                sink.tableRow()

                // packages
                sink.tableCell()
                sink.link("#" + slashToDot(cnPackage.@path.text()))
                sink.text(cnPackage.@path.text())
                sink.link_()
                sink.tableCell_()

                // total files
                sink.tableCell()
                sink.text(cnPackage.@totalFiles.text())
                sink.tableCell_()

                // files with bugs
                sink.tableCell()
                sink.text(cnPackage.@filesWithViolations.text())
                sink.tableCell_()

                // total bugs
                sink.tableCell()
                sink.text((cnPackage.@priority1.toInteger() + cnPackage.@priority2.toInteger() + cnPackage.@priority3.toInteger()).toString())
                sink.tableCell_()

                // Priority 1 errors
                sink.tableCell()
                sink.text(cnPackage.@priority1.text())
                sink.tableCell_()

                // Priority 2 errors
                sink.tableCell()
                sink.text(cnPackage.@priority2.text())
                sink.tableCell_()

                // Priority 3 errors
                sink.tableCell()
                sink.text(cnPackage.@priority3.text())
                sink.tableCell_()

                sink.tableRow_()
            }
        }

        sink.table_()

        sink.section1_()
    }


    /**
     * Print the File Detail Section.
     */
    protected void printPackageDetail() {
        sink.section1()

        // the Files section
        sink.sectionTitle1()
        sink.text(bundle.getString(FILES_KEY))
        sink.sectionTitle1_()


        CodeNarcResults.Package.each() {cnPackage ->

            if (cnPackage.children().size() >0 ) {

                sink.anchor(slashToDot(cnPackage.@path.text()))
                sink.anchor_()

                def packagePrefix = cnPackage.@path.text() + URL_SEPARATOR

                cnPackage.'File'.each() {cnFile ->

                    sink.sectionTitle2()
                    sink.text(packagePrefix + cnFile.@name.text())
                    sink.sectionTitle2_()

                    sink.table()
                    sink.tableRow()

                    // packages
                    sink.tableHeaderCell()
                    sink.text(bundle.getString(COLUMN_RULES_KEY))
                    sink.tableHeaderCell_()

                    // Priority errors
                    sink.tableHeaderCell()
                    sink.text(bundle.getString(COLUMN_PRIORITY_KEY))
                    sink.tableHeaderCell_()

                    // Line number
                    sink.tableHeaderCell()
                    sink.text(bundle.getString(COLUMN_LINE_KEY))
                    sink.tableHeaderCell_()

                    // source line
                    sink.tableHeaderCell()
                    sink.text(bundle.getString(COLUMN_SOURCE_KEY))
                    sink.tableHeaderCell_()

                    sink.tableRow_()

                    cnFile.Violation.each() {cnViolation ->

                        sink.tableRow()

                        // rules name
                        sink.tableCell()
                        sink.link("#" + cnViolation.@ruleName.text())
                        sink.text(cnViolation.@ruleName.text())
                        sink.link_()
                        sink.tableCell_()

                        // Priority
                        sink.tableCell()
                        sink.text(cnViolation.@priority.text())
                        sink.tableCell_()

                        // line number
                        sink.tableCell()
                        sink.text(cnViolation.@lineNumber.text())
                        sink.tableCell_()

                        // source line
                        sink.tableCell()
                        sink.text(cnViolation.SourceLine.text())
                        sink.tableCell_()

                        sink.tableRow_()
                    }

                    sink.table_()

                }
            }
        }

        sink.section1_()
    }

    /**
     * Print the File Detail Section.
     */
    protected void printRules() {
        sink.section1()

        // the Files section
        sink.sectionTitle1()
        sink.text(bundle.getString(RULE_KEY))
        sink.sectionTitle1_()



        sink.table()
        sink.tableRow()

        // packages
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_RULES_KEY))
        sink.tableHeaderCell_()

        // description
        sink.tableHeaderCell()
        sink.text(bundle.getString(COLUMN_DESCRIPTION_KEY))
        sink.tableHeaderCell_()

        sink.tableRow_()

        CodeNarcResults.Rules.Rule.each() {cnRule ->

            sink.tableRow()

            sink.anchor(cnRule.@name.text())
            sink.anchor_()


            // rules name

            sink.tableCell()
            sink.text(cnRule.@name.text())
            sink.tableCell_()

            // Priority
            sink.tableCell()
            sink.text(cnRule.Description.text())
            sink.tableCell_()


            sink.tableRow_()
        }



        sink.table_()

        sink.section1_()
    }

    public void generateReport() {

        log.info("Reporter Locale is " + this.bundle.getLocale().getLanguage())

        sink.head()
        sink.title()
        sink.text(getReportTitle())
        sink.title_()
        sink.head_()

        sink.body()

        doHeading()

        printSummary()

        printPackageSummary()

        printPackageDetail()

        printRules()

        sink.body_()

        log.debug("Closing up report....................")

        sink.flush()
        sink.close()
    }

    /**
     * Converts Slashes to dots for links
     */
    protected String slashToDot(String string) {
        string.replaceAll("/", ".")
    }

}
