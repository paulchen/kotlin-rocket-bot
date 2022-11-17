package at.rueckgr.kotlin.rocketbot.util

import java.util.*

class LocaleHelper {
    private val countryNamesMap: Map<String, Locale> = Locale
        .getISOCountries()
        .map { Locale("", it) }
        .associateBy { it.getDisplayCountry(Locale.ENGLISH) }

    fun getLocaleByCountryName(name: String) = countryNamesMap[name]

    fun getLocalizedCountryName(locale: Locale): String = locale.getDisplayCountry(Locale.GERMAN)
}
