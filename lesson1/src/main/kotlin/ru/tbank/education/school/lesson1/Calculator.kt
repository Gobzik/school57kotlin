package ru.tbank.education.school.lesson1

/**
 * Метод для вычисления простых арифметических операций.
 */
fun calculate(a: Double, b: Double, operation: OperationType = OperationType.ADD): Double? {
    return when (operation) {
        OperationType.ADD -> a + b
        OperationType.SUBTRACT -> a - b
        OperationType.MULTIPLY -> a * b
        OperationType.DIVIDE -> {
            if (b == 0.0) null
            else a / b
        }
        else -> throw IllegalArgumentException("Unknown operation: $operation")
    }
}

/**
 * Функция вычисления выражения, представленного строкой
 * @return результат вычисления строки или null, если вычисление невозможно
 * @sample "5 * 2".calculate()
 */
@Suppress("ReturnCount")
fun String.calculate(): Double? {
    this.let {
        val parts = this.split(" ")
        if (parts.size < 3) return null

        val a = parts[0].toDoubleOrNull() ?: return null
        val operator = parts[1]
        val c = parts[2].toDoubleOrNull() ?: return null

        return when (operator) {
            "+" -> a + c
            "-" -> a - c
            "*" -> a * c
            "/" -> {
                if (c == 0.0) null
                else a / c
            }
            else -> null // Неизвестный оператор
        }
    }
    return null
}