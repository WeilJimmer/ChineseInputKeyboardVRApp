package org.wbftw.weil.chinese_keyboard.converter

import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass

class JsonConverter {

    companion object {

        fun charListToJson(charList: List<Char>?): String {
            if (charList == null || charList.isEmpty()) {
                return "[]"
            }
            return charList.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }
        }

        fun candidatesPairToJson(candidates: CandidateClass.CandidatePair): String {
            return """{"w":"${candidates.candidateString}","p":"${candidates.fullPath.joinToString("")}"}"""
        }

        fun candidatesListToJson(candidates: List<CandidateClass.CandidatePair>): String {
            return candidates.joinToString(prefix = "[", postfix = "]", separator = ",") { candidatesPairToJson(it) }
        }

        fun parseCandidatePairFromJson(json: String): CandidateClass.CandidatePair? {
            val regex = """\{\s*"w"\s*:\s*"([^"]+)"\s*,\s*"p"\s*:\s*"([^"]+)"\s*\}""".toRegex()
            val matchResult = regex.find(json) ?: return null
            val (word, path) = matchResult.destructured
            return CandidateClass.CandidatePair(word, path.toList())
        }

    }

}