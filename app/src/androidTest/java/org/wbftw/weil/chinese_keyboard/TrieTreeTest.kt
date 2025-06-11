package org.wbftw.weil.chinese_keyboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.wbftw.weil.chinese_keyboard.input.ime.InputMethodPathOptimizer
import org.wbftw.weil.chinese_keyboard.input.ime.PersonalizedScoreManager
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import org.wbftw.weil.chinese_keyboard.utils.TrieHelper

@RunWith(AndroidJUnit4::class)
class TrieTreeTest {

    private val rootNode: UniformTrieNode = UniformTrieNode() // root

    init {

        // 初始化全局推廣樹

        // 建立樹結構

        // 樹結構如下所示：
        // 根|            根節點
        // 　|              |
        // 一|              1
        // 　|             / \
        // 　|            /   \
        // 　|           /     \
        // 　|          /       \
        // 　|         /         \
        // 二|        2           3
        // 　|       / \         / \
        // 　|      /   \       /   \
        // 　|     /     \     /     \
        // 三|    4       5   6       7
        // 　|  / | \    / \  |       |
        // 字| a  b  c  d   e f       g

        val node1 = rootNode.addChild('1') // 1 level
        val node12 = node1.addChild('2') // 2 level
        val node13 = node1.addChild('3') // 2 level
        val node124 = node12.addChild('4') // 3 level
        val node125 = node12.addChild('5') // 3 level
        val node136 = node13.addChild('6') // 3 level
        val node137 = node13.addChild('7') // 3 level

        // 添加候選字 [最早添加，排位越低。]

        node124.addCandidate("a")
        node124.addCandidate("b")
        node124.addCandidate("c")

        node125.addCandidate("d")
        node125.addCandidate("e")

        node136.addCandidate("f")

        node137.addCandidate("g")

    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.wbftw.weil.chinese_keyboard.dev", appContext.packageName)
    }

    @Test
    fun testGetCandidateList() {
        // 測試獲取候選字列表

        PersonalizedScoreManager.getInstance().clearScoreDatabase()

        val candidates1 = TrieHelper.getCandidateList("1", rootNode)
        println(candidates1)
        assert(candidates1 == listOf("a", "b", "c"))

        val candidates12 = TrieHelper.getCandidateList("12", rootNode)
        println(candidates12)
        assert(candidates12 == listOf("a", "b", "c"))

        val candidates124 = TrieHelper.getCandidateList("124", rootNode)
        println(candidates124)
        assert(candidates124 == listOf("a", "b", "c"))
    }

    @Test
    fun testPromoteUsedPath() {
        // 測試推廣已使用的路徑

        PersonalizedScoreManager.getInstance().clearScoreDatabase()

        InputMethodPathOptimizer.promoteUsedPath("124".toList(), rootNode, "b")

        // 檢查推廣後的候選字列表
        val candidates124AfterPromotion = TrieHelper.getCandidateList("124", rootNode)
        println(candidates124AfterPromotion)
        assert(candidates124AfterPromotion == listOf("b", "a", "c")) // b 被提升，會顯示在第一個

        val candidates12AfterPromotion = TrieHelper.getCandidateList("12", rootNode)
        println(candidates12AfterPromotion)
        assert(candidates12AfterPromotion == listOf("b", "a", "c")) // 同樣的推廣影響到父節點

        val candidates1AfterPromotion = TrieHelper.getCandidateList("1", rootNode)
        println(candidates1AfterPromotion)
        assert(candidates1AfterPromotion == listOf("b", "a", "c")) // 同樣的推廣影響到根節點
    }

    @Test
    fun testPromotePath() {
        PersonalizedScoreManager.getInstance().clearScoreDatabase()

        // 測試推廣路徑
        InputMethodPathOptimizer.promoteUsedPath("137".toList(), rootNode, "g")

        // 檢查推廣後的候選字列表
        val candidates137AfterPromotion = TrieHelper.getCandidateList("137", rootNode)
        println(candidates137AfterPromotion)
        assert(candidates137AfterPromotion == listOf("g")) // g 被提升到最後一位

        val candidates13AfterPromotion = TrieHelper.getCandidateList("13", rootNode)
        println(candidates13AfterPromotion)
        assert(candidates13AfterPromotion == listOf("g")) // 同樣的推廣影響到父節點

        val candidates1AfterPromotion = TrieHelper.getCandidateList("1", rootNode)
        println(candidates1AfterPromotion)
        assert(candidates1AfterPromotion == listOf("g"))
    }

}