package schedule

interface SessionRepository {
    fun add(session: Session)
    fun addAll(sessions: List<Session>)
    fun all() : List<Session>
}

class InMemorySessionRepository : SessionRepository {
    private val sessionsList  = mutableListOf<Session>()

    override fun add(session: Session) {
        sessionsList.add(session)
    }

    override fun addAll(sessions: List<Session>) {
        sessionsList.addAll(sessions)
    }

    override fun all(): List<Session> {
        return java.util.List.copyOf(sessionsList)
    }
}