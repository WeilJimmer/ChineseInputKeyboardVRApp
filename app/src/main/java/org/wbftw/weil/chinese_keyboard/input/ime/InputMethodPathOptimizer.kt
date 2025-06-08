package org.wbftw.weil.chinese_keyboard.input.ime

import android.util.Log
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode


/**
 * 輸入法路徑優化器
 * 根據用戶選擇優化路徑順序，提高常用候選字的優先級
 */
@Suppress("unused")
class InputMethodPathOptimizer(private val dictionaryRoot: UniformTrieNode?) {

    private val TAG = "InputMethodPathOptimizer"

    companion object {

        /**
         * 推廣已使用的路徑
         * @param path 要推廣的路徑
         * @param rootNode 節點(root節點)
         * @param wordIndex 選中的候選字索引
         * @param chooseWord 選中的候選字
         */
        fun promoteUsedPath(path: List<Char>, rootNode: UniformTrieNode, wordIndex: Int, chooseWord: String) {
            if (path.isEmpty() || wordIndex < 0) {
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
                current.promoteCandidate(wordIndex, chooseWord)
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

    fun optimizePath(path: List<Char>, wordIndex: Int, chooseWord: String) {
        if (dictionaryRoot != null) {
            promoteUsedPath(path, dictionaryRoot, wordIndex, chooseWord)
        }
    }

    fun optimizePath(candidatePair: CandidateClass.CandidatePair) {
        if (dictionaryRoot != null) {
            Log.d(TAG, "Optimizing path for candidate: ${candidatePair.candidateString}")
            optimizePath(candidatePair.fullPath, -1, candidatePair.candidateString)
        }
    }


}