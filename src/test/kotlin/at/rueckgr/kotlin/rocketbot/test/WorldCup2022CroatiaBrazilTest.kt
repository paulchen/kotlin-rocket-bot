package at.rueckgr.kotlin.rocketbot.test

class WorldCup2022CroatiaBrazilTest : FixtureTest(
    fixtureDirectory = "/example-football-api-responses/fixture-978072",
    expectedStateChanges = listOf(
        121 to "Anpfiff 1. Halbzeit",
        212 to "Halbzeitpause; Spielstand: 0\uFEFF:\uFEFF0",
        244 to "Anpfiff 2. Halbzeit; Spielstand: 0\uFEFF:\uFEFF0",
        341 to "Pause vor Verlängerung; Spielstand: 0\uFEFF:\uFEFF0",
        351 to "Anpfiff Verlängerung 1. Halbzeit; Spielstand: 0\uFEFF:\uFEFF0 i.V. (0\uFEFF:\uFEFF0)",
        387 to "Halbzeitpause in der Verlängerung; Spielstand: 0\uFEFF:\uFEFF1 i.V. (0\uFEFF:\uFEFF0)",
        393 to "Anpfiff Verlängerung 2. Halbzeit; Spielstand: 0\uFEFF:\uFEFF1 i.V. (0\uFEFF:\uFEFF0)",
        427 to "Pause vor Elfmeterschießen; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0)",
        436 to "Elfmeterschießen; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 0\uFEFF:\uFEFF0 i.E.",
        450 to "Spielende nach Elfmeterschießen; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 4\uFEFF:\uFEFF2 i.E."
    ),
    expectedEvents = listOf(
        383 to "Tor in Spielminute 105 für :flag_br:\u00A0*Brasilien* durch Neymar; Spielstand: 0\uFEFF:\uFEFF1 i.V. (0\uFEFF:\uFEFF0)",
        415 to "Tor in Spielminute 117 für :flag_hr:\u00A0*Kroatien* durch Bruno Petković; Spielstand: 1\uFEFF:\uFEFF1 i.V. (0\uFEFF:\uFEFF0)",
        440 to "Elfmetertreffer für :flag_hr:\u00A0*Kroatien* durch Nikola Vlašić; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 1\uFEFF:\uFEFF0 i.E.",
        440 to "Vergebener Elfmeter für :flag_br:\u00A0*Brasilien* durch Rodrygo; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 1\uFEFF:\uFEFF0 i.E.",
        442 to "Elfmetertreffer für :flag_hr:\u00A0*Kroatien* durch Lovro Majer; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 2\uFEFF:\uFEFF0 i.E.",
        444 to "Elfmetertreffer für :flag_br:\u00A0*Brasilien* durch Casemiro; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 2\uFEFF:\uFEFF1 i.E.",
        446 to "Elfmetertreffer für :flag_hr:\u00A0*Kroatien* durch Luka Modrić; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 3\uFEFF:\uFEFF1 i.E.",
        446 to "Elfmetertreffer für :flag_br:\u00A0*Brasilien* durch Pedro; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 3\uFEFF:\uFEFF2 i.E.",
        448 to "Elfmetertreffer für :flag_hr:\u00A0*Kroatien* durch Mislav Oršić; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 4\uFEFF:\uFEFF2 i.E.",
        450 to "Vergebener Elfmeter in Spielminute 120 für :flag_br:\u00A0*Brasilien* durch Marquinhos; Spielstand: 1\uFEFF:\uFEFF1 n.V. (0\uFEFF:\uFEFF0), 4\uFEFF:\uFEFF2 i.E."
    )
)
