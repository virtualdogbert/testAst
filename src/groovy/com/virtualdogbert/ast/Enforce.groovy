package com.virtualdogbert.ast

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target


@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass("com.virtualdogbert.ast.EnforceASTTransformation")
public @interface Enforce {
    Class value() default {false};
    Class failure() default {true};
    Class success() default {true};
}
