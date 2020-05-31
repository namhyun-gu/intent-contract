/*
 * Copyright 2020 Namhyun, Gu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.namhyun.intentcontract.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

fun ProcessingEnvironment.print(message: String, kind: Diagnostic.Kind = Diagnostic.Kind.NOTE) =
    messager.printMessage(kind, message)

fun ProcessingEnvironment.printWarn(message: String) = print(message, Diagnostic.Kind.WARNING)

fun ProcessingEnvironment.printError(message: String) = print(message, Diagnostic.Kind.ERROR)

fun Element.name() = simpleName.toString()

fun TypeElement.fullName() = this.qualifiedName.toString()

fun String.camelToSnakeCase(): String {
    return "[A-Z]".toRegex().replace(this) { "_${it.value}" }
}

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
