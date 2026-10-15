package schedule


import java.time.LocalDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionTest {
    private val validDate = LocalDate.of(2026, 10, 5)

    @Test
    fun createsAllFourSessionTypes() {
        val lecture = Lecture(
            date = validDate,
            topic = "Kotlin basics",
            location = "Room 312",
            teachers = listOf("Ivanov"),
        )
        val practice = Practice(
            date = validDate,
            topic = "Practice",
            location = "Room 313",
            teachers = listOf("Petrov"),
        )
        val exam = Exam(
            date = validDate,
            topic = "Final exam",
            location = "Room 401",
            teachers = listOf("Ivanov", "Petrov"),
        )
        val cancelled = Cancelled(
            date = validDate,
            topic = "Lecture postponed",
        )
        assertEquals(validDate, lecture.date)
        assertEquals("Room 312", lecture.location)
        assertEquals(listOf("Ivanov"), lecture.teachers)
        assertEquals(validDate, practice.date)
        assertEquals("Room 313", practice.location)
        assertEquals(listOf("Petrov"), practice.teachers)
        assertEquals(validDate, exam.date)
        assertEquals("Room 401", exam.location)
        assertEquals(listOf("Ivanov", "Petrov"), exam.teachers)
        assertEquals(validDate, cancelled.date)
        assertEquals("Lecture postponed", cancelled.topic)
    }

    @Test
    fun rejectsBlankTopic() {
        assertFailsWith<IllegalArgumentException> {
            Lecture(
                date = validDate,
                topic = "",
                location = "Room 312",
                teachers = listOf("Ivanov"),
            )
        }
    }

    @Test
    fun rejectsYearsOutsideAllowedRange() {
        for (year in listOf(0, 10000)) {
            assertFailsWith<IllegalArgumentException> {
                Cancelled(
                    date = LocalDate.of(year, 1, 1),
                    topic = "Topic",
                )
            }
        }
    }

    @Test
    fun rejectsLectureAndPracticeWithoutTeachers() {
        assertFailsWith<IllegalArgumentException> {
            Lecture(
                date = validDate,
                topic = "Topic",
                location = "Room 312",
                teachers = emptyList(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Practice(
                date = validDate,
                topic = "Topic",
                location = "Room 312",
                teachers = emptyList(),
            )
        }
    }

    @Test
    fun rejectsExamWithOnlyOneTeacher() {
        assertFailsWith<IllegalArgumentException> {
            Exam(
                date = validDate,
                topic = "Exam",
                location = "Room 401",
                teachers = listOf("Ivanov"),
            )
        }
    }

    @Test
    fun rejectsRepeatedTeacherNamesButAcceptsDifferentCase() {
        assertFailsWith<IllegalArgumentException> {
            Exam(
                date = validDate,
                topic = "Exam",
                location = "Room 401",
                teachers = listOf("Ivanov", "Ivanov"),
            )
        }
        val exam = Exam(
            date = validDate,
            topic = "Exam",
            location = "Room 401",
            teachers = listOf("Ivanov", "ivanov"),
        )
        assertEquals(listOf("Ivanov", "ivanov"), exam.teachers)
    }

    @Test
    fun rejectsDashForLocationAndTeacher() {
        assertFailsWith<IllegalArgumentException> {
            Lecture(
                date = validDate,
                topic = "Topic",
                location = "-",
                teachers = listOf("Ivanov"),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Lecture(
                date = validDate,
                topic = "Topic",
                location = "Room 312",
                teachers = listOf("-"),
            )
        }
    }

    @Test
    fun doesNotChangeAfterSourceTeacherListIsModified() {
        val sourceTeachers = mutableListOf("Ivanov", "Petrov")
        val exam = Exam(
            date = validDate,
            topic = "Exam",
            location = "Room 401",
            teachers = sourceTeachers,
        )
        sourceTeachers.clear()
        assertEquals(listOf("Ivanov", "Petrov"), exam.teachers)
    }

    @Test
    fun doesNotAllowChangingTeachersThroughPublicProperty() {
        val lecture = Lecture(
            date = validDate,
            topic = "Topic",
            location = "Room 312",
            teachers = listOf("Ivanov"),
        )
        val exposedTeachers = lecture.teachers as MutableList<String>
        assertFailsWith<UnsupportedOperationException> {
            exposedTeachers.add("Petrov")
        }
        assertEquals(listOf("Ivanov"), lecture.teachers)
    }
}