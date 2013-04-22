/*
 * Copyright (C) 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


File codenarcHtml =  new File(basedir, 'target/site/codenarc.html')

assert codenarcHtml.exists()

File codenarcXdoc = new File(basedir, 'target/CodeNarc.xml')
assert codenarcXdoc.exists()


def xmlSlurper = new XmlSlurper()
xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
xmlSlurper.setFeature("http://xml.org/sax/features/namespaces", false)

def path = xmlSlurper.parse( codenarcHtml )


println '***************************'
println "Checking HTML file"
println '***************************'

def codenarcInfo = path.body.div.findAll {it.@id == 'bodyColumn'}.div[1].table.tr[1]

def codenarcFiles = codenarcInfo.td[0].toInteger()
println "File Count is ${codenarcFiles}"

def codenarcViolations = codenarcInfo.td[1].toInteger()
println "Files With Violations is ${codenarcViolations}"

def codenarcBugs = codenarcInfo.td[2].toInteger()
println "Total Bug Count is ${codenarcBugs}"

def codenarcPriority1 = codenarcInfo.td[3].toInteger()
println "Priority1 Bug Count is ${codenarcPriority1}"

def codenarcPriority2 = codenarcInfo.td[4].toInteger()
println "Priority2 Bug Count is ${codenarcPriority2}"

def codenarcPriority3 = codenarcInfo.td[5].toInteger()
println "Priority3 Bug Count is ${codenarcPriority3}"


println '***************************'
println "Checking xml file"
println '***************************'

path = new XmlSlurper().parse(new File(basedir, 'target/CodeNarc.xml'))

allNodes = path.findAll {it.name() == 'CodeNarc'}.'PackageSummary'

def xmlBugFiles = allNodes.@totalFiles.toInteger()
println "File Count is ${xmlBugFiles}"

def xmlViolations = allNodes.@filesWithViolations.toInteger()
println "Files With Violations is ${xmlViolations}"

def xmlPriority1 = allNodes.@priority1.toInteger()
println "Priority1 Bug Count is ${xmlPriority1}"

def xmlPriority2 = allNodes.@priority2.toInteger()
println "Priority2 Bug Count is ${xmlPriority2}"

def xmlPriority3 = allNodes.@priority3.toInteger()
println "Priority3 Bug Count is ${xmlPriority3}"


assert codenarcFiles == xmlBugFiles
assert codenarcViolations == xmlViolations
assert codenarcBugs == (xmlPriority1 + xmlPriority2 + xmlPriority3 )
assert codenarcPriority1 == xmlPriority1
assert codenarcPriority2 == xmlPriority2
assert codenarcPriority3 == xmlPriority3
