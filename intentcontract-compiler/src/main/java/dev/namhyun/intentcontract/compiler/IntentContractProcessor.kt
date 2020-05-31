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

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import dev.namhyun.intentcontract.annotations.Extra
import dev.namhyun.intentcontract.annotations.IntentTarget
import dev.namhyun.intentcontract.annotations.Optional
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class IntentContractProcessor : AbstractProcessor() {
    private lateinit var elementUtils: Elements
    private val targetMap = mutableMapOf<TypeElement, MutableList<Element>>()

    private val intentClass = ClassName.bestGuess("android.content.Intent")
    private val contextClass = ClassName.bestGuess("android.content.Context")

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        findIntentTargets(roundEnv)
        findExtras(roundEnv)
        if (roundEnv.processingOver()) {
            val intentTargets = buildIntentTargets(targetMap)
            writeTypeSpec(intentTargets)
            val intentContracts = buildIntentContracts(targetMap)
            writeTypeSpec(intentContracts)
            val targetContracts = buildTargetContracts(targetMap)
            targetContracts.forEach {
                writeTypeSpec(it)
            }
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(IntentTarget::class.java.name, Extra::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    private fun findIntentTargets(roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(IntentTarget::class.java).forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.printError(
                    "IntentTarget must be class. ${it.simpleName} is ${it.kind}"
                )
            } else {
                targetMap[it as TypeElement] = mutableListOf()
            }
        }
    }

    private fun findExtras(roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(Extra::class.java).forEach {
            if (it.kind != ElementKind.FIELD) {
                processingEnv.printError(
                    "Extra must be class. ${it.simpleName} is ${it.kind}"
                )
            } else {
                val enclosingElement = it.enclosingElement as TypeElement
                if (targetMap.containsKey(enclosingElement)) {
                    targetMap[enclosingElement]!!.add(it)
                } else {
                    processingEnv.printError(
                        "Extra must be in IntentTarget class."
                    )
                }
            }
        }
    }

    private fun writeTypeSpec(typeSpec: TypeSpec) {
        val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
        FileSpec.builder("dev.namhyun.intentcontract.gen", typeSpec.name!!)
            .addType(typeSpec)
            .build()
            .writeTo(File(kaptKotlinGeneratedDir!!))
    }

    private fun buildIntentTargets(targetMap: Map<TypeElement, List<Element>>): TypeSpec {
        return TypeSpec.objectBuilder("IntentTargets")
            .addFunctions(targetMap.map { (target, extras) ->
                buildTargetFunc(target, extras)
            })
            .build()
    }

    private fun buildTargetFunc(target: TypeElement, extras: List<Element>): FunSpec {
        val targetName = target.name()
        val builder = FunSpec.builder(targetName.decapitalize())
            .returns(intentClass)

        builder.addParameter("context", contextClass)
        for (extra in extras) {
            val name = extra.name()
            val isOptional = extra.getAnnotation(Optional::class.java) != null
            builder.addParameter(
                name,
                extra.asType().asTypeName().asKotlinType().copy(nullable = isOptional)
            )
        }

        builder.addStatement(
            "val intent = %T(%L, %L)",
            intentClass,
            "context",
            "${target.fullName()}::class.java"
        )

        for (extra in extras) {
            val name = extra.name()
            val isOptional = extra.getAnnotation(Optional::class.java) != null
            if (isOptional) {
                builder.beginControlFlow("if (%L != null)", name)
            }
            builder.addStatement(
                "intent.putExtra(%L.%L, %L)",
                getContractName(targetName),
                getConstantName(name),
                name
            )
            if (isOptional) {
                builder.endControlFlow()
            }
        }
        builder.addStatement("return %L", "intent")
        return builder.build()
    }

    private fun buildConstants(
        extras: List<Element>
    ): List<PropertySpec> {
        return extras.map {
            val name = getConstantName(it.name())
            PropertySpec.builder(name, String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", name.toLowerCase())
                .build()
        }
    }

    private fun buildIntentContracts(targetMap: Map<TypeElement, List<Element>>): TypeSpec {
        val builder = TypeSpec.objectBuilder("IntentContracts")
        val contactFuncBuilder = FunSpec.builder("contact")
            .addParameter("target", contextClass)

        for (target in targetMap.keys) {
            val name = target.name()
            val contractorName = getContractName(name)
            val contractorClass = ClassName("dev.namhyun.intentcontract.gen", contractorName)
            contactFuncBuilder
                .beginControlFlow("if (%L is %T)", "target", target.asType())
                .addStatement("%T.contact(%L)", contractorClass, "target")
                .endControlFlow()
        }

        builder.addFunction(contactFuncBuilder.build())
        return builder.build()
    }

    private fun buildTargetContracts(targetMap: Map<TypeElement, List<Element>>): List<TypeSpec> {
        return targetMap.map { (target, extras) ->
            val targetName = target.name()
            val builder = TypeSpec.objectBuilder(getContractName(targetName))
                .addProperties(buildConstants(extras))

            val funcBuilder = FunSpec.builder("contact")
                .addParameter("target", target.asClassName())
                .addStatement("val intent = %L.intent", "target")

            for (extra in extras) {
                val name = extra.name()
                val methodLiteral = getExtraMethodLiteral(
                    extra.asType().asTypeName(),
                    getConstantName(name)
                )
                funcBuilder
                    .beginControlFlow(
                        "if (intent.hasExtra(%L))",
                        getConstantName(name)
                    )
                    .addStatement(
                        "target.%L = %L.$methodLiteral",
                        name,
                        "intent"
                    )
                    .endControlFlow()
            }
            builder.addFunction(funcBuilder.build())
            builder.build()
        }
    }

    // TODO Support Array type extra
    private fun getExtraMethodLiteral(
        type: TypeName,
        extraName: String
    ): String {
        return when (type) {
            BOOLEAN -> "getBooleanExtra($extraName, false)"
            BYTE -> "getByteExtra($extraName, 0)"
            CHAR -> "getCharExtra($extraName, '\\u0000')"
            DOUBLE -> "getDoubleExtra($extraName, 0.0)"
            FLOAT -> "getFloatExtra($extraName, 0.0f)"
            INT -> "getIntExtra($extraName, 0)"
            LONG -> "getLongExtra($extraName, 0)"
            SHORT -> "getShortExtra($extraName, 0)"
            else -> {
                return when (type.toString()) {
                    "java.lang.String" -> "getStringExtra($extraName)"
                    else -> {
                        processingEnv.printError("Not support extra type. $type")
                        ""
                    }
                }
            }
        }
    }

    private fun getConstantName(extraName: String): String =
        "EXTRA_${extraName.camelToSnakeCase().toUpperCase()}"

    private fun getContractName(targetName: String): String = "${targetName}_Contract"
}
