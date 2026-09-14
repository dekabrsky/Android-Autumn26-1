import model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PositionTest {

    @Test
    fun `recognizes position in upper case`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("FORWARD"))
    }

    @Test
    fun `recognizes position in any case`() {
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("goalkeeper"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("DeFeNdEr"))
    }

    @Test
    fun `ignores surrounding spaces`() {
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("  midfield\t"))
    }

    @Test
    fun `returns null for unknown or empty token`() {
        assertNull(Position.fromCsvTokenOrNull("striker"))
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("   "))
    }

    @Test
    fun `every position has russian name`() {
        assertEquals("нападающий", Position.FORWARD.russianName)
        assertEquals("полузащитник", Position.MIDFIELD.russianName)
        assertEquals("защитник", Position.DEFENDER.russianName)
        assertEquals("вратарь", Position.GOALKEEPER.russianName)
    }
}
