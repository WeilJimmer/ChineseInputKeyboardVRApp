package org.wbftw.weil.chinese_keyboard.input.utils

/**
 * 超高效的候選字提供器
 * 根據節點類型自動選擇最佳策略提供候選字
 */
@Suppress("unused")
class UltraEfficientCandidateProvider {

    private val searcher = UltraEfficientPathSearcher()
    private var currentSearchState = CandidateClass.NodeSearchState()

    /**
     * 獲取候選字 - 根據節點類型選擇最優策略
     *
     * @param node 當前節點
     * @param searchState 目標搜索狀態(索引或頁碼)
     * @param pageConfigure 分頁配置(設定每頁顯示的候選字數量和每條路徑的候選字數量限制)
     * @return 候選字列表及分頁信息
     */
    fun getCandidates(
        node: UniformTrieNode,
        searchState: CandidateClass.NodeSearchState,
        pageConfigure: CandidateClass.PageConfigure
    ): CandidateClass.CandidateResult {
        // 檢查是否為最終節點且沒有子節點
        if (node.isFinalNode() && node.getChildrenNodes().isEmpty()) {
            // 是純終節點，直接返回該節點的候選字
            return getDirectCandidates(node, searchState,pageConfigure.perPageSize)
        }

        // 不是純終節點或有子節點，使用BFS搜索候選字
        return getBfsCandidates(node, searchState, pageConfigure.perPageSize, pageConfigure.maxCandidatesPerPath)
    }

    /**
     * 直接獲取最終節點的候選字(高效分頁)
     */
    private fun getDirectCandidates(
        node: UniformTrieNode,
        searchState: CandidateClass.NodeSearchState,
        perPageSize: Int,
    ): CandidateClass.CandidateResult {
        // 獲取該節點的所有候選字
        val allCandidates = node.getPromoteCandidates(0, 0) // 獲取所有候選字

        // 計算分頁信息
        val startIndex = searchState.currentSearchPage * perPageSize
        val endIndex = minOf(startIndex + perPageSize, allCandidates.size)

        // 檢查頁碼是否有效
        if (startIndex >= allCandidates.size) {
            return CandidateClass.CandidateResult(
                candidates = emptyList(),
                currentPage = searchState.currentSearchPage,
                hasNextPage = false
            )
        }

        // 提取當前頁的候選字
        val pageCandidates = allCandidates.subList(startIndex, endIndex)

        // 獲取節點路徑
        val nodePath = node.getFullPath()

        return CandidateClass.CandidateResult(
            candidates = pageCandidates.mapIndexed { index, word ->
                CandidateClass.CandidateDetail(
                    word = word,
                    fullPath = nodePath,
                    wordIndex = index,
                    nodeIndex = 0 // 直接獲取，節點索引不重要
                )
            },
            currentPage = searchState.currentSearchPage,
            hasNextPage = endIndex < allCandidates.size
        )
    }

    /**
     * 使用BFS獲取非純終節點的候選字
     */
    private fun getBfsCandidates(
        node: UniformTrieNode, // 已經定位到目標的節點
        searchState: CandidateClass.NodeSearchState, // 目標搜索位置
        perPageSize: Int,
        maxCandidatesPerPath: Int // 每條路徑的候選字數量限制
    ): CandidateClass.CandidateResult {

        searcher.bfsSearch(node) // 使用路徑搜索器
        return searcher.getCurrentPageResult(currentSearchState, perPageSize)

    }

    /**
     * 清除快取
     */
    fun clearCache() {
        searcher.clearCache() // 清除搜索器的快取
        currentSearchState = CandidateClass.NodeSearchState() // 重置搜索狀態
    }
}


