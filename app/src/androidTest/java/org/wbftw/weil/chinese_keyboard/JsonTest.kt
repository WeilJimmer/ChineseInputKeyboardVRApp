package org.wbftw.weil.chinese_keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.wbftw.weil.chinese_keyboard.converter.JsonConverter
import org.wbftw.weil.chinese_keyboard.input.ime.PersonalizedScoreManager
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass

class JsonTest {

    @Test
    fun testCandidatesPairToJson() {

        val charList = listOf('a', 'b', 'c')
        val candidatePair1 = CandidateClass.CandidatePair("候選字", charList)
        val candidatePair2 = CandidateClass.CandidatePair("候選字2", listOf('1', '2', '3'))
        val candidateList = listOf(candidatePair1, candidatePair2)

        var json = JsonConverter.charListToJson(charList)
        println(json)
        assertEquals(json, "[\"a\",\"b\",\"c\"]")

        json = JsonConverter.candidatesPairToJson(candidatePair1)
        println(json)
        assertEquals(json, "{\"w\":\"候選字\",\"p\":\"abc\"}")

        json = JsonConverter.candidatesPairToJson(candidatePair2)
        println(json)
        assertEquals(json, "{\"w\":\"候選字2\",\"p\":\"123\"}")

        val parsedPair = JsonConverter.parseCandidatePairFromJson(json)
        println(parsedPair)
        assertNotNull(parsedPair)
        assertEquals(parsedPair!!.candidateString, candidatePair2.candidateString)
        assertEquals(parsedPair.fullPath, candidatePair2.fullPath)

        json = JsonConverter.candidatesListToJson(candidateList)
        println(json)
        assertEquals(json, "[{\"w\":\"候選字\",\"p\":\"abc\"},{\"w\":\"候選字2\",\"p\":\"123\"}]")
    }

    @Test
    fun testScoreMap(){
        val personalizedScoreManager = PersonalizedScoreManager.getInstance()
        personalizedScoreManager.clearScoreDatabase()
        personalizedScoreManager.setPathScore("abc測", 1, 1749620089400)
        personalizedScoreManager.setPathScore("abd測", 1, 1749620089401)
        personalizedScoreManager.setPathScore("cde試", 1, 1749620089308)
        personalizedScoreManager.setPathScore("cde試", 2, 1749620089401)
        personalizedScoreManager.setPathScore("a試", 5, 1749620089405)

        val json: String = personalizedScoreManager.saveToJson()
        println(json)
        assertNotNull(json)
        assertEquals(json,"""{"abc測":{"t":1749620089400,"s":1},"abd測":{"t":1749620089401,"s":1},"cde試":{"t":1749620089401,"s":2},"a試":{"t":1749620089405,"s":5}}""")

        personalizedScoreManager.clearScoreDatabase()
        personalizedScoreManager.loadFromJson(json)

        val scoreMap = personalizedScoreManager.getScoreDatabase()
        assertNotNull(scoreMap)
        assertEquals(scoreMap.size, 4)
        assertEquals(scoreMap["abc測"]?.score, 1)
        assertEquals(scoreMap["abd測"]?.score, 1)
        assertEquals(scoreMap["cde試"]?.score, 2)
        assertEquals(scoreMap["a試"]?.score, 5)

        assertEquals(scoreMap["abc測"]?.lastAccessTime, 1749620089400)
        assertEquals(scoreMap["abd測"]?.lastAccessTime, 1749620089401)
        assertEquals(scoreMap["cde試"]?.lastAccessTime, 1749620089401)
        assertEquals(scoreMap["a試"]?.lastAccessTime, 1749620089405)

    }

}