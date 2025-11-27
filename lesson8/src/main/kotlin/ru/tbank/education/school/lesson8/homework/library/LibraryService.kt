package ru.tbank.education.school.lesson8.homework.library

class LibraryService {
    private val books = mutableMapOf<String, Book>()
    private val borrowedBooks = mutableMapOf<String, String>()
    private val borrowerFines = mutableMapOf<String, Int>()

    fun addBook(book: Book) {
        if (books.containsKey(book.isbn)) {
            throw IllegalArgumentException("Book already exists")
        }
        books[book.isbn] = book
    }

    fun borrowBook(isbn: String, borrower: String) {
        if (!books.containsKey(isbn)) {
            throw IllegalArgumentException("Book doesn't exists")
        }
        if (!isAvailable(isbn)) {
            throw IllegalArgumentException("Book already borrowed")
        }
        if (hasOutstandingFines(borrower)) {
            throw IllegalArgumentException("Borrower has outstanding fines")
        }
        borrowedBooks[isbn] = borrower
    }

    fun returnBook(isbn: String) {
        if (isAvailable(isbn)) {
            throw IllegalArgumentException("Book is not borrowed")
        }
        borrowedBooks.remove(isbn)
    }

    fun isAvailable(isbn: String): Boolean {
        return !borrowedBooks.containsKey(isbn)
    }

    fun calculateOverdueFine(isbn: String, daysOverdue: Int): Int {
        if (isAvailable(isbn)) {
            throw IllegalArgumentException("Book is not currently borrowed")
        }
        if (daysOverdue <= 10) {
            return 0
        }
        return (daysOverdue - 10) * 60
    }

    // Новый метод для начисления штрафа читателю
    fun addFineToBorrower(isbn: String, daysOverdue: Int) {
        if (isAvailable(isbn)) {
            throw IllegalArgumentException("Book is not currently borrowed")
        }
        val borrower = borrowedBooks[isbn] ?: return
        val fine = calculateOverdueFine(isbn, daysOverdue)
        if (fine > 0) {
            borrowerFines[borrower] = (borrowerFines[borrower] ?: 0) + fine
        }
    }

    // Метод для проверки штрафа конкретного читателя
    fun getBorrowerFine(borrower: String): Int {
        return borrowerFines[borrower] ?: 0
    }

    private fun hasOutstandingFines(borrower: String): Boolean {
        return (borrowerFines[borrower] ?: 0) > 0
    }
}