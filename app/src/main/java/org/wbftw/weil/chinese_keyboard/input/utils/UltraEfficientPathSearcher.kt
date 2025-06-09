package org.wbftw.weil.chinese_keyboard.input.utils

import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass.CandidateResult
import java.util.LinkedList
import java.util.PriorityQueue
import java.util.Queue

/**
 * 超高效的路徑搜索器 - 進階優化版
 * 專門針對UniformTrieNode結構優化，提供極致的性能和內存效率
 */
class UltraEfficientPathSearcher() {

    var bfsCachePath: String = "" // 用於BFS搜索的緩存路徑
    var bfsResultCache: List<UniformTrieNode>? = null // 用於緩存BFS結果
    var pageResultCache: MutableList<CandidateResult> = mutableListOf() // 用於緩存每頁結果

    companion object {
        enum class Direction(val value: Int) {
            FORWARD(1),
            BACKWARD(-1) // for lookup
        }
    }
    /**
     * 根據總路徑分數獲取候選節點清單（使用 Best-First Search）
     */
    fun bestFirstSearch(root: UniformTrieNode): List<UniformTrieNode> {
        val targetPath = root.getFullPath().joinToString()
        if (bfsCachePath == targetPath && bfsResultCache != null) {
            return bfsResultCache!!
        }

        data class ScoredNode(
            val node: UniformTrieNode,
            val totalScore: Int,
            val pathLength: Int
        ) {
            val normalizedScore: Double get() = totalScore.toDouble() / pathLength
        }

        val result = mutableListOf<UniformTrieNode>()
        val queue = PriorityQueue<ScoredNode>(compareByDescending { it.normalizedScore })
        val bestScoreMap = mutableMapOf<UniformTrieNode, Double>()

        val rootScore = root.getSelfScore()
        queue.offer(ScoredNode(root, rootScore, 1))
        bestScoreMap[root] = rootScore.toDouble()

        while (queue.isNotEmpty()) {
            val scoredNode = queue.poll()
            if (scoredNode==null) continue // 防止空值
            val current = scoredNode.node
            val totalScore = scoredNode.totalScore
            val pathLength = scoredNode.pathLength

            if (current.isFinalNode()) {
                result.add(current)
            }

            for (child in current.getChildrenNodes()) {
                val childScore = child.getSelfScore()
                val newTotalScore = totalScore + childScore
                val newPathLength = pathLength + 1
                val newNormalized = newTotalScore.toDouble() / newPathLength

                val recorded = bestScoreMap[child]
                if (recorded == null || newNormalized > recorded) {
                    bestScoreMap[child] = newNormalized
                    queue.offer(ScoredNode(child, newTotalScore, newPathLength))
                }
            }
        }

        bfsResultCache = result
        bfsCachePath = targetPath
        return result
    }

    /**
     * 查找指定節點的候選字
     * @param node 要查找的節點
     * @return 返回候選字列表
     */
    fun findCandidateByNodeWithLimit(node: UniformTrieNode): List<String> {
        val percent = 0.3 // 30% 的候選字數量限制，假定平均為 10 候選字，選 3 個候選字。
        val maxLength = 5
        val candidatesSize = node.getCandidatesSize()
        val targetCount = (candidatesSize * percent).toInt().coerceAtMost(maxLength).coerceAtLeast(1)
        return node.getPromoteCandidates(0, targetCount)
    }

    /**
     * 獲取當前頁的結果
     * @param searchState 當前搜索狀態
     * @param perPage 每頁顯示的候選字數量
     * @return 返回一個包含候選字列表和搜索狀態列表
     */
    fun getCurrentPageResult(
        searchState: CandidateClass.NodeSearchState,
        perPage: Int = 9,
    ): CandidateResult {

        var hasNextPage: Boolean // 是否有下一頁
        var resultCandidates: List<CandidateClass.CandidateDetail>
        var finalResult: CandidateResult

        val direction = if ((searchState.currentSearchPage+1)>pageResultCache.size) {
            // 如果當前頁面索引大於快取大小，則為正向搜索，代表還沒檢索過
            Direction.FORWARD
        } else {
            // 否則為反向搜索，代表已經檢索過，直接從快取讀取
            Direction.BACKWARD
        }
        when (direction) {
            Direction.FORWARD -> {
                // 正向搜索 (新增或更新快取)
                val pair = searchForCandidatesForward(
                    startNodeIndex = searchState.startNodeIndex,
                    endNodeIndex = bfsResultCache!!.size,
                    extractedCandidate = searchState.extractedCandidate,
                    perPage = perPage
                )
                hasNextPage = pair.first // 是否有下一頁
                resultCandidates = pair.second // 獲取候選字列表
                finalResult = CandidateResult(
                    candidates = resultCandidates,
                    hasNextPage = hasNextPage,
                    currentPage = searchState.currentSearchPage,
                )
                pageResultCache.add(finalResult)
            }
            Direction.BACKWARD -> {
                // 反向搜索 (僅供查詢)
                var currentSearchPage = searchState.currentSearchPage
                if (currentSearchPage < 0) {
                    currentSearchPage = 0 // 防止頁面索引小於0
                }
                return if (pageResultCache.size > currentSearchPage) {
                    pageResultCache[currentSearchPage]
                }else{
                    CandidateResult() // 如果沒有快取，返回空結果
                }
            }
        }

        return finalResult
    }

    /**
     * 正向搜索候選字
     * @param startNodeIndex 起始節點索引
     * @param endNodeIndex 結束節點索引
     * @param extractedCandidate 已提取的候選字數量
     * @param perPage 每頁顯示的候選字數量
     * @return 返回一個包含是否有下一頁和候選字列表的Pair
     */
    fun searchForCandidatesForward(startNodeIndex: Int, endNodeIndex: Int, extractedCandidate: Int, perPage: Int): Pair<Boolean, List<CandidateClass.CandidateDetail>>{
        val resultCandidates: MutableList<CandidateClass.CandidateDetail> = mutableListOf()
        var hasNextPage = false
        for (ni in startNodeIndex until endNodeIndex) { //endNodeIndex is last bfsResultCache index, meaning the end of the search
            val node = bfsResultCache!![ni]
            val nodeFullPath: List<Char> = node.getFullPath()
            val nodeCandidates: List<String> = findCandidateByNodeWithLimit(node) // 候選字已被限制數量，每個節點只取特定數量。

            if (resultCandidates.size >= perPage){
                hasNextPage = true // 超過每頁限制，標記為有下一頁
                break  // 超過每頁限制則停止
            }
            if (extractedCandidate >= nodeCandidates.size) {
                continue // 這個 node 的候選字已經用完，跳到下一個 node
            }
            for (wi in extractedCandidate until nodeCandidates.size) {
                val candidate = nodeCandidates[wi]
                resultCandidates.add(
                    CandidateClass.CandidateDetail(
                        word = candidate,
                        fullPath = nodeFullPath,
                        wordIndex = wi,
                        nodeIndex = ni
                    )
                )
                if (resultCandidates.size >= perPage){
                    hasNextPage = true // 超過每頁限制，標記為有下一頁
                    break  // 超過每頁限制則停止
                }
            }
        }
        return Pair(hasNextPage, resultCandidates.toList())
    }


    /**
     * 清除快取
     * 用於重置搜索狀態或清理內存
     */
    fun clearCache() {
        bfsCachePath = ""
        bfsResultCache = null
        pageResultCache.clear()
    }

}