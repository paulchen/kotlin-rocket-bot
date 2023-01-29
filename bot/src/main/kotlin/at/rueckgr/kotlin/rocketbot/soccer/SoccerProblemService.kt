package at.rueckgr.kotlin.rocketbot.soccer

object SoccerProblemService {
    val problems = mutableMapOf<SoccerProblem, String>()
}

enum class SoccerProblem {
    CACHE_NOT_WRITABLE
}
