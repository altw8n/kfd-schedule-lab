package schedule

enum class ScheduleField(val label: String) {
    DATE("date"),
    TYPE("type"),
    TOPIC("topic"),
    LOCATION("location"),
    TEACHERS("teachers"),
}

sealed interface RejectionReason {
    data object InvalidSeparator : RejectionReason

    data class WrongFieldCount(
        val actualCount: Int,
    ) : RejectionReason

    data class EmptyRequiredField(
        val field: ScheduleField,
    ) : RejectionReason

    data class InvalidDate(
        val value: String,
    ) : RejectionReason

    data class UnknownSessionType(
        val value: String,
    ) : RejectionReason

    data class NotEnoughTeachers(
        val actualCount: Int,
    ) : RejectionReason

    data class ControlCharacters(
        val field: ScheduleField,
        val value: String,
    ) : RejectionReason

    data class SurroundingWhitespace(
        val field: ScheduleField,
        val value: String,
    ) : RejectionReason

    data class InvalidCancelledField(
        val field: ScheduleField,
        val value: String,
    ) : RejectionReason

    data class InvalidLocation(
        val value: String,
    ) : RejectionReason

    data class InvalidTeacher(
        val value: String,
    ) : RejectionReason

    data class DuplicateTeacher(
        val value: String,
    ) : RejectionReason
}