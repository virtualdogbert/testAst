/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 * Some of the setup is derived from the following grails plugings(Appache Licence)
 * https://github.com/groovy/groovy-core/blob/4993b10737881b2491c2daa01526fb15dd889ac5/src/main/org/codehaus/groovy/transform/NewifyASTTransformation.java
 * https://github.com/grails-plugins/grails-redis/tree/master/src/main/groovy/grails/plugins/redis
 */

package com.virtualdogbert.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
/**
 * This is used to skip adding an error handler, at a method level, if the errorHandler is applied at the class level.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MockDomainsASTTransformation extends AbstractASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2) return
        ClassNode beforeNode = new ClassNode(MockDomains.class)

        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {

            MethodNode methodNode = (MethodNode) nodes[1]

            Collection<String> domainClasses = (Collection<String>)methodNode.getDeclaringClass().getModule().getImports().findResults { ImportNode importNode ->
                String className = importNode.getClassName()

                if (DomainClassArtefactHandler.isDomainClass(Class.forName(className))) {
                    return className
                }

                return null
            }

            methodNode.getDeclaringClass().getModule().getStarImports().each { ImportNode importNode ->
                String packageName = importNode.packageName
                String fileName = sourceUnit.classLoader.getResource(packageName.replace('.','/'))?.file

                if(fileName && fileName.contains('domain')) {
                    List<File> files = new File(fileName).listFiles().toList()

                    Collection<String> domains = files.collect { File file ->
                        "$packageName${file.name.split('\\.')[0]}".toString()
                    }

                    domainClasses.addAll(domains)
                }
            }


            BlockStatement methodBody = (BlockStatement) methodNode.getCode()
            List statements = methodBody.getStatements()
            statements.add(0, createMocks(domainClasses))

        }
    }

    private Statement createMocks(Collection<String> classes) {
        ArgumentListExpression arguments = new ArgumentListExpression()
        classes.each { String clazz ->
            ClassNode classNode = new ClassNode(Class.forName(clazz))
            arguments.addExpression((Expression) (new ClassExpression(classNode)))
        }
        Expression thisExpression = new VariableExpression("this")
        Expression mocks = new MethodCallExpression(thisExpression, 'mockDomains', arguments)
        return new ExpressionStatement(mocks)
    }
}
