package schedule

import java.time.LocalDate
import java.time.format.DateTimeParseException

private val datePattern = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")

private val fieldOrder = listOf(
    ScheduleField.DATE,
    ScheduleField.TYPE,
    ScheduleField.TOPIC,
    ScheduleField.LOCATION,
    ScheduleField.TEACHERS,
)

fun parseScheduleLine(number: Long, line: String): ParseResult {
    require(number >= 1) {
        "line number must be positive"
    }

    fun reject(reason: RejectionReason): ParseResult.Rejected {
        return ParseResult.Rejected(
            lineNumber = number,
            reason = reason,
        )
    }
    if ('|' !in line) {
        return reject(RejectionReason.InvalidSeparator)
    }
    val fields = line.split('|')
    val structureViolation = findStructureViolation(fields)
    if (structureViolation != null) {
        return reject(structureViolation)
    }
    val dateText = fields[0]
    val typeText = fields[1]
    val topic = fields[2]
    val location = fields[3]
    val teachersText = fields[4]

    val date = parseFileDate(dateText)
        ?: return reject(RejectionReason.InvalidDate(dateText))
    val type = SessionType.entries.firstOrNull {
        it.name == typeText
    } ?: return reject(
        RejectionReason.UnknownSessionType(typeText),
    )
    val teachers = teachersText.split(',')
    if (type == SessionType.EXAM) {
        val count = teachers.count {
            it.isNotBlank() && it != "-"
        }
        if (count < 2) {
            return reject(
                RejectionReason.NotEnoughTeachers(count),
            )
        }
    }
    val textViolation = findTextViolation(fields, teachers)
    if (textViolation != null) {
        return reject(textViolation)
    }
    val sessionViolation = if (type == SessionType.CANCELLED) {
        findCancelledViolation(location, teachersText)
    } else {
        findActiveSessionViolation(location, teachers)
    }
    if (sessionViolation != null) {
        return reject(sessionViolation)
    }
    val session = createSession(date, type, topic, location, teachers)
    return ParseResult.Success(session)
}

private fun findStructureViolation(fields: List<String>): RejectionReason? {
    if (fields.size != 5) {
        return RejectionReason.WrongFieldCount(fields.size)
    }
    for (index in fields.indices) {
        if (fields[index].isBlank()) {
            return RejectionReason.EmptyRequiredField(fieldOrder[index])
        }
    }
    return null
}

private fun findTextViolation(
    fields: List<String>,
    teachers: List<String>,
): RejectionReason? {
    for (index in fields.indices) {
        val value = fields[index]
        if (value.any { Character.isISOControl(it) }) {
            return RejectionReason.ControlCharacters(
                field = fieldOrder[index],
                value = value,
            )
        }
    }
    for (index in fields.indices) {
        val value = fields[index]

        if (value != value.trim()) {
            return RejectionReason.SurroundingWhitespace(
                field = fieldOrder[index],
                value = value,
            )
        }
    }
    for (teacher in teachers) {
        if (teacher != teacher.trim()) {
            return RejectionReason.SurroundingWhitespace(
                field = ScheduleField.TEACHERS,
                value = teacher,
            )
        }
    }

    return null
}

private fun findCancelledViolation(
    location: String,
    teachersText: String,
): RejectionReason? {
    if (location != "-") {
        return RejectionReason.InvalidCancelledField(
            field = ScheduleField.LOCATION,
            value = location,
        )
    }
    if (teachersText != "-") {
        return RejectionReason.InvalidCancelledField(
            field = ScheduleField.TEACHERS,
            value = teachersText,
        )
    }
    return null
}

private fun findActiveSessionViolation(
    location: String,
    teachers: List<String>,
): RejectionReason? {
    if (location == "-") {
        return RejectionReason.InvalidLocation(location)
    }
    for (teacher in teachers) {
        if (teacher.isBlank() || teacher == "-") {
            return RejectionReason.InvalidTeacher(teacher)
        }
    }
    val seenTeachers = mutableSetOf<String>()
    for (teacher in teachers) {
        if (!seenTeachers.add(teacher)) {
            return RejectionReason.DuplicateTeacher(teacher)
        }
    }
    return null
}

private fun createSession(
    date: LocalDate,
    type: SessionType,
    topic: String,
    location: String,
    teachers: List<String>,
): Session {
    return when (type) {
        SessionType.LECTURE ->
            Lecture(date, topic, location, teachers)
        SessionType.PRACTICE ->
            Practice(date, topic, location, teachers)
        SessionType.EXAM ->
            Exam(date, topic, location, teachers)
        SessionType.CANCELLED ->
            Cancelled(date, topic)
    }
}

private fun parseFileDate(value: String): LocalDate? {
    if (!datePattern.matches(value)) {
        return null
    }
    val date = try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        return null
    }
    return if (date.year in 1..9999) date else null
}
