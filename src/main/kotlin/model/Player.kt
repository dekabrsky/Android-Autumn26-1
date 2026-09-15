package model

import com.github.javafaker.Country
import javax.crypto.KeyAgreement

class Player (
    val name: String,
    val team: Team,
    val position: Position,
    val country: String,
    val agency: String?,
    val transferCost: Long,
    val participations: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int
)

//Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards
//Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;D'Amore LLC;75012006;22;19;6;3;7

