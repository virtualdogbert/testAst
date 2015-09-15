package com.virtualdogbert.ast

import grails.util.Holders
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * The annotation enforce takes up to 3 closures can injects a call to the enforce method of the enforcerService
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class EnforceASTTransformation extends AbstractASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2) return
        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) nodes[1]
            ClassNode beforeNode = new ClassNode(Enforce.class)

            for (AnnotationNode annotationNode : methodNode.getAnnotations(beforeNode)) {
                ListExpression params = new ListExpression(annotationNode.members.collect{key,value-> value})
                BlockStatement methodBody = (BlockStatement) methodNode.getCode()
                List statements = methodBody.getStatements()
                statements.add(0, createEnforcerCall(params))
                break
            }

            VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit)
            sourceUnit.AST.classes.each {
                scopeVisitor.visitClass(it)
            }
        }
    }

    private Statement createEnforcerCall(ListExpression params) {
        ClassNode holder = new ClassNode(Holders.class)
        Expression context = new StaticMethodCallExpression(holder, "getApplicationContext", ArgumentListExpression.EMPTY_ARGUMENTS)
        Expression service = new MethodCallExpression(context, "getBean", new ConstantExpression('enforcerService'));
        Expression call = new MethodCallExpression(service, 'enforce', new ArgumentListExpression(params))
        return new ExpressionStatement(call)
    }
}
