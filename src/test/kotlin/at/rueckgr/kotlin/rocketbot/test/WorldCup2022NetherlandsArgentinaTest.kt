package at.rueckgr.kotlin.rocketbot.test

class WorldCup2022NetherlandsArgentinaTest : FixtureTest(
    fixtureDirectory = "/example-football-api-responses/fixture-977794",
    expectedStateChanges = listOf(
        120 to "Anpfiff 1. Halbzeit",
        221 to "Halbzeitpause; Spielstand: 0\uFEFF:\uFEFF1",
        251 to "Anpfiff 2. Halbzeit; Spielstand: 0\uFEFF:\uFEFF1 (0\uFEFF:\uFEFF1)",
        366 to "Pause vor Verlängerung; Spielstand: 2\uFEFF:\uFEFF2 (0\uFEFF:\uFEFF1)",
        376 to "Anpfiff Verlängerung 1. Halbzeit; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1)",
        406 to "Halbzeitpause in der Verlängerung; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1)",
        411 to "Anpfiff Verlängerung 2. Halbzeit; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1)",
        443 to "Pause vor Elfmeterschießen; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1)",
        450 to "Elfmeterschießen; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 0\uFEFF:\uFEFF0 i.E.",
        469 to "Spielende nach Elfmeterschießen; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 3\uFEFF:\uFEFF4 i.E."
    ),
    expectedEvents = listOf(
        187 to "Tor in Spielminute 35 für :flag_ar:\u00A0*Argentinien* durch Nahuel Molina; Spielstand: 0\uFEFF:\uFEFF1",
        306 to "Elfmetertreffer in Spielminute 73 für :flag_ar:\u00A0*Argentinien* durch Lionel Messi; Spielstand: 0\uFEFF:\uFEFF2 (0\uFEFF:\uFEFF1)",
        326 to "Tor in Spielminute 83 für :flag_nl:\u00A0*Niederlande* durch Wout Weghorst; Spielstand: 1\uFEFF:\uFEFF2 (0\uFEFF:\uFEFF1)",
        362 to "Tor in Spielminute 90 für :flag_nl:\u00A0*Niederlande* durch Wout Weghorst; Spielstand: 2\uFEFF:\uFEFF2 (0\uFEFF:\uFEFF1)",
        392 to "Tor in Spielminute 90 für :flag_nl:\u00A0*Niederlande* durch Wout Weghorst; Spielstand: 2\uFEFF:\uFEFF2 i.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1)",
        452 to "Vergebener Elfmeter für :flag_nl:\u00A0*Niederlande* durch Virgil van Dijk; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 0\uFEFF:\uFEFF0 i.E.",
        454 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Lionel Messi; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 0\uFEFF:\uFEFF1 i.E.",
        456 to "Vergebener Elfmeter für :flag_nl:\u00A0*Niederlande* durch Steven Berghuis; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 0\uFEFF:\uFEFF1 i.E.",
        458 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Leandro Paredes; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 0\uFEFF:\uFEFF2 i.E.",
        460 to "Elfmetertreffer für :flag_nl:\u00A0*Niederlande* durch Teun Koopmeiners; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 1\uFEFF:\uFEFF2 i.E.",
        460 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Gonzalo Montiel; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 1\uFEFF:\uFEFF3 i.E.",
        462 to "Elfmetertreffer für :flag_nl:\u00A0*Niederlande* durch Wout Weghorst; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 2\uFEFF:\uFEFF3 i.E.",
        464 to "Vergebener Elfmeter für :flag_ar:\u00A0*Argentinien* durch Enzo Fernández; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 2\uFEFF:\uFEFF3 i.E.",
        466 to "Elfmetertreffer für :flag_nl:\u00A0*Niederlande* durch Luuk de Jong; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 3\uFEFF:\uFEFF3 i.E.",
        468 to "Elfmetertreffer für :flag_ar:\u00A0*Argentinien* durch Lautaro Martínez; Spielstand: 2\uFEFF:\uFEFF2 n.V. (2\uFEFF:\uFEFF2, 0\uFEFF:\uFEFF1), 3\uFEFF:\uFEFF4 i.E."
    )
)
