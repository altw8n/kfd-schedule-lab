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
    val teachers : List<String>
) : Session

class Practice (
    override val date : LocalDate,
    override val topic : String,
    val location : String,
    val teachers : List<String>
) : Session

class Exam (
    override val date : LocalDate,
    override val topic : String,
    val location : String,
    val teachers : List<String>
) : Session

class Cancelled (
    override val date : LocalDate,
    override val topic : String,
) : Session