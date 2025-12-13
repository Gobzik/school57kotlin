package ru.tbank.education.school.lesson10.homework

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

object DocumentationGenerator {

    fun generateDoc(obj: Any): String {
        val kClass = obj::class

        if (kClass.findAnnotation<InternalApi>() != null) {
            return "Документация скрыта (InternalApi)."
        }

        val docClass = kClass.findAnnotation<DocClass>()
            ?: return "Нет документации для класса."

        val sb = StringBuilder()

        sb.appendLine("=== Документация: ${kClass.simpleName} ===")
        sb.appendLine("Описание: ${docClass.description}")
        sb.appendLine("Автор: ${docClass.author}")
        sb.appendLine("Версия: ${docClass.version}")
        sb.appendLine()

        val internalPropertyNames = kClass.memberProperties
            .filter { it.findAnnotation<InternalApi>() != null }
            .map { it.name }
            .toSet()

        val visibleProperties = kClass.memberProperties
            .filterNot { it.name in internalPropertyNames }

        if (visibleProperties.isNotEmpty()) {
            sb.appendLine("--- Свойства ---")
            for (prop in visibleProperties) {
                sb.appendLine("- ${prop.name}")

                prop.findAnnotation<DocProperty>()?.let {
                    sb.appendLine("  Описание: ${it.description}")
                    if (it.example.isNotBlank()) {
                        sb.appendLine("  Пример: ${it.example}")
                    }
                }
                sb.appendLine()
            }
        }

        val excludedMethodNames = setOf("toString", "equals", "hashCode", "copy")

        val methods = kClass.memberFunctions
            .filterNot {
                it.name in excludedMethodNames ||
                        it.name.startsWith("component") ||
                        it.findAnnotation<InternalApi>() != null ||
                        it.javaMethod?.declaringClass == Any::class.java
            }

        if (methods.isNotEmpty()) {
            sb.appendLine("--- Методы ---")
            for (method in methods) {

                val params = method.parameters
                    .filter { it.kind == KParameter.Kind.VALUE }
                    .filterNot { it.name in internalPropertyNames }

                val signature = params.joinToString(", ") {
                    "${it.name}: ${it.type}"
                }

                sb.appendLine("- ${method.name}($signature)")

                val docMethod = method.findAnnotation<DocMethod>()
                sb.appendLine(
                    "  Описание: ${docMethod?.description ?: "Нет описания"}"
                )

                if (params.isNotEmpty()) {
                    sb.appendLine("  Параметры:")
                    for (param in params) {
                        val desc = param.findAnnotation<DocParam>()?.description
                            ?: "Нет описания"
                        sb.appendLine("    - ${param.name}: $desc")
                    }
                }

                sb.appendLine(
                    "  Возвращает: ${docMethod?.returns ?: "Нет описания"}"
                )
                sb.appendLine()
            }
        }

        return sb.toString().trimEnd()
    }
}
