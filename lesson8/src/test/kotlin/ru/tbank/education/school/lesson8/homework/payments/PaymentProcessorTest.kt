package ru.tbank.education.school.lesson8.homework.payments

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate

class PaymentProcessorTest {

    private lateinit var processor: PaymentProcessor

    @BeforeEach
    fun setUp() {
        processor = PaymentProcessor()
    }

    // ===== ВАЛИДАЦИЯ ВХОДНЫХ ДАННЫХ =====

    @Test
    @DisplayName("Бросает исключение при отрицательной сумме")
    fun `should throw exception for negative amount`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = -100,
                cardNumber = "4111111111111111",
                expiryMonth = 12,
                expiryYear = getNextYear(),
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Amount must be positive", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при нулевой сумме")
    fun `should throw exception for zero amount`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 0,
                cardNumber = "4111111111111111",
                expiryMonth = 12,
                expiryYear = getNextYear(),
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Amount must be positive", exception.message)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "123", "123456789012", "12345678901234567890", "abc", "4111-1111-1111-1111"])
    @DisplayName("Бросает исключение при невалидном номере карты")
    fun `should throw exception for invalid card number`(cardNumber: String) {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = cardNumber,
                expiryMonth = 12,
                expiryYear = getNextYear(),
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Invalid card number format", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при невалидном месяце (0)")
    fun `should throw exception for invalid month zero`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = "4111111111111111",
                expiryMonth = 0,
                expiryYear = getNextYear(),
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Invalid expiry date", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при невалидном месяце (13)")
    fun `should throw exception for invalid month thirteen`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = "4111111111111111",
                expiryMonth = 13,
                expiryYear = getNextYear(),
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Invalid expiry date", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при просроченной карте (прошлый год)")
    fun `should throw exception for expired card last year`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = "4111111111111111",
                expiryMonth = 12,
                expiryYear = LocalDate.now().year - 1,
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Invalid expiry date", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при просроченной карте (прошлый месяц этого года)")
    fun `should throw exception for expired card last month`() {
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year

        val expiredMonth = if (currentMonth > 1) currentMonth - 1 else 12
        val expiredYear = if (currentMonth > 1) currentYear else currentYear - 1

        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = "4111111111111111",
                expiryMonth = expiredMonth,
                expiryYear = expiredYear,
                currency = "USD",
                customerId = "customer123"
            )
        }
        assertEquals("Invalid expiry date", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при пустой валюте")
    fun `should throw exception for empty currency`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = "4111111111111111",
                expiryMonth = 12,
                expiryYear = getNextYear(),
                currency = "",
                customerId = "customer123"
            )
        }
        assertEquals("Currency cannot be empty", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при пустом customerId")
    fun `should throw exception for empty customerId`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.processPayment(
                amount = 100,
                cardNumber = "4111111111111111",
                expiryMonth = 12,
                expiryYear = getNextYear(),
                currency = "USD",
                customerId = ""
            )
        }
        assertEquals("Customer ID cannot be blank", exception.message)
    }

    // ===== ПРОВЕРКА ПОДОЗРИТЕЛЬНЫХ КАРТ =====

    @ParameterizedTest
    @ValueSource(strings = ["4444111111111111", "5555111111111111", "1111111111111111", "9999111111111111"])
    @DisplayName("Блокирует подозрительные карты по префиксу")
    fun `should block suspicious cards by prefix`(cardNumber: String) {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = cardNumber,
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("REJECTED", result.status)
        assertTrue(result.message.contains("fraud", ignoreCase = true))
    }

    @Test
    @DisplayName("Блокирует карту с невалидной контрольной суммой Луна")
    fun `should block card with invalid luhn check`() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4111111111111112",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("REJECTED", result.status)
        assertTrue(result.message.contains("fraud", ignoreCase = true))
    }

    @Test
    @DisplayName("Пропускает карту с валидной контрольной суммой Луна")
    fun `should accept card with valid luhn check`() {
        val result = processor.processPayment(
            amount = 50,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertNotEquals("REJECTED", result.status)
    }

    // ===== КОНВЕРТАЦИЯ ВАЛЮТ =====

    @ParameterizedTest
    @CsvSource(
        "USD, 100",
        "EUR, 100",
        "GBP, 100",
        "JPY, 100",
        "RUB, 100",
        "usd, 100",
        "Eur, 100"
    )
    @DisplayName("Корректно обрабатывает валюты")
    fun `should handle currencies correctly`(currency: String, amount: Int) {
        val result = processor.processPayment(
            amount = amount,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = currency,
            customerId = "customer123"
        )

        assertNotEquals("REJECTED", result.status)
    }

    @Test
    @DisplayName("Использует USD по умолчанию для неподдерживаемой валюты")
    fun `should use USD as default for unsupported currency`() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "CAD",
            customerId = "customer123"
        )

        assertNotEquals("REJECTED", result.status)
    }

    // ===== ЛОГИКА ПЛАТЕЖНОГО ШЛЮЗА =====

    @Test
    @DisplayName("Успешный платеж")
    fun `should process successful payment`() {
        val result = processor.processPayment(
            amount = 50,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
        assertEquals("Payment completed", result.message)
    }

    @Test
    @DisplayName("Ошибка 'превышен лимит транзакции'")
    fun `should handle transaction limit exceeded`() {
        val result = processor.processPayment(
            amount = 100_001,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("FAILED", result.status)
        assertEquals("Transaction limit exceeded", result.message)
    }

    @Test
    @DisplayName("Ошибка 'таймаут шлюза'")
    fun `should handle gateway timeout`() {
        val result = processor.processPayment(
            amount = 170,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("FAILED", result.status)
        assertEquals("Gateway timeout", result.message)
    }

    @Test
    @DisplayName("Неизвестная ошибка шлюза для не-VISA карт")
    fun `should handle unknown gateway error for non-visa cards`() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "30000000000004",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )
    }

    // ===== РАСЧЕТ СКИДОК ЛОЯЛЬНОСТИ =====

    @Test
    @DisplayName("Бросает исключение при отрицательной базовой сумме для скидки")
    fun `should throw exception for negative base amount in loyalty discount`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.calculateLoyaltyDiscount(points = 1000, baseAmount = -100)
        }
        assertEquals("Base amount must be positive", exception.message)
    }

    @Test
    @DisplayName("Бросает исключение при нулевой базовой сумме для скидки")
    fun `should throw exception for zero base amount in loyalty discount`() {
        val exception = assertThrows<IllegalArgumentException> {
            processor.calculateLoyaltyDiscount(points = 1000, baseAmount = 0)
        }
        assertEquals("Base amount must be positive", exception.message)
    }

    @ParameterizedTest
    @CsvSource(
        "100, 10000, 20",
        "30000, 10000, 5000",
        "20000, 8000, 3000",
        "10000, 5000, 1500",
        "5000, 3000, 500",
        "20000, 2000, 1500",
        "2000, 1000, 100",
        "15000, 500, 500",
        "1000, 100, 0",
        "500, 499, 0"
    )
    @DisplayName("Корректно рассчитывает скидки лояльности")
    fun `should calculate loyalty discounts correctly`(baseAmount: Int, points: Int, expectedDiscount: Int) {
        val discount = processor.calculateLoyaltyDiscount(points, baseAmount)
        assertEquals(expectedDiscount, discount)
    }

    // ===== ПАКЕТНАЯ ОБРАБОТКА =====

    @Test
    @DisplayName("Возвращает пустой список для пустого ввода")
    fun `should return empty list for empty input`() {
        val results = processor.bulkProcess(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    @DisplayName("Обрабатывает смешанные валидные и невалидные платежи")
    fun `should process mixed valid and invalid payments`() {
        val payments = listOf(
            PaymentData(50, "4111111111111111", 12, getNextYear(), "USD", "customer1"),
            PaymentData(-100, "4111111111111111", 12, getNextYear(), "USD", "customer2"),
            PaymentData(100, "5500111111111111", 12, getNextYear(), "USD", "customer3"),
            PaymentData(50, "4111111111111111", 12, getNextYear(), "USD", "customer4")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(4, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("REJECTED", results[1].status)
        assertEquals("SUCCESS", results[3].status)
    }

    @Test
    @DisplayName("Обрабатывает все типы ошибок в пакетной обработке")
    fun `should handle all error types in bulk processing`() {
        val payments = listOf(
            PaymentData(50, "4111111111111111", 12, getNextYear(), "USD", "customer1"),
            PaymentData(100, "", 12, getNextYear(), "USD", "customer2"),
            PaymentData(100, "4444111111111111", 12, getNextYear(), "USD", "customer3"),
            PaymentData(100_001, "4111111111111111", 12, getNextYear(), "USD", "customer4")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(4, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("REJECTED", results[1].status)
        assertEquals("FAILED", results[3].status)
    }

    // ===== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ ПОКРЫТИЯ =====

    @Test
    @DisplayName("Проверяет все ветки проверки срока действия карты")
    fun `should test all expiry date validation branches`() {
        val currentYear = LocalDate.now().year
        val currentMonth = LocalDate.now().monthValue

        assertThrows<IllegalArgumentException> {
            processor.processPayment(100, "4111111111111111", 12, currentYear - 1, "USD", "customer1")
        }

        if (currentMonth > 1) {
            assertThrows<IllegalArgumentException> {
                processor.processPayment(100, "4111111111111111", currentMonth - 1, currentYear, "USD", "customer2")
            }
        }

        assertDoesNotThrow {
            processor.processPayment(100, "4111111111111111", currentMonth, currentYear, "USD", "customer3")
        }

        if (currentMonth < 12) {
            assertDoesNotThrow {
                processor.processPayment(100, "4111111111111111", currentMonth + 1, currentYear, "USD", "customer4")
            }
        }

        assertDoesNotThrow {
            processor.processPayment(100, "4111111111111111", 1, currentYear + 1, "USD", "customer5")
        }
    }

    @Test
    @DisplayName("Тестирует все ветки обработки ошибок шлюза")
    fun `should test all gateway error handling branches`() {
        val result3 = processor.processPayment(100001, "4111111111111111", 12, getNextYear(), "USD", "customer3")
        assertEquals("FAILED", result3.status)
        assertEquals("Transaction limit exceeded", result3.message)

        val result4 = processor.processPayment(170, "4111111111111111", 12, getNextYear(), "USD", "customer4")
        assertEquals("FAILED", result4.status)
        assertEquals("Gateway timeout", result4.message)
    }

    @Test
    @DisplayName("Тестирует успешный платеж с другой VISA картой")
    fun `should process successful payment with other visa card`() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4222222222222",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
        assertEquals("Payment completed", result.message)
    }

    @Test
    @DisplayName("Тестирует все уровни скидок лояльности с граничными значениями")
    fun `should test all loyalty discount levels with boundaries`() {
        assertEquals(0, processor.calculateLoyaltyDiscount(499, 10000))
        assertEquals(500, processor.calculateLoyaltyDiscount(500, 10000))
        assertEquals(500, processor.calculateLoyaltyDiscount(500, 20000))

        assertEquals(200, processor.calculateLoyaltyDiscount(2000, 2000))
        assertEquals(1500, processor.calculateLoyaltyDiscount(2000, 15000))
        assertEquals(1500, processor.calculateLoyaltyDiscount(2000, 20000))
        assertEquals(499, processor.calculateLoyaltyDiscount(4999, 4990))

        assertEquals(750, processor.calculateLoyaltyDiscount(5000, 5000))
        assertEquals(3000, processor.calculateLoyaltyDiscount(5000, 20000))
        assertEquals(3000, processor.calculateLoyaltyDiscount(5000, 25000))

        assertEquals(2000, processor.calculateLoyaltyDiscount(10000, 10000))
        assertEquals(5000, processor.calculateLoyaltyDiscount(10000, 25000))
        assertEquals(5000, processor.calculateLoyaltyDiscount(10000, 30000))
    }

    @Test
    @DisplayName("Тестирует пакетную обработку с различными исключениями")
    fun `should handle various exceptions in bulk processing`() {
        val payments = listOf(
            PaymentData(50, "4111111111111111", 12, getNextYear(), "USD", "customer1"),
            PaymentData(-100, "4111111111111111", 12, getNextYear(), "USD", "customer2"),
            PaymentData(100, "1234567890123456789", 12, getNextYear(), "USD", "customer3"),
        )

        val results = processor.bulkProcess(payments)

        assertEquals(3, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("REJECTED", results[1].status)
        assertEquals("REJECTED", results[2].status)
    }

    @Test
    @DisplayName("Тестирует все ветки алгоритма Луна")
    fun `should test all luhn algorithm branches`() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4111111111111211",
            expiryMonth = 12,
            expiryYear = getNextYear(),
            currency = "USD",
            customerId = "customer123"
        )
        assertNotNull(result)
    }

    @Test
    @DisplayName("Тестирует все сценарии конвертации валют")
    fun `should test all currency conversion scenarios`() {
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "RUB", "CAD")

        currencies.forEach { currency ->
            assertDoesNotThrow {
                processor.processPayment(
                    amount = 100,
                    cardNumber = "4111111111111111",
                    expiryMonth = 12,
                    expiryYear = getNextYear(),
                    currency = currency,
                    customerId = "customer123"
                )
            }
        }
    }

    private fun getNextYear(): Int {
        return LocalDate.now().year + 1
    }
}