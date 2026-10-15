package schedule

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import kotlin.test.assertEquals

@ResourceLock(Resources.SYSTEM_OUT)
class CommandsTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun periodIncludesBothBoundsAndPreservesFileOrder() {
        val input = """
            2026-10-31|LECTURE|Last day|Room 1|Ivanov
            2026-09-30|LECTURE|Before range|Room 1|Ivanov
            2026-10-15|PRACTICE|Middle|Room 2|Petrov
            2026-11-01|LECTURE|After range|Room 1|Ivanov
            2026-10-01|CANCELLED|First day|-|-
        """.trimIndent()
        val result = execute(input, "period", "--from", "2026-10-01", "--to", "2026-10-31")
        assertEquals(0, result.code)
        assertEquals(
            """
                2026-10-31 | LECTURE | Last day | Room 1
                2026-10-15 | PRACTICE | Middle | Room 2
                2026-10-01 | CANCELLED | First day | -
                Total: 3
            """.trimIndent() + "\n",
            result.output,
        )
    }

    @Test
    fun periodAllowsSingleDayRange() {
        val result = execute(
            "2026-10-05|LECTURE|Topic|Room 1|Ivanov",
            "period", "--from", "2026-10-05", "--to", "2026-10-05",
        )
        assertEquals(0, result.code)
        assertEquals("2026-10-05 | LECTURE | Topic | Room 1\nTotal: 1\n", result.output)
    }

    @Test
    fun periodCountsRejectionOutsideRangeAndContinuesReading() {
        val input = """
            2026-11-01|EXAM|Outside range|Room 1|Ivanov
            2026-10-05|LECTURE|Valid|Room 2|Petrov
        """.trimIndent()
        val result = execute(input, "period", "--from", "2026-10-01", "--to", "2026-10-31")
        assertEquals(1, result.code)
        assertEquals("2026-10-05 | LECTURE | Valid | Room 2\nTotal: 1\n", result.output)
    }

    @Test
    fun periodPrintsZeroWhenNoDatesMatch() {
        val result = execute(
            "2026-11-01|LECTURE|Topic|Room 1|Ivanov",
            "period", "--from", "2026-10-01", "--to", "2026-10-31",
        )
        assertEquals(0, result.code)
        assertEquals("Total: 0\n", result.output)
    }

    @Test
    fun typeSkipsRejectionsAndOtherTypesAndPreservesOrder() {
        val input = """
            2026-10-20|EXAM|Second date|Room 3|Petrov,Ivanov
            2026-10-05|LECTURE|Other type|Room 1|Ivanov
            2026-10-05|EXAM|Invalid exam|Room 3|Ivanov
            2026-10-01|EXAM|First date|Room 2|Ivanov,Petrov
        """.trimIndent()
        val result = execute(input, "type", "--type", "EXAM")
        assertEquals(1, result.code)
        assertEquals(
            """
                2026-10-20 | EXAM | Second date | Room 3 | Petrov, Ivanov
                2026-10-01 | EXAM | First date | Room 2 | Ivanov, Petrov
                Total: 2
            """.trimIndent() + "\n",
            result.output,
        )
    }

    @Test
    fun typeCountsRejectionsEvenWhenNoSessionsMatch() {
        val input = """
            2026-10-05|LECTURE|Valid lecture|Room 1|Ivanov
            2026-10-05|PRACTICE|Invalid practice|Room 1|-
        """.trimIndent()
        val result = execute(input, "type", "--type", "EXAM")
        assertEquals(1, result.code)
        assertEquals("Total: 0\n", result.output)
    }

    @Test
    fun typePrintsDashesForCancelledSession() {
        val result = execute(
            "2026-10-05|CANCELLED|Postponed|-|-",
            "type", "--type", "CANCELLED",
        )
        assertEquals(0, result.code)
        assertEquals("2026-10-05 | CANCELLED | Postponed | - | -\nTotal: 1\n", result.output)
    }

    @Test
    fun allCommandsHandleEmptyFile() {
        val period = execute("", "period", "--from", "2026-10-01", "--to", "2026-10-31")
        val byType = execute("", "type", "--type", "EXAM")
        val errors = execute("", "errors")
        assertEquals(0, period.code)
        assertEquals("Total: 0\n", period.output)
        assertEquals(0, byType.code)
        assertEquals("Total: 0\n", byType.output)
        assertEquals(0, errors.code)
        assertEquals("Rejected: 0 of 0\n", errors.output)
    }

    @Test
    fun errorsPreservesLineNumbersAndContinuesAfterBlankLine() {
        val input = "2026-10-05|LECTURE|Valid|Room 1|Ivanov\r\n" +
            "\r\n" +
            "2026-13-45|LECTURE|Invalid|Room 1|Ivanov\r\n" +
            "2026-10-06|CANCELLED|Postponed|-|-\r\n"
        val result = execute(input, "errors")
        assertEquals(1, result.code)
        assertEquals(
            "line 2: invalid field separator\n" +
                "line 3: invalid date \"2026-13-45\"\n" +
                "Rejected: 2 of 4\n",
            result.output,
        )
    }

    private fun execute(input: String, vararg arguments: String): CommandResult {
        val file = directory.resolve("schedule.txt")
        Files.writeString(file, input, UTF_8)
        val originalOutput = System.out
        val buffer = ByteArrayOutputStream()
        PrintStream(buffer, true, UTF_8).use { capturedOutput ->
            try {
                System.setOut(capturedOutput)
                val code = runApplication(arrayOf(*arguments, "--file", file.toString()))
                return CommandResult(
                    code = code,
                    output = buffer.toString(UTF_8).replace("\r\n", "\n"),
                )
            } finally {
                System.setOut(originalOutput)
            }
        }
    }

    private data class CommandResult(
        val code: Int,
        val output: String,
    )
}
