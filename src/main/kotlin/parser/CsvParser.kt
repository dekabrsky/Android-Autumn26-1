package parser

import model.ParseResult
import model.Player
import model.Position
import model.Team
import java.io.File

object CsvParser
{
    fun parse() : ParseResult
    {
        val file = File("src/main/resources/fakePlayersDirty.csv")
        if (!file.exists()) return ParseResult(emptyList(), 0)
        
        return parseLines(file.bufferedReader().readLines())
    }

    fun parseLines(lines: List<String>) : ParseResult
    {
        var skippedCount = 0
        val players = mutableListOf<Player>()
        val teamsMap = mutableMapOf<Pair<String, String>, Team>()
        
        if (lines.isEmpty())
            return ParseResult(players, skippedCount)
            
        val header = lines[0].split(";").map { it.trim() }
        
        for (line in lines.drop(1))
        {
            val values = line.split(";")
            val record = header.zip(values).toMap()

            val name = record["Name"]?.trim().orEmpty()
            val teamName = record["Team"]?.trim().orEmpty()
            val city = record["City"]?.trim().orEmpty()
            val nationality = record["Nationality"]?.trim().orEmpty()
            val positionRaw = record["Position"]?.trim().orEmpty()

            val agency = record["Agency"]?.trim()?.takeIf { it.isNotEmpty() }

            if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() ||
                nationality.isEmpty() || positionRaw.isEmpty()) {
                skippedCount++
                continue
            }

            val position = Position.fromCsvTokenOrNull(positionRaw)
            if (position == null) {
                skippedCount++
                continue
            }

            val transferCost = record["Transfer cost"]?.trim()?.toLongOrNull()
            val participations = record["Participations"]?.trim()?.toIntOrNull()
            val goals = record["Goals"]?.trim()?.toIntOrNull()
            val assists = record["Assists"]?.trim()?.toIntOrNull()
            val yellowCards = record["Yellow cards"]?.trim()?.toIntOrNull()
            val redCards = record["Red cards"]?.trim()?.toIntOrNull()

            if (transferCost == null || participations == null || goals == null ||
                assists == null || yellowCards == null || redCards == null) {
                skippedCount++
                continue
            }

            val teamKey = Pair(teamName, city)
            val team = teamsMap.getOrPut(teamKey) { Team(teamName, city) }

            val player = Player(
                name = name,
                team = team,
                position = position,
                nationality = nationality,
                agency = agency,
                transferCost = transferCost,
                participations = participations,
                goals = goals,
                assists = assists,
                yellowCards = yellowCards,
                redCards = redCards
            )
            
            players.add(player)
            team.players.add(player)
        }
        return ParseResult(players, skippedCount)
    }
}