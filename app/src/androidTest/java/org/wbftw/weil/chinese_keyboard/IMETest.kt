package org.wbftw.weil.chinese_keyboard

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.wbftw.weil.chinese_keyboard.input.ime.InputMethodCandidateManager
import org.wbftw.weil.chinese_keyboard.loader.DictionaryLoader
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import org.wbftw.weil.chinese_keyboard.utils.TrieHelper

class IMETest {

    /**
     * 測試用的字典加載器
     * 這個類別用於測試時加載字典
     */

    val dictionaryLoader = DictionaryLoader
    var dictionary: UniformTrieNode? = null

    fun getContext(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * 初始化字典
     */
    init {
        println("=== TestLoader Init ===")
        dictionary = dictionaryLoader.loadDictionary(getContext())
        if (dictionary == null) {
            println("[Error] Dictionary is null after loading.")
        } else {
            println("[Succ] Dictionary loaded successfully.")
        }
    }

    @Test
    fun testLoadDictionary() {
        // 測試字典是否正確加載
        assert(dictionary != null) { "Dictionary should not be null after loading." }
        println("[Test] Dictionary loaded successfully in test.")
    }

    @Test
    fun testGetNode(){
        // 測試獲取節點
        assert(dictionary != null) { "Dictionary should not be null after loading." }
        val node_j = dictionary?.getChildNode('j')
        assert(node_j != null) { "Node should not be null." }
        println("[Test] Node 'j' loaded successfully: $node_j")
        val node_ji = node_j?.getChildNode('i')
        assert(node_ji != null) { "Node 'ji' should not be null." }
        println("[Test] Node 'ji' loaded successfully: $node_ji")
        val node_ji3 = node_ji?.getChildNode('3')
        assert(node_ji3 != null) { "Node 'ji3' should not be null." }
        println("[Test] Node 'ji3' loaded successfully: $node_ji3")
    }

    @Test
    fun testGetCandidateList() {
        // 測試獲取候選字列表
        assert(dictionary != null) { "Dictionary should not be null after loading." }
        val candidates = TrieHelper.getCandidateList("ji3", dictionary ?: UniformTrieNode())
        println("[Test] Candidates for 'ji3': $candidates")
        assert(candidates.isNotEmpty()) { "Candidates should not be empty." }

        val candidates2 = TrieHelper.getCandidateList("e0", dictionary ?: UniformTrieNode())
        println("[Test] Candidates for 'e0': $candidates2")
        assert(candidates2.isNotEmpty()) { "Candidates for 'e0' should not be empty." }

        val candidates3 = TrieHelper.getCandidateList("j", dictionary ?: UniformTrieNode())
        println("[Test] Candidates for 'j': $candidates3")
        assert(candidates3.isNotEmpty()) { "Candidates for 'j' should not be empty." }

    }

    @Test
    fun testGetCandidateListCurrentPage() {
        // 測試獲取候選字列表，帶有分頁
        assert(dictionary != null) { "Dictionary should not be null after loading." }

        dictionary?.let{
            // 候選字管理器
            val pageConfigure = CandidateClass.PageConfigure()
            val candidateManager = InputMethodCandidateManager(dictionary!!, pageConfigure)

            // 當前候選字列表(帶路徑信息)
            var candidatesPair: List<CandidateClass.CandidatePair> = emptyList()

            candidateManager.setInput("ji3")

            candidatesPair = candidateManager.getCurrentPageCandidates()
            println("[Test] Candidates with path for 'ji3': $candidatesPair")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'ji3' should not be empty." }

            candidateManager.setInput("e0")
            candidatesPair = candidateManager.getCurrentPageCandidates()
            println("[Test] Candidates with path for 'e0': $candidatesPair")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'e0' should not be empty." }

            candidateManager.setInput("j")
            candidatesPair = candidateManager.getCurrentPageCandidates()
            println("[Test] Candidates with path for 'j': $candidatesPair")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'j' should not be empty." }

            candidateManager.setInput("u")
            candidatesPair = candidateManager.getCurrentPageCandidates()
            println("[Test] Candidates with path for 'u': $candidatesPair")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'u' should not be empty." }

        }

    }

    @Test
    fun testGetCandidateListNextPage(){
        assert(dictionary != null) { "Dictionary should not be null after loading." }

        dictionary?.let{
            val pageConfigure = CandidateClass.PageConfigure( perPageSize = 3, maxCandidatesPerPath = 10)
            val candidateManager = InputMethodCandidateManager(dictionary!!, pageConfigure)

            var candidatesResult : CandidateClass.CandidateResult
            var candidatesPair: List<CandidateClass.CandidatePair> = emptyList()

            candidateManager.setInput("u")

            candidatesResult = candidateManager.getCurrentPageCandidatesFullInformation()
            candidatesPair = candidatesResult.candidates.map { CandidateClass.CandidatePair.from(it) }
            println("[Test] Candidates with path for 'u' (Page 1): $candidatesPair")
            println("[Test] Has next page: ${candidatesResult.hasNextPage}, Current page: ${candidatesResult.currentPage}")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'u' should not be empty." }

            // 獲取下一頁
            candidatesResult = candidateManager.getNextPageCandidatesFullInformation()
            candidatesPair = candidatesResult.candidates.map { CandidateClass.CandidatePair.from(it) }
            println("[Test] Candidates with path for 'u' (Page 2): $candidatesPair")
            println("[Test] Has next page: ${candidatesResult.hasNextPage}, Current page: ${candidatesResult.currentPage}")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'u' should not be empty." }

            // 獲取第三頁
            candidatesResult = candidateManager.getNextPageCandidatesFullInformation()
            candidatesPair = candidatesResult.candidates.map { CandidateClass.CandidatePair.from(it) }
            println("[Test] Candidates with path for 'u' (Page 3): $candidatesPair")
            println("[Test] Has next page: ${candidatesResult.hasNextPage}, Current page: ${candidatesResult.currentPage}")
            assert(candidatesPair.isNotEmpty()) { "Candidates with path for 'u' should not be empty." }

        }
    }


}