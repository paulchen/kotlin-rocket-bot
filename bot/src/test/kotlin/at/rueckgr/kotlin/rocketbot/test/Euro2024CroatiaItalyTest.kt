package at.rueckgr.kotlin.rocketbot.test

class Euro2024CroatiaItalyTest : FixtureTest(
    fixtureDirectory = "/example-football-api-responses/fixture-1145529",
    expectedStateChanges = listOf(
        118 to "Anpfiff 1. Halbzeit",
        209 to "Halbzeitpause; Spielstand: 0\uFEFF:\uFEFF0",
        243 to "Anpfiff 2. Halbzeit; Spielstand: 0\uFEFF:\uFEFF0",
        350 to "Schlusspfiff nach regulärer Spielzeit; Spielstand: 1\uFEFF:\uFEFF1 (0\uFEFF:\uFEFF0)",
    ),
    expectedEvents = listOf(
        261 to "Vergebener Elfmeter in Spielminute 53 für :flag_hr:\u00A0*Kroatien* durch Luka Modrić; Spielstand: 0\uFEFF:\uFEFF0",
        263 to "Tor in Spielminute 55 für :flag_hr:\u00A0*Kroatien* durch Luka Modrić; Spielstand: 1\uFEFF:\uFEFF0 (0\uFEFF:\uFEFF0)",
        347 to "Tor in Spielminute 90 für :flag_it:\u00A0*Italien* durch Mattia Zaccagni; Spielstand: 1\uFEFF:\uFEFF1 (0\uFEFF:\uFEFF0)",
    )
)
