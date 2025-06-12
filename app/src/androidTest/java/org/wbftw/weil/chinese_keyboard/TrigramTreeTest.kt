package org.wbftw.weil.chinese_keyboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.wbftw.weil.chinese_keyboard.input.utils.AssociativeWordNode
import org.wbftw.weil.chinese_keyboard.loader.DictionaryLoader

@RunWith(AndroidJUnit4::class)
class TrigramTreeTest {
    private var rootNode: AssociativeWordNode? = AssociativeWordNode() // root

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.wbftw.weil.chinese_keyboard.dev", appContext.packageName)
    }

    @Test
    fun testWord() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rootNode = DictionaryLoader.loadTrigramDictionary(context)

        assertNotNull(rootNode)

        println(rootNode!!.getChildren().map { it.char })

        println(rootNode!!.getChild('的')!!.getChildren().map { it.char })

        // Test for a specific word
        rootNode!!.getCandidateWords("的").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '的': $candidates")
            assert(candidates.isNotEmpty())
        }
        rootNode!!.getCandidateWords("好").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '好': $candidates")
            assert(candidates.isNotEmpty())
        }
        rootNode!!.getCandidateWords("天").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '天': $candidates")
            assert(candidates.isNotEmpty())
        }
        rootNode!!.getCandidateWords("天氣").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '天氣': $candidates")
            assert(candidates.isNotEmpty())
        }
        rootNode!!.getCandidateWords("的女").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '的女': $candidates")
            assert(candidates.isNotEmpty())
        }
        rootNode!!.getCandidateWords("女孩").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '女孩': $candidates")
            assert(candidates.isNotEmpty())
        }
        rootNode!!.getCandidateWords("孩子").let { candidates ->
            assertNotNull(candidates)
            println("Candidates for '孩子': $candidates")
            assert(candidates.isNotEmpty())
        }



    }

}