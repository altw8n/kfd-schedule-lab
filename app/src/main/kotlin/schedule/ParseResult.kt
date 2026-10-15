package schedule

sealed interface ParseResult {
    data class Success(val session: Session) : ParseResult

    data class Rejected(
        val lineNumber: Long,
        val reason: RejectionReason,
    ) : ParseResult
}