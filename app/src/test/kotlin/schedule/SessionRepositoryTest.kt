package schedule

import java.time.LocalDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionRepositoryTest {
    private val first = Cancelled(LocalDate.of(2026, 10, 5), "First")
    private val second = Cancelled(LocalDate.of(2026, 10, 6), "Second")
    private val third = Cancelled(LocalDate.of(2026, 10, 7), "Third")

    @Test
    fun startsEmpty() {
        val repository = InMemorySessionRepository()
        assertEquals(emptyList(), repository.all())
    }

    @Test
    fun addsSingleAndMultipleSessionsInOrder() {
        val repository = InMemorySessionRepository()
        repository.add(first)
        repository.addAll(listOf(second, third))
        assertEquals(listOf(first, second, third), repository.all())
    }

    @Test
    fun preservesDuplicates() {
        val repository = InMemorySessionRepository()
        repository.add(first)
        repository.addAll(listOf(first, first))
        assertEquals(listOf(first, first, first), repository.all())
    }

    @Test
    fun addingEmptyListDoesNotChangeContents() {
        val repository = InMemorySessionRepository()
        repository.add(first)
        repository.addAll(emptyList())
        assertEquals(listOf(first), repository.all())
    }

    @Test
    fun snapshotDoesNotChangeAfterFurtherAdditions() {
        val repository = InMemorySessionRepository()
        repository.add(first)
        val snapshot = repository.all()
        repository.add(second)
        repository.addAll(listOf(third))
        assertEquals(listOf(first), snapshot)
        assertEquals(listOf(first, second, third), repository.all())
    }

    @Test
    fun snapshotCannotBeUsedToModifyRepository() {
        val repository = InMemorySessionRepository()
        repository.add(first)
        val snapshot = repository.all() as MutableList<Session>
        assertFailsWith<UnsupportedOperationException> {
            snapshot.clear()
        }
        assertEquals(listOf(first), repository.all())
    }

    @Test
    fun changingSourceListDoesNotChangeRepository() {
        val repository = InMemorySessionRepository()
        val source = mutableListOf<Session>(first, second)
        repository.addAll(source)
        source.clear()
        assertEquals(listOf(first, second), repository.all())
    }
}
