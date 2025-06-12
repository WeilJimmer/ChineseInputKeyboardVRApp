package buildtools

import org.wbftw.weil.chinese_keyboard.input.utils.AssociativeWordNode
import org.wbftw.weil.chinese_keyboard.input.utils.UniformTrieNode
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.Queue

object TrigramBuilder {

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Build trigram dict ===")
        buildTries()
    }

    private fun buildTries() {

        try {
            // read the trigram file : dataset/trigram_output/output_000.txt ~ output_099.txt

            val trigramFileFolder = "dataset/trigram_output/"
            val trigramFilePrefix = "output_"
            val trigramFileSuffix = ".txt"

            val rootNode = AssociativeWordNode()

            var totalCount = 0L
            for (i in 0..10) {
                val fileName = "${trigramFileFolder}${trigramFilePrefix}${i.toString().padStart(3, '0')}${trigramFileSuffix}"
                println("Processing file: $fileName")
                var createCount = 0
                for (line in File(fileName).readLines()) {
                    val parts = line.split("\t")
                    if (parts.size >= 2) {
                        val word: CharArray = parts[0].toCharArray()
                        val frequency = parts[1].toIntOrNull() ?: 0
                        val result = addNodeRecursively(
                            rootNode,
                            word,
                            frequency
                        )
                        if (result == 0) {
                            createCount++
                        }
                    } else {
                        println("Invalid line format: $line")
                    }
                }
                totalCount += createCount.toLong()
                println("Created $createCount trigram node from file: $fileName")
            }

            println("Total trigrams processed: $totalCount")

            println("Total nodes created: ${countNodes(rootNode)}")

            println("[Calculate] calculating children score...")

            calculateScoresPostOrder(rootNode)

            println("Score of root node: ${rootNode.getScore()}")

            // 序列化字典樹
            val outputFile = File("app/src/main/assets/trigram.bin")
            serializeDictionaryTree(rootNode, outputFile)

            val serializedSize = outputFile.length()

            println("[Succ] serialized: ${outputFile.absolutePath}")
            println("[Succ] file size: $serializedSize bytes")

        } catch (e: Exception) {
            println("[Error]: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addNodeRecursively(
        rootNode: AssociativeWordNode,
        word: CharArray,
        frequency: Int,
        threshold: Int = 100
    ): Int {

        if (frequency >= threshold) {

            var currentNode = rootNode
            for (char in word) {
                currentNode = currentNode.addChild(char)
            }
            currentNode.setScore(frequency)

            return 0

        }

        return 1

    }

    private fun calculateScoresPostOrder(node: AssociativeWordNode): Int {
        // 先遞迴計算所有子節點
        val isFinalNode = node.isFinal()
        if (isFinalNode) {
            return node.getScore()
        }
        val childrenScore = node.getChildren().sumOf { child ->
            calculateScoresPostOrder(child)
        }
        // 再計算自己的分數
        node.setScore(childrenScore)
        return childrenScore
    }

    /**
     * 序列化字典樹為二進制格式
     * @param rootNode 字典樹根節點
     * @param outputFile 輸出文件
     */
    private fun serializeDictionaryTree(rootNode: AssociativeWordNode, outputFile: File) {
        outputFile.parentFile.mkdirs()

        val oos = ObjectOutputStream(BufferedOutputStream(FileOutputStream(outputFile)))

        // 直接序列化整個樹結構
        oos.writeObject(rootNode)

        oos.flush()
        oos.close()
    }

    fun countNodes(node: AssociativeWordNode): Int {
        return 1 + node.getChildren().sumOf { child ->
            countNodes(child)
        }
    }


}