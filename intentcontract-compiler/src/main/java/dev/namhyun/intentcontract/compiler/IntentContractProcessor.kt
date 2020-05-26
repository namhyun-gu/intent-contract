package dev.namhyun.intentcontract.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import dev.namhyun.intentcontract.Extra
import dev.namhyun.intentcontract.IntentTarget
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
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
            val intentTargetSpec = buildIntentTargets(targetMap)
            writeTypeSpec(intentTargetSpec)
            val intentContractSpec = buildIntentContracts(targetMap)
            writeTypeSpec(intentContractSpec)
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
        val builder = TypeSpec.objectBuilder("IntentTargets")

        val properties = mutableListOf<PropertySpec>()
        targetMap.forEach { properties.addAll(buildConstants(it.key, it.value)) }
        builder.addProperties(properties)

        val funcSpecs = targetMap.map { buildTargetFunc(it.key, it.value) }
        builder.addFunctions(funcSpecs)

        return builder.build()
    }

    private fun buildTargetFunc(intentTarget: TypeElement, extras: List<Element>): FunSpec {
        val targetName = intentTarget.simpleName.toString()
        val builder = FunSpec.builder(targetName.decapitalize())
            .returns(intentClass)

        builder.addParameter("context", contextClass)
        extras.forEach {
            val extraName = it.simpleName.toString()
            builder.addParameter(extraName, it.asType().asTypeName().asKotlinType())
        }

        builder.addStatement(
            "val intent = %T(%L, %L)",
            intentClass,
            "context",
            "${intentTarget.qualifiedName}::class.java"
        )
        extras.forEach {
            val extraName = it.simpleName.toString()
            builder.addStatement(
                "intent.putExtra(%L, %L)",
                getConstantName(targetName, extraName),
                extraName
            )
        }
        builder.addStatement("return %L", "intent")
        return builder.build()
    }

    private fun buildConstants(
        intentTarget: TypeElement,
        extras: List<Element>
    ): List<PropertySpec> {
        val targetName = intentTarget.simpleName.toString()
        return extras.map {
            val extraName = it.simpleName.toString()
            val constantName = getConstantName(targetName, extraName)
            PropertySpec.builder(constantName, String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", constantName.toLowerCase())
                .build()
        }
    }

    private fun buildIntentContracts(targetMap: Map<TypeElement, List<Element>>): TypeSpec {
        val builder = TypeSpec.objectBuilder("IntentContracts")
        val contactFuncBuilder = FunSpec.builder("contact")
            .addParameter("context", contextClass)

        targetMap.keys.forEach {
            val name = it.simpleName.toString()
            val contractorName = getContractName(name)
            val contractorClass = ClassName("dev.namhyun.intentcontract.gen", contractorName)
            contactFuncBuilder
                .beginControlFlow("if (%L is %T)", "context", it.asType())
                .addStatement("%T.contact(%L)", contractorClass, "context")
                .endControlFlow()
        }

        builder.addFunction(contactFuncBuilder.build())
        return builder.build()
    }

    private fun buildTargetContracts(targetMap: Map<TypeElement, List<Element>>): List<TypeSpec> {
        return targetMap.map {
            val targetName = it.key.simpleName.toString()
            val builder = TypeSpec.objectBuilder(getContractName(targetName))
            val contactFuncBuilder = FunSpec.builder("contact")
                .addParameter("activity", it.key.asClassName())
                .addStatement("val intent = %L.intent", "activity")

            for (element in it.value) {
                val elementName = element.simpleName.toString()
                val constantName = getConstantName(targetName, elementName)
                contactFuncBuilder
                    .beginControlFlow("if (intent.hasExtra(IntentTargets.$constantName))")
                    .addStatement(
                        "activity.%L = %L.${getExtraMethodLiteral(
                            element.asType().asTypeName(),
                            getConstantName(targetName, elementName)
                        )}",
                        elementName,
                        "intent"
                    )
                    .endControlFlow()
            }

            builder.addFunction(contactFuncBuilder.build())
            builder.build()
        }
    }

    // TODO Support Array type extra
    private fun getExtraMethodLiteral(type: TypeName, extraName: String): String {
        val extraConstant = "IntentTargets.$extraName"
        return when (type) {
            BOOLEAN -> "getBooleanExtra($extraConstant, false)"
            BYTE -> "getByteExtra($extraConstant, 0)"
            CHAR -> "getCharExtra($extraConstant, '\\u0000')"
            DOUBLE -> "getDoubleExtra($extraConstant, 0.0)"
            FLOAT -> "getFloatExtra($extraConstant, 0.0f)"
            INT -> "getIntExtra($extraConstant, 0)"
            LONG -> "getLongExtra($extraConstant, 0)"
            SHORT -> "getShortExtra($extraConstant, 0)"
            else -> {
                return when (type.toString()) {
                    "java.lang.String" -> "getStringExtra($extraConstant)"
                    else -> {
                        processingEnv.printError("Not support extra type. $type")
                        ""
                    }
                }
            }
        }
    }

    private fun getConstantName(targetName: String, extraName: String): String =
        "EXTRA_${targetName.toUpperCase()}_${extraName.toUpperCase()}"

    private fun getContractName(targetName: String): String = "${targetName}_Contract"
}
