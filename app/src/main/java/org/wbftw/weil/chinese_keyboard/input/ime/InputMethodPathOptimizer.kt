package org.wbftw.weil.chinese_keyboard.input.ime

import android.util.Log
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode


/**
 * 輸入法路徑優化器
 * 根據用戶選擇優化路徑順序，提高常用候選字的優先級
 */
@Suppress("unused")
class InputMethodPathOptimizer(scoreTreeInit: UniformTrieNode? = null) {

    private val TAG = "InputMethodPathOptimizer"
    private var scoreTree: UniformTrieNode? = UniformTrieNode() // 用於存儲優化後的路徑樹

    init {
        if (scoreTreeInit != null) {
            Log.d(TAG, "Initializing score tree with provided node")
            scoreTree = scoreTreeInit
        } else {
            Log.d(TAG, "Using default empty score tree")
        }
    }

    companion object {

        /**
         * 推廣已使用的路徑
         * @param path 要推廣的路徑
         * @param rootNode 節點(root節點)
         * @param chooseWord 選中的候選字
         */
        fun promoteUsedPath(path: List<Char>, rootNode: UniformTrieNode, chooseWord: String) {
            if (path.isEmpty()) {
                return // 如果路徑為空或索引無效，直接返回
            }

            val nodes = mutableListOf<Pair<Char,UniformTrieNode>>()
            var current = rootNode

            // 收集路徑上的所有節點
            for (char in path) {
                nodes.add(Pair(char, current))
                current = current.getChildNode(char) ?: break
            }

            // 如果是最後一個節點，將選中的關鍵詞提升優先級
            if (current.isFinalNode()){
                current.promoteCandidate( chooseWord)
            }

            // 從上到下推廣整條路徑
            for (i in nodes.indices) {
                val pathChar = nodes[i].first
                val nodeToPromote = nodes[i].second
                if (nodeToPromote.isNotFinalNode()) {
                    nodeToPromote.promoteChild(pathChar)
                }
            }
        }

    }

    fun optimizePath(path: List<Char>, chooseWord: String) {
        scoreTree?.let { promoteUsedPath(path, it, chooseWord) }
    }

    fun optimizePath(candidatePair: CandidateClass.CandidatePair) {
        if (scoreTree != null) {
            Log.d(TAG, "Optimizing path for candidate: ${candidatePair.candidateString}")
            optimizePath(candidatePair.fullPath, candidatePair.candidateString)
        }
    }

    fun setScoreTree(scoreTreeRead: UniformTrieNode?) {
        // 設置優化後的路徑樹
        if (scoreTreeRead != null) {
            Log.d(TAG, "Setting score tree")
            scoreTree = scoreTreeRead
        } else {
            Log.w(TAG, "Score tree is null, skipping set")
        }
    }


}