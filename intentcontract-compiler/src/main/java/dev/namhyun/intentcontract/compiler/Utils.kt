package dev.namhyun.intentcontract.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

fun ProcessingEnvironment.print(message: String, kind: Diagnostic.Kind = Diagnostic.Kind.NOTE) =
    messager.printMessage(kind, message)

fun ProcessingEnvironment.printWarn(message: String) = print(message, Diagnostic.Kind.WARNING)

fun ProcessingEnvironment.printError(message: String) = print(message, Diagnostic.Kind.ERROR)

internal fun TypeName.asKotlinType() =
    when (this.toString()) {
        "java.lang.String" -> ClassName("kotlin", "String")
        "java.lang.Integer" -> ClassName("kotlin", "Int")
        "java.lang.Float" -> ClassName("kotlin", "Float")
        "java.lang.Double" -> ClassName("kotlin", "Double")
        "java.lang.Char" -> ClassName("kotlin", "Char")
        "java.lang.Boolean" -> ClassName("kotlin", "Boolean")
        else -> this
    }