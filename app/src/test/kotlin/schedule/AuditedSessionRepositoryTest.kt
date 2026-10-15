package schedule

import java.time.LocalDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuditedSessionRepositoryTest {
    private val first = Cancelled(LocalDate.of(2026, 10, 5), "First")
    private val second = Cancelled(LocalDate.of(2026, 10, 6), "Second")

    @Test
    fun startsWithEmptyAuditAndZeroCount() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        assertEquals(0, repository.totalAdded)
        assertEquals(emptyList(), repository.auditEvents)
    }

    @Test
    fun delegatesStorageAndRecordsSingleAndBatchAdditions() {
        val storage = InMemorySessionRepository()
        val repository = AuditedSessionRepository(storage)
        repository.add(first)
        repository.addAll(listOf(second, first, second))
        val expectedSessions = listOf(first, second, first, second)
        assertEquals(expectedSessions, storage.all())
        assertEquals(expectedSessions, repository.all())
        assertEquals(4, repository.totalAdded)
        assertEquals(
            listOf(
                AuditEvent(AuditOperation.ADD, 1),
                AuditEvent(AuditOperation.ADD_ALL, 3),
            ),
            repository.auditEvents,
        )
    }

    @Test
    fun recordsEmptyBatchWithoutIncreasingCount() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        repository.add(first)
        repository.addAll(emptyList())
        assertEquals(listOf(first), repository.all())
        assertEquals(1, repository.totalAdded)
        assertEquals(
            listOf(
                AuditEvent(AuditOperation.ADD, 1),
                AuditEvent(AuditOperation.ADD_ALL, 0),
            ),
            repository.auditEvents,
        )
    }

    @Test
    fun readingDoesNotCreateAuditEvents() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        repository.add(first)
        repository.all()
        repository.all()
        assertEquals(1, repository.totalAdded)
        assertEquals(listOf(AuditEvent(AuditOperation.ADD, 1)), repository.auditEvents)
    }

    @Test
    fun auditSnapshotDoesNotChangeAfterFurtherAdditions() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        repository.add(first)
        val snapshot = repository.auditEvents
        repository.add(second)
        assertEquals(listOf(AuditEvent(AuditOperation.ADD, 1)), snapshot)
        assertEquals(2, repository.auditEvents.size)
    }

    @Test
    fun auditSnapshotCannotBeUsedToModifyJournal() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        repository.add(first)
        val snapshot = repository.auditEvents as MutableList<AuditEvent>
        assertFailsWith<UnsupportedOperationException> {
            snapshot.clear()
        }
        assertEquals(listOf(AuditEvent(AuditOperation.ADD, 1)), repository.auditEvents)
        assertEquals(1, repository.totalAdded)
    }

    @Test
    fun failedAdditionsDoNotCreateAuditEventsOrIncreaseCount() {
        val repository = AuditedSessionRepository(RejectingRepository())
        assertFailsWith<IllegalStateException> {
            repository.add(first)
        }
        assertFailsWith<IllegalStateException> {
            repository.addAll(listOf(first, second))
        }
        assertEquals(emptyList(), repository.auditEvents)
        assertEquals(0, repository.totalAdded)
    }

    @Test
    fun internalDelegateCallsDoNotCauseDuplicateAuditEvents() {
        val repository = AuditedSessionRepository(BatchThroughAddRepository())
        repository.addAll(listOf(first, second))
        assertEquals(listOf(first, second), repository.all())
        assertEquals(2, repository.totalAdded)
        assertEquals(listOf(AuditEvent(AuditOperation.ADD_ALL, 2)), repository.auditEvents)
    }
}

private class RejectingRepository : SessionRepository {
    override fun add(session: Session) {
        throw IllegalStateException("Storage unavailable")
    }
    override fun addAll(sessions: List<Session>) {
        throw IllegalStateException("Storage unavailable")
    }
    override fun all(): List<Session> = emptyList()
}

private class BatchThroughAddRepository : SessionRepository {
    private val storage = InMemorySessionRepository()

    override fun add(session: Session) {
        storage.add(session)
    }

    override fun addAll(sessions: List<Session>) {
        for (session in sessions) {
            add(session)
        }
    }

    override fun all(): List<Session> = storage.all()
}
