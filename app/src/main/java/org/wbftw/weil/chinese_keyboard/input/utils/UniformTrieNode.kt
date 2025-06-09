package org.wbftw.weil.chinese_keyboard.input.utils

import java.io.Serializable

@Suppress("unused")
data class UniformTrieNode(
    val char: Char = '|', // 默認字符為不可識別字符
) : Serializable {
    private val childrenKeys = mutableListOf<Char>() // 維護子節點順序
    private val childrenMap = mutableMapOf<Char, UniformTrieNode>() // 快速查找
    private val candidates = mutableListOf<String>() // 終端候選字
    private val candidateScore = mutableMapOf<String, Int>() // 候選字分數
    private var parentFullPathChars: List<Char> = listOf() // 父節點字符列表
    private var currentFullPathChars: List<Char> = listOf() // 當前節點到根節點的完整字符路徑
    private var isRoot: Boolean = false // 標記是否為根節點
    private var selfScore: Int = 0 // 自身分數，預設為0

    // 快取排序的結果
    @Transient
    private var sortedChildrenKeysCache: List<Char>? = null // 排序的子節點鍵快取

    @Transient
    private var isChildrenKeysCacheDirty: Boolean = true // 標記子節點鍵快取是否髒

    @Transient
    private var sortedCandidatesCache: List<String>? = null // 排序的候選字快取

    @Transient
    private var isCandidatesCacheDirty: Boolean = true // 標記候選字快取是否髒

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

    /**
     * 是否為終端節點
     * @return boolean
     */
    fun isFinalNode(): Boolean {
        return candidates.isNotEmpty() // 有候選字即為終端節點
    }

    /**
     * 是否為非終端節點
     * @return boolean
     */
    fun isNotFinalNode(): Boolean {
        return candidates.isEmpty() // 無候選字即為非終端節點
    }

    /**
     * 取得子節點分數
     * @param char 子節點字符
     */
    fun getChildrenScore(char: Char): Int {
        return getChildNode(char)?.getSelfScore()?:0 // 返回子節點的分數，預設為 0
    }

    /**
     * 設置子節點分數
     * @param char 子節點字符
     * @param score 分數
     * @return 返回當前節點
     */
    fun setChildrenScore(char: Char, score: Int = 0): UniformTrieNode {
        if (childrenMap.containsKey(char)) {
            getChildNode(char)?.setSelfScore(score) // 設置子節點的分數(為了排序用)
            isChildrenKeysCacheDirty = true // 標記子節點鍵快取為髒
            sortedChildrenKeysCache = null // 清除排序快取
        }
        return this
    }

    /**
     * 設置當前節點的分數
     * @return 返回當前節點
     */
    fun setSelfScore(score: Int = 0): UniformTrieNode {
        selfScore = score // 設置自身分數
        return this
    }

    /**
     * 獲取自身分數
     * @return 返回自身分數
     */
    fun getSelfScore(): Int {
        return selfScore // 返回自身分數
    }

    /**
     * 設置候選字分數
     * @param candidate 候選字
     * @return score 分數，預設為 0
     */
    fun getCandidateScore(candidate: String): Int {
        return candidateScore.getOrDefault(candidate, 0) // 返回候選字的分數，預設為 0
    }

    /**
     * 設置候選字分數
     * @param candidate 候選字
     * @param score 分數
     * @return 返回當前節點
     */
    fun setCandidateScore(candidate: String, score: Int = 0): UniformTrieNode {
        if (candidate.isNotEmpty()) {
            candidateScore[candidate] = score // 設置候選字的分數
            isCandidatesCacheDirty = true // 標記候選字快取為髒
            sortedCandidatesCache = null // 清除排序快取
        }
        return this
    }

    /**
     * 提升子節點分數
     * 如果子節點不存在，則不進行推廣
     */
    fun promoteChild(char: Char) {
        if (!childrenMap.containsKey(char)) {
            return // 如果沒有這個子節點，則不進行推廣
        }
        setChildrenScore(char, getChildrenScore(char) + 1) // 增加子節點分數
    }

    /**
     * 提升候選字分數
     * 如果候選字不存在，則不進行推廣
     * @param candidate 候選字
     */
    fun promoteCandidate(candidate: String) {
        if (candidate.isEmpty()) {
            return // 如果候選字為空，則不進行推廣
        }
        setCandidateScore(candidate, getCandidateScore(candidate) + 1) // 增加候選字分數
    }

    /**
     * 獲取排序的子節點鍵列表，分數從高到低排序
     * @return 返回排序後的子節點鍵列表
     */
    fun getOrderedChildrenKeys(): List<Char> {
        if (isChildrenKeysCacheDirty || sortedChildrenKeysCache == null) {
            // 如果快取髒或未初始化，則重新計算排序
            sortedChildrenKeysCache = childrenKeys.sortedByDescending { getChildrenScore(it) }
            isChildrenKeysCacheDirty = false // 重置髒標記
        }
        return sortedChildrenKeysCache ?: emptyList() // 返回排序後的子節點鍵列表
    }

    /**
     * 獲取排序的候選字列表，分數從高到低排序
     * @return 返回排序後的候選字列表
     */
    fun getChildrenNodes(): List<UniformTrieNode> {
        return getOrderedChildrenKeys().mapNotNull { childrenMap[it] } // 返回子節點列表，分數高到低排序
    }

    /**
     * 獲取子節點
     * @param char 子節點字符
     * @return 返回子節點，如果不存在則返回null
     */
    fun getChildNode(char: Char): UniformTrieNode?{
        return childrenMap[char]
    }

    /**
     * 獲取當前節點的字符
     * @return 返回當前節點的字符
     */
    fun getKey(): Char {
        return char
    }

    /**
     * 獲取第一個子節點 (最高優先級別是分數最高的子節點)
     * @return 返回第一個子節點，如果沒有則返回null
     */
    fun getFirstNode(): UniformTrieNode? {
        return getChildrenNodes().firstOrNull() // 返回分數最高的子節點
    }

    /**
     * 獲取當前節點的完整候選字列表(按照分數高到低)
     * @return 返回排序後的候選字列表，分數從高到低排序
     */
    fun getOrderedCandidates(): List<String> {
        if (isCandidatesCacheDirty || sortedCandidatesCache == null) {
            // 如果快取髒或未初始化，則重新計算排序
            sortedCandidatesCache = candidates.sortedByDescending { getCandidateScore(it) }
            isCandidatesCacheDirty = false // 重置髒標記
        }
        return sortedCandidatesCache ?: emptyList() // 返回排序後的候選字列表
    }

    /**
     * 過濾候選字列表
     * @param list 要過濾的候選字列表
     * @param index 開始索引
     * @param limit 返回的候選字數量限制
     * @return 返回過濾後的候選字列表
     */
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
     * @return 返回候選字列表，優先級從分數最高到最低。
     */
    fun getPromoteCandidates(index: Int, limit: Int = 0): List<String>{
        if (candidates.isEmpty()) {
            return emptyList() // 如果沒有候選字，返回空列表
        }
        val promoted = getOrderedCandidates()
        return filterList(promoted, index, limit)
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

