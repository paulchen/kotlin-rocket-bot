package at.rueckgr.kotlin.rocketbot.soccer

class TeamMapper {
    companion object {
        val instance = TeamMapper()
    }

    fun mapTeamName(teamName: String): String {
        // TODO apply possible mapping of team names here (e.g. Italy -> ITA)
        return teamName
    }
}
