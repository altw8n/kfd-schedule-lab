package schedule

interface CommandHandlers {
    fun period(command: Command.Period): Int
    fun byType(command: Command.ByType): Int
    fun errors(command: Command.Errors): Int
}

object StudentCommandHandlers : CommandHandlers {
    override fun period(command: Command.Period): Int = runPeriod(command)
    override fun byType(command: Command.ByType): Int = runType(command)
    override fun errors(command: Command.Errors): Int = runErrors(command)
}

fun runPeriod(command: Command.Period): Int {
    var selectedCount = 0L
    var hasRejections = false
    forEachScheduleLine(command.file) { number, line ->
        when (val result = parseScheduleLine(number, line)) {
            is ParseResult.Success -> {
                val session = result.session
                if (session.date in command.from..command.to) {
                    println(formatPeriodSession(session))
                    selectedCount += 1
                }
            }
            is ParseResult.Rejected -> {
                hasRejections = true
            }
        }
    }
    println("Total: $selectedCount")
    return if (hasRejections) 1 else 0
}

fun runType(command: Command.ByType): Int {
    var selectedCount = 0L
    var hasRejections = false
    forEachScheduleLine(command.file) { number, line ->
        when (val result = parseScheduleLine(number, line)) {
            is ParseResult.Success -> {
                val session = result.session
                if (sessionTypeOf(session) == command.type) {
                    println(formatTypeSession(session))
                    selectedCount += 1
                }
            }
            is ParseResult.Rejected -> {
                hasRejections = true
            }
        }
    }
    println("Total: $selectedCount")
    return if (hasRejections) 1 else 0
}

fun runErrors(command: Command.Errors): Int {
    var totalLines = 0L
    var rejectedLines = 0L
    forEachScheduleLine(command.file) { number, line ->
        totalLines += 1
        when (val result = parseScheduleLine(number, line)) {
            is ParseResult.Success -> {
            }
            is ParseResult.Rejected -> {
                rejectedLines += 1
                println(formatRejection(result))
            }
        }
    }
    println("Rejected: $rejectedLines of $totalLines")
    return if (rejectedLines == 0L) 0 else 1
}

private fun formatPeriodSession(session: Session): String {
    val type = when (session) {
        is Lecture -> SessionType.LECTURE
        is Practice -> SessionType.PRACTICE
        is Exam -> SessionType.EXAM
        is Cancelled -> SessionType.CANCELLED
    }
    val location = when (session) {
        is Lecture -> session.location
        is Practice -> session.location
        is Exam -> session.location
        is Cancelled -> "-"
    }
    return "${session.date} | ${type.name} | ${session.topic} | $location"
}

private fun sessionTypeOf(session: Session): SessionType {
    return when (session) {
        is Lecture -> SessionType.LECTURE
        is Practice -> SessionType.PRACTICE
        is Exam -> SessionType.EXAM
        is Cancelled -> SessionType.CANCELLED
    }
}

private fun formatTypeSession(session: Session): String {
    val teachers = when (session) {
        is Lecture -> session.teachers.joinToString(", ")
        is Practice -> session.teachers.joinToString(", ")
        is Exam -> session.teachers.joinToString(", ")
        is Cancelled -> "-"
    }
    return "${formatPeriodSession(session)} | $teachers"
}

