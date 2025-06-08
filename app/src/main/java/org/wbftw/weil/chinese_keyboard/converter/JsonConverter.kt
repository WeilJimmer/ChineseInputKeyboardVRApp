package org.wbftw.weil.chinese_keyboard.converter

import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass

class JsonConverter {

    companion object {

        /**
         * 將字符列表轉換為 JSON 字符串
         * @param charList 字符列表
         * @return JSON 字符串 例如 ["a", "b", "c"]
         */
        fun charListToJson(charList: List<Char>?): String {
            if (charList == null || charList.isEmpty()) {
                return "[]"
            }
            return charList.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }
        }

        /**
         * 將候選字對象轉換為 JSON 字符串
         * @param candidates 候選字對象
         * @return JSON 字符串 例如 {"w": "幹", "p": "e0"}
         */
        fun candidatesPairToJson(candidates: CandidateClass.CandidatePair): String {
            return """{"w":"${candidates.candidateString}","p":"${candidates.fullPath.joinToString("")}"}"""
        }

        fun candidatesListToJson(candidates: List<CandidateClass.CandidatePair>): String {
            return candidates.joinToString(prefix = "[", postfix = "]", separator = ",") { candidatesPairToJson(it) }
        }

        /**
         * 將候選字結果轉換為 JSON 字符串
         * @param candidates 候選字列表
         * @param charList 路徑字符列表
         * @param hasNextPage 是否有下一頁
         * @return JSON 字符串 例如 {"words": [ { "w": "幹", "p": "e0"}, ...], "chars": [" ","3","4"], "hasNextPage": true/false}
         */
        fun candidatesResultToJson(candidates: List<CandidateClass.CandidatePair>, charList: List<Char>?, hasNextPage: Boolean): String {
            val candidatesPairListJson = candidatesListToJson(candidates)
            val charListJson = charListToJson(charList)
            println("""{"words":$candidatesPairListJson,"chars":$charListJson,"hasNextPage":$hasNextPage}""")
            return """{"words":$candidatesPairListJson,"chars":$charListJson,"hasNextPage":$hasNextPage}"""
        }

        /**
         * 從 JSON 字符串解析候選字對象
         * @param json JSON 字符串 例如 {"w": "幹", "p": "e0"}
         * @return 候選字對象 或 null 如果解析失敗
         */
        fun parseCandidatePairFromJson(json: String): CandidateClass.CandidatePair? {
            val regex = """\{\s*"w"\s*:\s*"([^"]+)"\s*,\s*"p"\s*:\s*"([^"]+)"\s*\}""".toRegex()
            val matchResult = regex.find(json) ?: return null
            val (word, path) = matchResult.destructured
            return CandidateClass.CandidatePair(word, path.toList())
        }

    }

}