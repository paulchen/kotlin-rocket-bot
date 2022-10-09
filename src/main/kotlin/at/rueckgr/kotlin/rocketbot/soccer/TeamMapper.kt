package at.rueckgr.kotlin.rocketbot.soccer

data class FormattedTeamName (val name: String, val flag: String)

object TeamMapper {
    private val flags = mapOf(
        "Argentina" to ":ar:",
        "Australia" to ":au:",
        "Belgium" to ":be:",
        "Brazil" to ":br:",
        "Cameroon" to ":cm:",
        "Canada" to ":cn:",
        "Costa Rica" to ":cr:",
        "Croatia" to ":hr:",
        "Denmark" to ":dk:",
        "Ecuador" to ":ec:",
        "England" to ":england:",
        "France" to ":fr:",
        "Germany" to ":de:",
        "Ghana" to ":gh:",
        "Iran" to ":ir:",
        "Japan" to ":jp:",
        "Mexico" to ":mx:",
        "Morocco" to ":ma:",
        "Netherlands" to ":nl:",
        "Poland" to ":pl:",
        "Portugal" to ":pt:",
        "Qatar" to ":qa:",
        "Saudi Arabia" to ":sa:",
        "Senegal" to ":sn:",
        "Serbia" to ":rs:",
        "South Korea" to ":kr:",
        "Spain" to ":es:",
        "Switzerland" to ":ch:",
        "Tunisia" to ":tn:",
        "Uruguay" to ":uy:",
        "USA" to ":us:",
        "Wales" to ":wales:",
    )

    fun mapTeamName(teamName: String): FormattedTeamName {
        val flag = if(flags.containsKey(teamName)) {
            flags[teamName]!!
        }
        else {
            ""
        }
        return FormattedTeamName(teamName, flag)
    }
}
