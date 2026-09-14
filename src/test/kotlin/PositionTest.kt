import model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PositionTest {

    @Test
    fun `recognizes exact tokens`() {
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("FORWARD"))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("MIDFIELD"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("DEFENDER"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("GOALKEEPER"))
    }

    @Test
    fun `is case insensitive`() {
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("defender"))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("GoalKeeper"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull("forward"))
    }

    @Test
    fun `ignores surrounding whitespace`() {
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("  midfield \t"))
    }

    @Test
    fun `returns null for blank or unknown token`() {
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("   "))
        assertNull(Position.fromCsvTokenOrNull("COACH"))
    }

    @Test
    fun `has russian names`() {
        assertEquals("нападающий", Position.FORWARD.russianName)
        assertEquals("полузащитник", Position.MIDFIELD.russianName)
        assertEquals("защитник", Position.DEFENDER.russianName)
        assertEquals("вратарь", Position.GOALKEEPER.russianName)
    }
}
