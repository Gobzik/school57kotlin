package ru.tbank.education.school.lesson7.practise.task1

import kotlin.math.pow

/**
 * Задание 1.
 * Дан список чисел. Необходимо вернуть только чётные числа.
 * Перепиши императивный код ниже в функциональном стиле (используя filter).
 */
fun filterEvenNumbers(numbers: List<Int>): List<Int> {
    return numbers.filter { number -> number % 2 == 0 }
}

/**
 * Задание 2.
 * Преобразуй все имена в нижний регистр.
 * Используй map вместо цикла.
 */
fun lowerCaseNames(names: List<String>): List<String> {
    return names.map { it -> it.toLowerCase() }
}

/**
 * Задание 3.
 * Подсчитай сумму всех чисел в списке.
 * Используй sum() или reduce().
 */
fun sumPrices(prices: List<Int>): Int {
    return prices.sum()
}

/**
 * Задание 4.
 * Найди средний возраст людей.
 * Используй map и average.
 */
data class Person(val name: String, val age: Int)

fun averageAge(people: List<Person>): Double {
    return people.map { it -> it.age }.average()
}

/**
 * Задание 5.
 * Верни сумму только положительных чисел.
 * Используй filter и sum().
 */
fun sumPositive(numbers: List<Int>): Int {
    return numbers.filter { number -> number >= 0 }.sum()
}

/**
 * Задание 6.
 * Сгруппируй слова по первой букве.
 * Используй groupBy.
 */
fun groupWordsByFirstLetter(words: List<String>): Map<Char, List<String>> {
    val grouped = words.groupBy { it[0] }
    return grouped.mapValues { entry -> entry.value.map { it.toString() } }
}

/**
 * Задание 7.
 * Проверь, что все числа положительные.
 * Используй all { it > 0 }.
 */
fun areAllPositive(numbers: List<Int>): Boolean {
    return numbers.all { number -> number > 0 }
}


/**
 * Задание 8.
 * Подсчитай количество слов длиной больше 3 символов.
 * Используй count.
 */
fun countLongWords(words: List<String>): Int {
    return words.filter { word -> word.length > 3 }.size
}

/**
 * Задание 9.
 * Верни квадраты только чётных чисел.
 * Используй цепочку filter → map.
 */
fun squaredEvenNumbers(numbers: List<Int>): List<Int> {
    return numbers.filter { number -> number % 2 == 0 }.map { number -> number.toDouble().pow(2.0).toInt() }
}

/**
 * Задание 10.
 * Удали дубликаты из списка.
 * Используй distinct().
 */
fun uniqueElements(data: List<Int>): List<Int> {
    return data.distinct()
}

/**
 * Задание 11.
 * Объедини два списка.
 * Используй плюс-оператор (+) или plus().
 */
fun mergeLists(list1: List<Int>, list2: List<Int>): List<Int> {
    return list1 + list2
}


/**
 * Задание 12.
 * Сумма транзакций по категории FOOD.
 * Используй filter и sumOf.
 */
data class Transaction(val id: Int, val amount: Int, val category: String)

fun totalFoodExpenses(txs: List<Transaction>): Int {
    return txs.filter { tx -> tx.category == "FOOD" }.sumOf { transaction -> transaction.amount }
}