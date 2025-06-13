package org.wbftw.weil.chinese_keyboard.input.ime

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wbftw.weil.chinese_keyboard.input.utils.AssociativeWordNode
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import org.wbftw.weil.chinese_keyboard.loader.DictionaryLoader

class InputMethodAssociativeWordManager (
    private val context: android.content.Context,
    private val rootNode: UniformTrieNode
) {

    private val TAG = "InputMethodAssociativeWordManager"
    private var trigramRootNode: AssociativeWordNode? = null
    private var isReady = false
    private var loadJob: Job? = null

    init {
        loadTrigramDataInBackground()
    }
    private fun loadTrigramDataInBackground() {
        // 防止重複加載
        if (loadJob?.isActive == true || isReady) {
            return
        }
        loadJob = CoroutineScope(Dispatchers.IO).launch {
            delay(10)
            try {
                Log.d(TAG, "Starting to load trigram.bin")
                trigramRootNode = DictionaryLoader.loadTrigramDictionary(context)
                if (trigramRootNode != null) {
                    isReady = true
                    Log.d(TAG, "Trigram dictionary loaded successfully. Manager is ready.")
                } else {
                    Log.e(TAG, "Failed to load trigram dictionary.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading trigram dictionary: ${e.message}", e)
            }
        }
    }

    /**
     * 根據輸入的字元序列獲取聯想詞建議。
     *
     * @param inputString 一個包含1到2個中文字元的列表。
     * @return 一個按照分數從高到低排序的聯想字元列表。如果尚未準備好或輸入無效，則返回空列表。
     */
    fun getAssociativeWords(inputString: String): List<Char> {
        if (!isReady) {
            Log.w(TAG, "Trigram data is not ready yet.")
            return emptyList()
        }

        if (inputString.isEmpty()) {
            Log.w(TAG, "Input characters is empty.")
            return emptyList()
        }

        var finalInputString = inputString
        if (inputString.length > 2) {
            finalInputString = inputString.takeLast(2) // 只取最後兩個字元
        }

        Log.d(TAG, "Getting associative words for: $finalInputString")

        return trigramRootNode?.getCandidateWords(finalInputString) ?: emptyList()
    }

    /**
     * 檢查管理器是否已準備好提供聯想詞。
     * @return 如果trigram數據已加載，則為true，否則為false。
     */
    fun isManagerReady(): Boolean {
        return isReady
    }

    /**
     * 取消後台加載任務（如果正在進行）。
     */
    fun cancelLoading() {
        loadJob?.cancel()
        Log.d(TAG, "Trigram loading cancelled.")
    }
}