package schedule

enum class AuditOperation {
    ADD,
    ADD_ALL
}

data class AuditEvent (
    val operation: AuditOperation,
    val addedCount : Int
)

class AuditedSessionRepository (private val delegate : SessionRepository)
    : SessionRepository by delegate {
    private val events  = mutableListOf<AuditEvent>()
    var totalAdded: Int = 0
        private set

    val auditEvents: List<AuditEvent>
        get() = java.util.List.copyOf(events)

    override fun add(session: Session) {
        delegate.add(session)
        addRecord(AuditOperation.ADD, 1)
    }

    override fun addAll(sessions: List<Session>) {
        delegate.addAll(sessions)
        addRecord(AuditOperation.ADD_ALL, sessions.size)
    }

    private fun addRecord(auditOperation: AuditOperation, addedCount: Int) {
        events.add(AuditEvent(auditOperation, addedCount))
        totalAdded += addedCount
    }
}