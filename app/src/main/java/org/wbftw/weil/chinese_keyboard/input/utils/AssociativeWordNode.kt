package org.wbftw.weil.chinese_keyboard.input.utils

import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import java.io.Serializable

data class AssociativeWordNode (
    val char: Char = '|', // 預設字符為不可識別字符
): Serializable {

    private val childrenMap = mutableMapOf<Char, AssociativeWordNode>()
    private var score: Int = 0 // 預設分數為0

    // 快取排序的結果
    @Transient
    private var sortedChildrenKeysCache: List<Char>? = null // 排序的子節點鍵快取

    /**
     * 創建或獲取子節點
     * @param char 子節點的字符
     * @return 返回創建或已存在的子節點
     */
    fun addChild(char: Char): AssociativeWordNode {
        if (!childrenMap.containsKey(char)) {
            val newNode = AssociativeWordNode(char)
            childrenMap[char] = newNode
        }
        return childrenMap[char]!!
    }

    /**
     * 獲取子節點
     * @param char 子節點的字符
     * @return 返回子節點，如果不存在則返回null
     */
    fun getChild(char: Char): AssociativeWordNode? {
        if (childrenMap.containsKey(char)) {
            return childrenMap[char]
        }
        return null
    }

    /**
     * 獲取所有子節點，不排序！
     * @return 返回子節點列表
     */
    fun getChildren(): List<AssociativeWordNode> {
        return childrenMap.values.toList()
    }

    /**
     * 獲取排序的子節點鍵
     * @return 返回排序的子節點鍵列表
     */
    fun getSortedChildrenKeys(): List<Char> {
        if (sortedChildrenKeysCache==null) {
            // 依照子節點分數排序
            sortedChildrenKeysCache = childrenMap.keys.sortedByDescending {
                childrenMap[it]?.getScore() ?: 0
            }
        }
        return sortedChildrenKeysCache ?: emptyList()
    }

    fun setScore(score: Int) {
        this.score = score
    }

    fun getScore(): Int {
        return score
    }

    fun isRoot(): Boolean {
        return char == '|'
    }

    fun isFinal(): Boolean {
        return childrenMap.isEmpty()
    }

    fun clearCache() {
        sortedChildrenKeysCache = null
    }

    fun getCandidateWords(path: String): List<Char> {
        var currentNode = this
        for (char in path) {
            currentNode = currentNode.getChild(char) ?: return emptyList()
        }
        return currentNode.getSortedChildrenKeys()
    }


}