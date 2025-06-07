package org.wbftw.weil.chinese_keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.wbftw.weil.chinese_keyboard.converter.JsonConverter
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

}