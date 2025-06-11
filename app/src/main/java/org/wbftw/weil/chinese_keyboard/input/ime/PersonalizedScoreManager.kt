package org.wbftw.weil.chinese_keyboard.input.ime

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.wbftw.weil.chinese_keyboard.input.utils.CandidateClass.ScoreData

/**
 * 個性化分數管理類
 */
object PersonalizedScoreManager {

    private val TAG = "PersonalizedScoreManager"
    private const val CAPACITY: Int = 2000
    private var valueChanged: Boolean = false

    private val scoreDatabase = object : LinkedHashMap<String, ScoreData>(CAPACITY, 0.5f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ScoreData>?): Boolean {
            // 如果超過容量，則移除最舊的條目
            var result = false
            if (eldest != null && size > CAPACITY) {
                println("Removing eldest entry: ${eldest.key} with score ${eldest.value.score}")
                result = true
                valueChanged = true
            }
            return result
        }
    }

    fun getInstance(): PersonalizedScoreManager {
        return this
    }

    /**
     * 獲取詞條的有效分數，不會更新訪問時間或計數
     */
    fun getEffectiveScore(path: String): Int {
        val data = scoreDatabase[path]
        if (data == null) {
            return 0
        }
        return data.calculateEffectiveScore()
    }

    /**
     * 獲取詞條的 Map
     */
    fun getScoreDatabase(): Map<String, ScoreData> {
        return scoreDatabase
    }

    /**
     * 設定詞條的分數和最後訪問時間
     */
    fun setPathScore(path: String, score: Int = 1, lastAccessTime: Long = System.currentTimeMillis()) {
        val existingData = scoreDatabase[path]
        if (existingData != null) {
            // 如果詞條已存在，則增加其分數並更新訪問時間
            existingData.score = score
            existingData.lastAccessTime = lastAccessTime
        } else {
            // 創建新分數條目
            val newData = ScoreData(
                score = score,
                lastAccessTime = lastAccessTime
            )
            scoreDatabase[path] = newData
        }
        valueChanged = true
    }

    /**
     * 移除詞條
     * @param path 詞條的完整路徑
     */
    fun removePath(path: String) {
        scoreDatabase.remove(path)
        valueChanged = true
    }

    /**
     * 提升詞條分數
     * 如果詞條已存在，則增加其分數並更新訪問時間
     * @param word 要提升分數的詞條(完整路徑:節點或詞)
     * @param additionalScore 要增加的分數，預設為 1。
     */
    fun promoteWordScore(word: String, additionalScore: Int = 1) {
        val currentTime = System.currentTimeMillis()
        val existingData = scoreDatabase[word]
        if (existingData != null) {
            existingData.score += additionalScore
            existingData.lastAccessTime = currentTime
        } else {
            // 創建新分數條目
            val newData = ScoreData(
                score = additionalScore,
                lastAccessTime = currentTime
            )
            scoreDatabase[word] = newData
        }
        valueChanged = true
    }

    /**
     * 保存到持久存儲
     */
    fun saveToJson(): String {
        // 實際應用中，這裡會序列化數據到文件或數據庫
        println("Saving ${scoreDatabase.size} entries to storage")
        val gson = Gson()
        val type = object : TypeToken<LinkedHashMap<String, ScoreData>>() {}.type
        return gson.toJson(scoreDatabase, type)
    }

    /**
     * 從JSON加載
     */
    fun loadFromJson(json: String) {
        // 實際應用中，這裡會從文件或數據庫讀取數據
        println("Loading data from json")
        val gson = Gson()
        val type = object : TypeToken<LinkedHashMap<String, ScoreData>>() {}.type
        val loadedData: LinkedHashMap<String, ScoreData> = gson.fromJson(json, type)
        scoreDatabase.clear() // 清除現有數據
        scoreDatabase.putAll(loadedData) // 加載新數據
        println("Loaded ${scoreDatabase.size} entries from json")
        valueChanged = false
    }

    /**
     * 清除分數數據庫
     * 注意：這將清除所有個性化分數數據
     */
    fun clearScoreDatabase() {
        scoreDatabase.clear()
        valueChanged = true
        println("Score database cleared")
    }

    /**
     * 檢查是否有數據變更
     */
    fun getValueChanged(): Boolean {
        return valueChanged
    }

    /**
     * 設置數據變更狀態
     * @param value 是否有數據變更
     */
    fun setValueChanged(value: Boolean) {
        valueChanged = value
    }

    /**
     * 獲取當前快取大小
     */
    fun getCacheSize(): Int = scoreDatabase.size

}