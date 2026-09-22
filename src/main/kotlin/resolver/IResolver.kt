package resolver

import model.Player
import model.Position
import model.Team

interface IResolver {
    fun getCountWithoutAgency(): Int
    fun getBestScorerDefender(): Pair<String, Int>
    fun getTheExpensiveGermanPlayerPosition(): String
    fun getTheRudestTeam(): Team
    fun getAverageTransferCostByPosition(): Map<Position, Double>
    fun getMostValuablePlayers(): List<Player>
    fun getMostPopularAgencyByCountry(): Map<String, String>
    fun getGoalsShareByMedianCost(): Pair<Double, Double>
}