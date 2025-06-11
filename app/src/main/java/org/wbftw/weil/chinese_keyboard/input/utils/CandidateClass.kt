package org.wbftw.weil.chinese_keyboard.input.utils

import com.google.gson.Gson
import kotlin.math.exp
import kotlin.math.ln
import com.google.gson.annotations.SerializedName

class CandidateClass {
    /**
     * 候選字結果包含路徑信息
     */
    data class CandidateDetail(
        val word: String,         // 候選字
        val fullPath: List<Char>,  // 所屬節點的完整路徑
        val wordIndex: Int,       // 該路徑中的索引位置
        val nodeIndex: Int        // 節點在BFS序列中的索引
    )

    /**
     * 候選字結果類
     */
    data class CandidatePair(
        @SerializedName("w") val candidateString: String,
        @SerializedName("p") val fullPath: List<Char>,  // 所屬節點的完整路徑 (用於加強常用字的優先級排序)
    ) {

        companion object {
            /**
             * 將 CandidateWithPath 轉換為 CandidatePair
             */
            fun from(candidate: CandidateDetail): CandidatePair {
                return CandidatePair(
                    candidateString = candidate.word,
                    fullPath = candidate.fullPath
                )
            }
        }

        override fun toString(): String {
            return "CandidatePair(word='$candidateString', path=$fullPath)"
        }
    }

    /**
     * 路徑搜索狀態 - 記錄路徑的搜索進度
     */
    data class NodeSearchState(
        val startNodeIndex: Int = 0,     // 當前節點在 BFS 序列中的索引
        val extractedCandidate: Int = 0, // 已提取的候選字數量
        val currentSearchPage: Int = 0   // 當前搜索的頁碼(從0開始)
    )

    data class PageConfigure(
        val perPageSize: Int = 9,         // 每頁顯示的候選字數量
        val maxCandidatesPerPath: Int = 2 // 每條路徑的候選字數量限制(僅用於非終節點)
    )

    /**
     * 候選字結果類
     */
    data class CandidateResult(
        val candidates: List<CandidateClass.CandidateDetail> = emptyList(),
        val hasNextPage: Boolean = false,
        val currentPage: Int = 0,
    )

    /**
     * 詞條分數數據類
     */
    data class ScoreData(
        @SerializedName("s") var score: Int,
        @SerializedName("t") var lastAccessTime: Long = System.currentTimeMillis(),
    ) {
        /**
         * 計算有效分數（考慮時間衰退和使用頻率）
         */
        fun calculateEffectiveScore(): Int {
            val currentTime = System.currentTimeMillis()
            val daysSinceLastAccess = (currentTime - lastAccessTime) / (86400 * 1000.0)

            // --- 頻率分數：對次數取對數以避免高頻詞壓制 ---
            val freqComponent = ln(1.0 + score)

            // --- 時間衰減分數：7天內使用的加分效果最強 ---
            val recencyComponent = 1.0 / (1.0 + daysSinceLastAccess / 7.0)

            // --- 加權組合：可調參數 ---
            val totalScore = freqComponent + recencyComponent * 1.5  // 1.5 是近期權重

            return (totalScore * 100).toInt()
        }

        /**
         * 轉發為 JSON 字符串
         */
        override fun toString(): String {
            //return """{"s":$score,"t":$lastAccessTime}"""
            return Gson().toJson(this)
        }

        fun fromJson(json: String): ScoreData {
            // 解析 JSON 字符串並返回 ScoreData 對象
            return Gson().fromJson(json, ScoreData::class.java)
        }

    }

}