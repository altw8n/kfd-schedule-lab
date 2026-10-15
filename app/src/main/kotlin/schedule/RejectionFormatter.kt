package schedule

fun formatRejection(rejection: ParseResult.Rejected): String {
    val message = when (val reason = rejection.reason) {
        RejectionReason.InvalidSeparator ->
            "invalid field separator"
        is RejectionReason.WrongFieldCount ->
            "got ${reason.actualCount} fields, expected 5"
        is RejectionReason.EmptyRequiredField ->
            "empty required field ${quoteValue(reason.field.label)}"
        is RejectionReason.InvalidDate ->
            "invalid date ${quoteValue(reason.value)}"
        is RejectionReason.UnknownSessionType ->
            "unknown session type ${quoteValue(reason.value)}"
        is RejectionReason.NotEnoughTeachers ->
            "type \"EXAM\" requires at least 2 teachers, got ${reason.actualCount}"
        is RejectionReason.ControlCharacters ->
            "field ${quoteValue(reason.field.label)} contains control characters: " +
                    quoteValue(reason.value)
        is RejectionReason.SurroundingWhitespace ->
            "field ${quoteValue(reason.field.label)} has leading or trailing whitespace: " +
                    quoteValue(reason.value)
        is RejectionReason.InvalidCancelledField ->
            "type \"CANCELLED\" requires ${quoteValue(reason.field.label)} " +
                    "to be \"-\", got ${quoteValue(reason.value)}"
        is RejectionReason.InvalidLocation ->
            "invalid location ${quoteValue(reason.value)}"
        is RejectionReason.InvalidTeacher ->
            "invalid teacher ${quoteValue(reason.value)}"
        is RejectionReason.DuplicateTeacher ->
            "duplicate teacher ${quoteValue(reason.value)}"
    }
    return "line ${rejection.lineNumber}: $message"
}

private fun quoteValue(value: String): String = buildString {
    append('"')
    for (character in value) {
        when {
            character == '\\' -> append("\\\\")
            character == '"' -> append("\\\"")
            Character.isISOControl(character) || character.code > 127 -> {
                append("\\u")
                append(
                    character.code
                        .toString(16)
                        .uppercase()
                        .padStart(4, '0'),
                )
            }
            else -> append(character)
        }
    }
    append('"')
}