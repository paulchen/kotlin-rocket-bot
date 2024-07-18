package at.rueckgr.kotlin.rocketbot.test

class WorldCup2022FinalTest : FixtureTest(
    fixtureDirectory = "/example-football-api-responses/fixture-979139",
    expectedStateChanges = listOf(
        120 to "Anpfiff 1. Halbzeit",
        224 to "Halbzeitpause; Spielstand: 2\uFEFF:\uFEFF0",
        254 to "Anpfiff 2. Halbzeit; Spielstand: 2\uFEFF:\uFEFF0 (2\uFEFF:\uFEFF0)",
        361 to "Pause vor Verlängerung; Spielstand: 2\uFEFF:\uFEFF2 (2\uFEFF:\uFEFF0)",
        371 to "Anpfiff Verlängerung 1. Halbzeit; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0)",
        402 to "Halbzeitpause in der Verlängerung; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0)",
        407 to "Anpfiff Verlängerung 2. Halbzeit; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0)",
        446 to "Pause vor Elfmeterschießen; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0)",
        454 to "Elfmeterschießen; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 0\uFEFF:\uFEFF0 i.E.",
        467 to "Spielende nach Elfmeterschießen; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 4\uFEFF:\uFEFF2 i.E."
    ),
    expectedEvents = listOf(
        164 to "Elfmetertreffer in Spielminute 22 für :flag_ar:\u00A0*Argentinien* durch Lionel Messi; Spielstand: 1\uFEFF:\uFEFF0",
        191 to "Tor in Spielminute 36 für :flag_ar:\u00A0*Argentinien* durch Ángel Di María; Spielstand: 2\uFEFF:\uFEFF0",
        323 to "Elfmetertreffer in Spielminute 79 für :flag_fr:\u00A0*Frankreich* durch Kylian Mbappé; Spielstand: 2\uFEFF:\uFEFF1 (2\uFEFF:\uFEFF0)",
        326 to "Tor in Spielminute 82 für :flag_fr:\u00A0*Frankreich* durch Kylian Mbappé; Spielstand: 2\uFEFF:\uFEFF2 (2\uFEFF:\uFEFF0)",
        413 to "Tor in Spielminute 109 für :flag_ar:\u00A0*Argentinien* durch Lionel Messi; Spielstand: 3\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0)",
        431 to "Elfmetertreffer in Spielminute 116 für :flag_fr:\u00A0*Frankreich* durch Kylian Mbappé; Spielstand: 3\uFEFF:\uFEFF3 i.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0)",
        455 to "Elfmetertreffer für :flag_fr:\u00A0*Frankreich* durch Kylian Mbappé; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 0\uFEFF:\uFEFF1 i.E.",
        457 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Lionel Messi; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 1\uFEFF:\uFEFF1 i.E.",
        458 to "Vergebener Elfmeter für :flag_fr:\u00A0*Frankreich* durch Kingsley Coman; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 1\uFEFF:\uFEFF1 i.E.",
        460 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Paulo Dybala; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 2\uFEFF:\uFEFF1 i.E.",
        462 to "Vergebener Elfmeter für :flag_fr:\u00A0*Frankreich* durch Aurélien Tchouaméni; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 2\uFEFF:\uFEFF1 i.E.",
        464 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Leandro Paredes; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 3\uFEFF:\uFEFF1 i.E.",
        466 to "Elfmetertreffer für :flag_fr:\u00A0*Frankreich* durch Randal Kolo Muani; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 3\uFEFF:\uFEFF2 i.E.",
        466 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Gonzalo Montiel; Spielstand: 3\uFEFF:\uFEFF3 n.V. (2\uFEFF:\uFEFF2, 2\uFEFF:\uFEFF0), 4\uFEFF:\uFEFF2 i.E."
    )
)
