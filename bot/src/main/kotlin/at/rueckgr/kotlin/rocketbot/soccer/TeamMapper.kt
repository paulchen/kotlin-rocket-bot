package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.util.LocaleHelper

data class FormattedTeamName (val name: String, val flag: String)

object TeamMapper {
    private val flags = mapOf(
        "England" to ":england:",
        "USA" to ":us:",
        "Wales" to ":wales:",
        "Scotland" to ":scotland:",
        "Czech Republic" to ":cz:",
    )

    private val names = mapOf(
        "Scotland" to "Schottland",
        "Czech Republic" to "Tschechien",
    )

    fun mapTeamName(teamName: String): FormattedTeamName {
        val locale = LocaleHelper().getLocaleByCountryName(teamName)
        if (locale == null) {
            val flag = flags[teamName] ?: ""
            val name = names[teamName] ?: teamName
            return FormattedTeamName(name, flag)
        }
        val flag = ":flag_" + locale.country.lowercase() + ":"
        return FormattedTeamName(LocaleHelper().getLocalizedCountryName(locale), flag)
    }
}
