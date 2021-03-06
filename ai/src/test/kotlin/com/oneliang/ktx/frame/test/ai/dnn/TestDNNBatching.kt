package com.oneliang.ktx.frame.test.ai.dnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.util.common.toArray
import java.io.File

class TestDNNBatching(private val fullFilename: String, override val batchSize: Int) : Batching<Pair<Float, Array<Float>>>(batchSize) {

    private var reader = File(fullFilename).bufferedReader()

    private var lineCount = 0
    override fun reset() {
        this.lineCount = 0
        this.reader = File(fullFilename).bufferedReader()
    }

    private fun parseLine(line: String): Pair<Float, Array<Float>> {
        val rowDataList = line.split(Constants.Symbol.COMMA)
        return rowDataList[0].toFloat() to rowDataList.subList(1, rowDataList.size).toArray { it.toFloat() }
    }

    override fun fetch(): Result<Pair<Float, Array<Float>>> {
        var currentLineCount = 0
        var line = reader.readLine() ?: null
        val dataList = mutableListOf<Pair<Float, Array<Float>>>()
        while (line != null) {//break when finished
            if (line.isNotBlank()) {
                dataList += parseLine(line)
                currentLineCount++
                lineCount++
                if (currentLineCount == batchSize) {
                    break
                }
            }
            line = reader.readLine() ?: null
        }
        return if (dataList.isEmpty()) {
            reader.close()
            Result(true)
        } else {
            Result(false, dataList)
        }
    }
}