package org.wbftw.weil.chinese_keyboard.loader

import android.content.Context
import org.wbftw.weil.chinese_keyboard.input.utils.AssociativeWordNode
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import java.io.BufferedInputStream
import java.io.ObjectInputStream

/**
 * 字典加載器
 * 從二進位文件加載字典樹
 */
object DictionaryLoader {
    /**
     * 從二進位文件加載字典
     * @param context 應用上下文
     * @return 字典樹根節點
     */
    fun loadDictionary(context: Context): UniformTrieNode? {
        try {
            // 從assets加載字典
            val inputStream = context.assets.open("dictionary.bin")
            val ois = ObjectInputStream(BufferedInputStream(inputStream))

            // 讀取整個樹結構
            val rootNode = ois.readObject() as UniformTrieNode
            ois.close()

            return rootNode

        } catch (e: Exception) {
            println("[Error] when loading.. : ${e.message}")
            e.printStackTrace()
            // 返回空的根節點
            return null
        }
    }

    fun loadTrigramDictionary(context: Context): AssociativeWordNode? {
        try {
            // 從assets加載3gram字典
            val inputStream = context.assets.open("trigram.bin")
            val ois = ObjectInputStream(BufferedInputStream(inputStream))

            // 讀取整個樹結構
            val rootNode = ois.readObject() as AssociativeWordNode
            ois.close()

            return rootNode

        } catch (e: Exception) {
            println("[Error] when loading trigram dictionary: ${e.message}")
            e.printStackTrace()
            // 返回空的根節點
            return null
        }
    }
}