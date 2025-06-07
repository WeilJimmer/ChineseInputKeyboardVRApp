package org.wbftw.weil.chinese_keyboard.utils

import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import kotlin.text.iterator

object TrieHelper {

    /**
     * [!DEBUG] 獲取候選字列表 (DFS 優先、測是用途)
     * @param path 要查找的路徑
     * @param node 當前節點(root節點)
     * @param limit 返回的候選字數量限制
     * @param index 當前候選字索引
     * @return 返回候選字列表
     */
    fun getCandidateList(path: String, node: UniformTrieNode, index: Int = 0, limit: Int = 20): List<String> {
        if (path.isEmpty()) {
            return emptyList() // 如果路徑為空，返回空列表
        }

        var currentNode: UniformTrieNode? = node
        for (char in path) {
            currentNode = currentNode?.getChildNode(char)
            if (currentNode == null) {
                return emptyList() // 如果找不到對應的子節點，返回空列表
            }
        }

        if (currentNode== null) {
            return emptyList() // 如果當前節點為空，返回空列表
        }

        // DFS 深度優先 遍歷直到找到終端節點
        while (currentNode!!.isNotFinalNode()){
            currentNode = currentNode.getFirstNode()
        }

        return currentNode.getPromoteCandidates(index, limit)
    }

}