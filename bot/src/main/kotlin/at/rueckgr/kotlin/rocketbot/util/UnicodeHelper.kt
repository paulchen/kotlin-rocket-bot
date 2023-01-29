package at.rueckgr.kotlin.rocketbot.util

import GraphemesBaseListener
import GraphemesLexer
import GraphemesParser
import GraphemesParser.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

// taken from https://github.com/bhamiltoncx/unicode-graphemes and modified
class UnicodeHelper {
    enum class GraphemeType { EMOJI, NON_EMOJI }
    data class Result(val type: GraphemeType, val stringOffset: Int, val stringLength: Int)

    private class CodePointCounter(private val input: String) {
        var inputIndex = 0
        var codePointIndex = 0

        fun advanceToIndex(newCodePointIndex: Int): Int {
            while (codePointIndex < newCodePointIndex) {
                val codePoint = Character.codePointAt(input, inputIndex)
                inputIndex += Character.charCount(codePoint)
                codePointIndex++
            }
            return inputIndex
        }
    }

    private class GraphemesParsingListener(string: String) : GraphemesBaseListener() {
        private val codePointCounter: CodePointCounter = CodePointCounter(string)
        private var clusterType: GraphemeType = GraphemeType.NON_EMOJI
        private var clusterStringStartIndex = 0

        val result: MutableList<Result> = mutableListOf()

        override fun enterGrapheme_cluster(ctx: Grapheme_clusterContext) {
            clusterStringStartIndex = codePointCounter.advanceToIndex(ctx.getStart().startIndex)
        }

        override fun enterEmoji_sequence(ctx: Emoji_sequenceContext?) {
            clusterType = GraphemeType.EMOJI
        }

        override fun exitGrapheme_cluster(ctx: Grapheme_clusterContext) {
            val clusterStringStopIndex = codePointCounter.advanceToIndex(ctx.getStop().stopIndex + 1)
            val clusterStringLength = clusterStringStopIndex - clusterStringStartIndex
            result.add(Result(clusterType, clusterStringStartIndex, clusterStringLength))
        }
    }

    private fun parse(string: String): List<Result> {
        val lexer = GraphemesLexer(CharStreams.fromString(string))
        val tokens = CommonTokenStream(lexer)
        val parser = GraphemesParser(tokens)
        val tree: GraphemesContext = parser.graphemes()
        val listener = GraphemesParsingListener(string)
        ParseTreeWalker.DEFAULT.walk(listener, tree)

        return listener.result
    }

    fun findOffsetOfSecondCharacter(username: String): Int {
        val grapheme = UnicodeHelper().parse(username)
        return when (grapheme.size) {
            0 -> 0
            1 -> username.length
            else -> grapheme[1].stringOffset
        }
    }
}
