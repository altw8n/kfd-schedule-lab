package schedule

import java.time.LocalDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ScheduleLineParserTest {
    @Test
    fun parsesAllFourSessionTypes() {
        val lecture = assertIs<Lecture>(parseSuccess("LECTURE", "Room 1", "Ivanov"))
        assertEquals(LocalDate.of(2026, 10, 5), lecture.date)
        assertEquals("Topic", lecture.topic)
        assertEquals("Room 1", lecture.location)
        assertEquals(listOf("Ivanov"), lecture.teachers)

        val practice = assertIs<Practice>(parseSuccess("PRACTICE", "Room 2", "Petrov"))
        assertEquals("Room 2", practice.location)
        assertEquals(listOf("Petrov"), practice.teachers)

        val exam = assertIs<Exam>(parseSuccess("EXAM", "Room 3", "Petrov,Ivanov"))
        assertEquals("Room 3", exam.location)
        assertEquals(listOf("Petrov", "Ivanov"), exam.teachers)

        val cancelled = assertIs<Cancelled>(parseSuccess("CANCELLED", "-", "-"))
        assertEquals(LocalDate.of(2026, 10, 5), cancelled.date)
        assertEquals("Topic", cancelled.topic)
    }

    @Test
    fun rejectsInvalidStructureAndPreservesTrailingEmptyFields() {
        assertRejected("", RejectionReason.InvalidSeparator)
        assertRejected("2026-10-05;LECTURE;Topic;Room 1;Ivanov", RejectionReason.InvalidSeparator)
        assertRejected(
            "2026-10-05|LECTURE|Topic|Room 1|Ivanov|extra",
            RejectionReason.WrongFieldCount(6),
        )
        assertRejected(
            "2026-10-05|LECTURE|Topic|Room 1|",
            RejectionReason.EmptyRequiredField(ScheduleField.TEACHERS),
        )
        assertRejected("|LECTURE||Room 1|Ivanov", RejectionReason.EmptyRequiredField(ScheduleField.DATE))
    }

    @Test
    fun checksDateBeforeTypeAndExamCountBeforeTextViolations() {
        assertRejected(
            "2026-13-45|SEMINAR|Topic|Room 1|Ivanov",
            RejectionReason.InvalidDate("2026-13-45"),
        )
        assertRejected(
            "2026-10-05|EXAM|Topic\t|Room 1|Ivanov",
            RejectionReason.NotEnoughTeachers(1),
        )
    }

    @Test
    fun checksControlCharactersBeforeWhitespaceAndPreservesFieldOrder() {
        assertRejected(
            "2026-10-05|LECTURE| Topic|Room\t1|Ivanov",
            RejectionReason.ControlCharacters(ScheduleField.LOCATION, "Room\t1"),
        )
        assertRejected(
            "2026-10-05|LECTURE|Topic\t|Room\t1|Ivanov",
            RejectionReason.ControlCharacters(ScheduleField.TOPIC, "Topic\t"),
        )
        assertRejected(
            "2026-10-05|LECTURE|Topic|Room 1|Ivanov, Petrov",
            RejectionReason.SurroundingWhitespace(ScheduleField.TEACHERS, " Petrov"),
        )
    }

    @Test
    fun checksCancelledFieldsInOrder() {
        assertRejected(
            "2026-10-05|CANCELLED|Topic|Room 1|Ivanov",
            RejectionReason.InvalidCancelledField(ScheduleField.LOCATION, "Room 1"),
        )
        assertRejected(
            "2026-10-05|CANCELLED|Topic|-|Ivanov",
            RejectionReason.InvalidCancelledField(ScheduleField.TEACHERS, "Ivanov"),
        )
    }

    @Test
    fun checksLocationThenInvalidTeachersThenDuplicates() {
        assertRejected(
            "2026-10-05|LECTURE|Topic|-|-",
            RejectionReason.InvalidLocation("-"),
        )
        assertRejected(
            "2026-10-05|LECTURE|Topic|Room 1|Ivanov,Ivanov,,Petrov",
            RejectionReason.InvalidTeacher(""),
        )
        assertRejected(
            "2026-10-05|EXAM|Topic|Room 1|Ivanov,Ivanov",
            RejectionReason.DuplicateTeacher("Ivanov"),
        )
    }

    private fun parseSuccess(type: String, location: String, teachers: String): Session {
        val result = parseScheduleLine(1L, "2026-10-05|$type|Topic|$location|$teachers")
        return assertIs<ParseResult.Success>(result).session
    }

    private fun assertRejected(line: String, reason: RejectionReason) {
        assertEquals(ParseResult.Rejected(7L, reason), parseScheduleLine(7L, line), line)
    }
}
