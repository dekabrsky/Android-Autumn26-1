import model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PositionTest {

    @Test
    fun `token is recognized regardless of case and spaces`() {
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("DEFENDER"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("defender"))
        assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull("  Defender  "))
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("goalkeeper"))
        assertEquals(Position.FORWARD, Position.fromCsvTokenOrNull(" forward"))
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("MidField"))
    }

    @Test
    fun `unknown and empty tokens give null`() {
        assertNull(Position.fromCsvTokenOrNull(""))
        assertNull(Position.fromCsvTokenOrNull("   "))
        assertNull(Position.fromCsvTokenOrNull("striker"))
    }

    @Test
    fun `every position has a russian name`() {
        assertEquals("защитник", Position.DEFENDER.russianName)
        assertEquals("нападающий", Position.FORWARD.russianName)
        assertEquals("полузащитник", Position.MIDFIELD.russianName)
        assertEquals("вратарь", Position.GOALKEEPER.russianName)
    }
}
