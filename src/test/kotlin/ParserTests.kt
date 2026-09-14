import model.Position
import org.junit.jupiter.api.Assertions.assertEquals
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertNull

class ParserTest {
    private val header = "name,team,pos,country,agency,cost,mp,g,a,y,r"

    @Test
    fun `empty agency becomes null`() {
        val res = CsvParser.parse(sequenceOf(
            header,
            "Ivan,Zenit (SPb),forward,Russia,,1000,10,1,1,0,0"
        ))
        assertEquals(1, res.players.size)
        assertNull(res.players[0].Agency)
    }

    @Test fun `case and spaces in position are normalized`() {
        val res = CsvParser.parse(sequenceOf(
            header,
            "Ivan,Zenit (SPb),  ForWarD ,Russia,Agent,1000,10,1,1,0,0"
        ))
        assertEquals(Position.FORWARD, res.players[0].Position)
    }

    @Test fun `broken rows are skipped and counted`() {
        val res = CsvParser.parse(sequenceOf(
            header,
            "Ivan,Zenit (SPb),forward,Russia,Agent,1000,10,1,1,0,0",
            ",Zenit (SPb),forward,Russia,Agent,1000,10,1,1,0,0", // пустое имя
            "Petr,Zenit (SPb),forward,Russia,Agent,abc,10,1,1,0,0" // нечисловая стоимость
        ))
        assertEquals(1, res.players.size)
        assertEquals(2, res.skipped)
    }

    @Test fun `blank position skips row`() {
        val res = CsvParser.parse(sequenceOf(
            header, "Ivan,Zenit (SPb),,Russia,Agent,1000,10,1,1,0,0"
        ))
        assertEquals(0, res.players.size)
        assertEquals(1, res.skipped)
    }
}