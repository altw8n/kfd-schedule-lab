package schedule

import java.time.LocalDate

sealed interface Session {
    val date : LocalDate
    val topic : String
}

class Lecture (
    override val date : LocalDate,
    override val topic : String,
    val location : String,
    teachers : List<String>
) : Session {
    private val teacherSnapshot: List<String> = java.util.List.copyOf(teachers)
    val teachers: List<String>
        get() = teacherSnapshot

    init {
        validateDate(date)
        validateTopic(topic)
        validateLocation(location)
        validateTeachers(teachers = teacherSnapshot, minimumTeacherCount = 1)
    }
}

class Practice (
    override val date : LocalDate,
    override val topic : String,
    val location : String,
    teachers : List<String>
) : Session {
    private val teacherSnapshot: List<String> = java.util.List.copyOf(teachers)
    val teachers: List<String>
        get() = teacherSnapshot

    init {
        validateDate(date)
        validateTopic(topic)
        validateLocation(location)
        validateTeachers(teachers = teacherSnapshot, minimumTeacherCount = 1)
    }
}

class Exam (
    override val date : LocalDate,
    override val topic : String,
    val location : String,
    teachers : List<String>
) : Session {
    private val teacherSnapshot: List<String> = java.util.List.copyOf(teachers)
    val teachers: List<String>
        get() = teacherSnapshot

    init {
        validateDate(date)
        validateTopic(topic)
        validateLocation(location)
        validateTeachers(teachers = teacherSnapshot, minimumTeacherCount = 2)
    }
}

class Cancelled (
    override val date : LocalDate,
    override val topic : String,
) : Session {
    init {
        validateDate(date)
        validateTopic(topic)
    }
}
