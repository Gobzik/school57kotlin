@file:Suppress("IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE")

package ru.tbank.education.school.lesson7.practise.task1

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Задание: Из ленты событий выдели:
 *  - первое событие типа ERROR,
 *  - последние два события типа LOGIN,
 *  - первые N событий за сегодня.
 *
 * Верни тройку: Triple<Event?, List<Event>, List<Event>>, где по порядку будет:
 *  - первое событие типа ERROR,
 *  - последние два события типа LOGIN,
 *  - первые N событий за сегодня.
 *
 */
enum class EventType { LOGIN, LOGOUT, ERROR, INFO }
data class Event(val type: EventType, val date: LocalDateTime)

fun sliceEvents(
    events: List<Event>,
    nToday: Int
): Triple<Event?, List<Event>, List<Event>> {
    val firstError = events.firstOrNull { it.type == EventType.ERROR }

    val loginEvents = events.filter { it.type == EventType.LOGIN }
    val lastTwoLogins = loginEvents.takeLast(2)

    val today = LocalDate.now()
    val todayEvents = events.filter { it.date.toLocalDate() == today }
    val firstNToday = todayEvents.take(nToday)

    return Triple(firstError, lastTwoLogins, firstNToday)
}