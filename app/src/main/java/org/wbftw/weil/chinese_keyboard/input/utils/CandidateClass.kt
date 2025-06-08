package org.wbftw.weil.chinese_keyboard.input.utils

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
        val candidateString: String,
        val fullPath: List<Char>,  // 所屬節點的完整路徑 (用於加強常用字的優先級排序)
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
}