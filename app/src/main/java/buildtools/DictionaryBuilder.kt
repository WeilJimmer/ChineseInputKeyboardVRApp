package buildtools
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode

import java.io.*

/**
 * 字典構建器
 * 構建字典樹並序列化為二進制格式
 */
object DictionaryBuilder {

    // 聲調字符 - 用於檢測路徑是否需要補充空格
    private val TONE_CHARS = setOf('6', '3', '4', '7')

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Build Dictionary ===")
        buildDictionary()
    }

    /**
     * 構建字典
     */
    fun buildDictionary() {
        try {
            val bopomofoPath = "app/src/main/assets/keyboard-ui/bopomofo.cin"
            println("[Info] Read file ... $bopomofoPath")

            // 讀取源字典文件
            val inputFile = File(bopomofoPath)

            if (!inputFile.exists()) {
                println("[Fail] Not Found: ${inputFile.absolutePath}")
                println("[Warn] Create example...")
                createExampleDictionary(inputFile)
                return
            }

            // 解析並構建字典樹
            val rootNode = buildDictionaryTree(inputFile)
            println("[Succ] Build dictionary tree successfully.")

            // 序列化字典樹
            val outputFile = File("app/src/main/assets/dictionary.bin")
            serializeDictionaryTree(rootNode, outputFile)

            val serializedSize = outputFile.length()

            println("[Succ] serialized: ${outputFile.absolutePath}")
            println("[Succ] file size: $serializedSize bytes")

        } catch (e: Exception) {
            println("[Error]: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 從字典文件構建字典樹
     * @param file 字典文件
     * @return 字典樹根節點
     */
    private fun buildDictionaryTree(file: File): UniformTrieNode {
        val rootNode = UniformTrieNode()
        val lines = FileInputStream(file).use { fis ->
            InputStreamReader(fis, Charsets.UTF_8).useLines { it.toList() }
        }

        println("[Info] Parsing ${lines.size} lines...")

        // 收集所有項目以便進行反向排序
        val entries = mutableListOf<Pair<String, String>>()

        // 處理每一行
        for (line in lines) {
            // 跳過空行和註釋
            if (line.isBlank() || line.startsWith("#")) continue

            val parts = line.trim().split("\\s+".toRegex(), 2)
            if (parts.size != 2) continue

            val (code, word) = parts

            // 處理路徑，確保結尾有聲調標記或空格
            val path = normalizeCode(code)

            // 添加到列表
            entries.add(Pair(path, word))
        }

        // 構建樹
        var entryCount = 0
        for ((path, word) in entries) {
            addWordToTree(rootNode, path, word)
            entryCount++

            // 打印進度
            if (entryCount % 10000 == 0) {
                println("[Info] Process $entryCount items...")
            }
        }

        println("[Succ] $entryCount items processed.")

        return rootNode
    }

    /**
     * 將詞添加到字典樹
     * @param rootNode 根節點
     * @param path 路徑
     * @param word 候選詞
     */
    private fun addWordToTree(rootNode: UniformTrieNode, path: String, word: String) {
        var currentNode = rootNode

        // 遍歷路徑字符
        for (char in path) {
            currentNode = currentNode.addChild(char)
        }

        // 添加候選詞
        currentNode.addCandidate(word)
    }

    /**
     * 正規化字碼，確保結尾有聲調標記或空格
     */
    private fun normalizeCode(code: String): String {
        return if (code.isNotEmpty() && !TONE_CHARS.contains(code.last())) {
            "$code " // 添加空格表示一聲
        } else {
            code
        }
    }


    /**
     * 序列化字典樹為二進制格式
     * @param rootNode 字典樹根節點
     * @param outputFile 輸出文件
     */
    private fun serializeDictionaryTree(rootNode: UniformTrieNode, outputFile: File) {
        outputFile.parentFile.mkdirs()

        val oos = ObjectOutputStream(BufferedOutputStream(FileOutputStream(outputFile)))

        // 直接序列化整個樹結構
        oos.writeObject(rootNode)

        oos.flush()
        oos.close()
    }

    /**
     * 創建示例字典
     */
    private fun createExampleDictionary(file: File) {
        file.parentFile.mkdirs()

        // 寫入示例字典內容
        val exampleContent = """
            # 注音字典示例文件
            # 格式: <字碼> <候選字>
            
            2k7 的
            2u4 的
            2u6 的
            g4 是
        """.trimIndent()

        file.writeText(exampleContent)
        println("[Succ] example file created: ${file.absolutePath}")
    }
}
