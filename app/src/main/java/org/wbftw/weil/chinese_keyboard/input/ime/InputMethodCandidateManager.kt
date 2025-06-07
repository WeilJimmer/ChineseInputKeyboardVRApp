package org.wbftw.weil.chinese_keyboard.input.ime

import android.nfc.Tag
import android.util.Log
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass
import org.wbftw.weil.chinese_keyboard.input.utils.UltraEfficientCandidateProvider
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode

/**
 * 主輸入法候選字管理器
 * 負責管理候選字的查找、分頁和用戶輸入
 * @param rootNode 根節點
 * @param pageConfigure 分頁配置
 */
@Suppress("unused")
class InputMethodCandidateManager(
    private val rootNode: UniformTrieNode,
    private val pageConfigure: CandidateClass.PageConfigure
) {
    private val TAG = "InputMethodCandidateManager"

    private val provider = UltraEfficientCandidateProvider() // 使用超高效候選字提供器
    private var currentNode: UniformTrieNode? = null

    // 當前頁索引
    private var currentSearchState: CandidateClass.NodeSearchState = CandidateClass.NodeSearchState()
    private var nextSearchState: CandidateClass.NodeSearchState = CandidateClass.NodeSearchState()
    private var hasNextPage: Boolean = false

    companion object {

        /**
         * 根據輸入查找節點
         */
        fun findNodeByInput(input: String, root: UniformTrieNode): UniformTrieNode? {
            var current = root

            for (char in input) {
                val next = current.getChildNode(char) ?: return null
                current = next
            }

            return current
        }

    }

    /**
     * 設置用戶輸入
     */
    fun setInput(input: String): List<Char>? {
        // 根據輸入查找節點
        currentNode = findNodeByInput(input, rootNode)
        currentSearchState = CandidateClass.NodeSearchState() // 重置搜索狀態
        provider.clearCache()
        if (currentNode?.isFinalNode() == true){
            return listOf('*')
        }else if (currentNode?.isNotFinalNode() == true) {
            // 如果是非終端節點，則返回該節點的子節點鍵
            return currentNode?.getOrderedChildrenKeys()
        }
        return null
    }

    /**
     * 獲取當前頁候選字
     */
    fun getCurrentPageCandidates(): List<CandidateClass.CandidatePair> {
        return getCurrentPageCandidatesFullInformation().candidates.map {
            CandidateClass.CandidatePair.from(it)
        }
    }

    /**
     * 獲取帶路徑信息和詳細資料的候選字
     * 用於高級功能如用戶選擇後的路徑提升
     */
    fun getCurrentPageCandidatesFullInformation(): CandidateClass.CandidateResult {
        val node = currentNode
        if (node == null) {
            Log.e(TAG, "Current node is null, cannot get candidates.")
            return CandidateClass.CandidateResult() // 如果沒有當前節點，返回空結果
        }
        val result = provider.getCandidates(
            node = node,
            searchState = currentSearchState,
            pageConfigure = pageConfigure
        )
        // 更新下一頁搜索狀態
        val lastCandidate = result.candidates.lastOrNull()
        hasNextPage = result.hasNextPage
        nextSearchState = CandidateClass.NodeSearchState(
            nodeIndex = currentSearchState.nodeIndex,
            extractedCandidate = (lastCandidate?.wordIndex ?: 0) + 1,
            currentSearchPage = (result.currentPage + 1)
        )
        return result
    }

    /**
     * 獲取下一頁
     */
    fun getNextPageCandidates(): List<CandidateClass.CandidatePair> {
        val node = currentNode ?: return emptyList()

        currentSearchState = if (hasNextPage){
            // 如果有下一頁，使用下一頁的搜索狀態
            nextSearchState
        } else {
            // 否則，從頭開始搜索
            CandidateClass.NodeSearchState()
        }

        return getCurrentPageCandidates()
    }

    /**
     * 獲取上一頁
     */
    fun getPrevPageCandidates(): List<CandidateClass.CandidatePair> {
        val node = currentNode ?: return emptyList()

        // 如果當前頁索引為0，則無法返回上一頁
        if (currentSearchState.currentSearchPage <= 0) {
            currentSearchState = CandidateClass.NodeSearchState() // 重置搜索狀態
        }else{
            // 如果當前頁索引大於0，則返回上一頁
            currentSearchState = CandidateClass.NodeSearchState(
                nodeIndex = currentSearchState.nodeIndex,
                extractedCandidate = currentSearchState.extractedCandidate,
                currentSearchPage = currentSearchState.currentSearchPage - 1
            )
        }

        return getCurrentPageCandidates()
    }

    /**
     * 獲取當前節點的完整路徑
     */
    fun getCurrentNodeFullPath(): List<Char> {
        return currentNode?.getFullPath() ?: emptyList()
    }

}
