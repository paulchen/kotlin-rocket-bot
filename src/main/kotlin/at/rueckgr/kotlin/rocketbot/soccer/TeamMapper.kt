package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.util.LocaleHelper

data class FormattedTeamName (val name: String, val flag: String)

object TeamMapper {
    private val flags = mapOf(
        "England" to ":england:",
        "USA" to ":us:",
        "Wales" to ":wales:",
    )

    fun mapTeamName(teamName: String): FormattedTeamName {
        val locale = LocaleHelper().getLocaleByCountryName(teamName)
        if (locale == null) {
            if (flags.containsKey(teamName)) {
                return FormattedTeamName(teamName, flags[teamName]!!)
            }
            return FormattedTeamName(teamName, "")
        }
        val flag = ":" + locale.country.lowercase() + ":"
        return FormattedTeamName(LocaleHelper().getLocalizedCountryName(locale), flag)
    }
}
