package org.wbftw.weil.chinese_keyboard.input.utils

import java.io.Serializable

@Suppress("unused")
data class UniformTrieNode(
    val char: Char = '|', // 默認字符為不可識別字符
) : Serializable {
    private val childrenKeys = mutableListOf<Char>() // 維護子節點順序
    private val childrenMap = mutableMapOf<Char, UniformTrieNode>() // 快速查找
    private val candidates = mutableListOf<String>() // 終端候選字
    private var parentFullPathChars: List<Char> = listOf() // 父節點字符列表
    private var currentFullPathChars: List<Char> = listOf() // 當前節點到根節點的完整字符路徑
    private var isRoot: Boolean = false

    constructor(char: Char = '|', parentChars: List<Char> = listOf()) : this(char) {
        this.parentFullPathChars = parentChars
        this.currentFullPathChars = (parentChars.toMutableList().apply { add(char) } ).toList()
        this.isRoot = parentChars.isEmpty() // 如果沒有父節點，則為根節點
    }

    /**
     * 創建或獲取子節點
     * @param char 子節點的字符
     * @return 返回創建或已存在的子節點
     */
    fun addChild(char: Char): UniformTrieNode {
        if (!childrenMap.containsKey(char)) {
            val newNode = UniformTrieNode(char, currentFullPathChars)
            childrenMap[char] = newNode
            childrenKeys.add(char)
        }
        return childrenMap[char]!!
    }

    /**
     * 建立字典
     * @param candidate 候選字
     * @return 返回子節點
     */
    fun addCandidate(candidate: String): UniformTrieNode {
        if (candidate.isNotEmpty() && !candidates.contains(candidate)) {
            candidates.add(candidate)
        }
        return this
    }

    fun isFinalNode(): Boolean {
        return candidates.isNotEmpty() // 有候選字即為終端節點
    }

    fun isNotFinalNode(): Boolean {
        return candidates.isEmpty() // 無候選字即為非終端節點
    }

    fun promoteChild(char: Char) {
        val index = childrenKeys.indexOf(char)
        if (index != -1) {
            childrenKeys.removeAt(index)
            childrenKeys.add(char) // 移到最末 = 最高優先級
        }
    }

    fun promoteCandidate(index: Int, candidate: String) {
        if (index>=0 && index in candidates.indices && candidates[index] == candidate) {
            candidates.removeAt(index)
            candidates.add(candidate) // 移到最末 = 最高優先級
        }else{
            val wordIndex = candidates.indexOf(candidate)
            if (wordIndex != -1) {
                candidates.removeAt(wordIndex)
                candidates.add(candidate) // 移到最末 = 最高優先級
            }
        }
    }

    fun getOrderedChildrenKeys(): List<Char> {
        return childrenKeys.asReversed() // 從最後開始遍歷
    }

    fun getChildrenNodes(): List<UniformTrieNode> {
        return childrenKeys.mapNotNull { childrenMap[it] }.asReversed() // 返回子節點列表，從最後開始
    }

    fun getChildNode(char: Char): UniformTrieNode?{
        return childrenMap[char]
    }

    fun getKey(): Char {
        return char
    }

    /**
     * 獲取第一個子節點 (最高優先級別是最後一個)
     * @return 返回第一個子節點，如果沒有則返回null
     */
    fun getFirstNode(): UniformTrieNode? {
        return if (childrenKeys.isNotEmpty()) {
            childrenMap[childrenKeys.last()] ?: UniformTrieNode() // 返回最後一個子節點
        } else {
            null
        }
    }

    fun filterList(list: List<String>, index: Int, limit: Int): List<String> {
        // 空列表或索引超出範圍時直接返回空列表
        if (list.isEmpty() || index >= list.size) {
            return emptyList()
        }

        // 如果limit小於等於0，返回index之後的所有元素
        return if (limit <= 0) {
            list.subList(index, list.size)
        } else {
            // 正常情況，返回指定範圍的元素
            val endIndex = minOf(index + limit, list.size)
            list.subList(index, endIndex)
        }
    }

    /**
     * 獲取推廣候選字列表
     * @return 返回候選字列表，按長度降序排列
     */
    fun getPromoteCandidates(index: Int, limit: Int = 0): List<String>{
        if (candidates.isEmpty()) {
            return emptyList() // 如果沒有候選字，返回空列表
        }
        val promoted = candidates.toMutableList().asReversed()
        return filterList(promoted, index, limit)
    }

    /**
     * 獲取當前節點的候選字列表
     * @return 返回子節點候選字的列表
     */
    fun getCandidates(): List<String> {
        return candidates.toList() // 返回候選字列表的副本
    }

    /**
     * 獲取當前節點的候選字總數量
     * @return 數量
     */
    fun getCandidatesSize(): Int {
        return candidates.size // 返回候選字數量
    }

    /**
     * 獲取當前節點到根節點的路徑鍵列表
     * @return 返回從根到當前節點的鍵列表
     */
    fun getFullPath(): List<Char> {
        return currentFullPathChars.toList() // 返回當前路徑字符列表的副本
    }

    /**
     * 獲取所有子節點的鍵(Debug用)
     * @return 返回子節點鍵的列表
     */
    fun dump(): String {
        return "Children: ${childrenKeys.joinToString(", ")}, Candidates: ${candidates.joinToString(", ")}"
    }


}

