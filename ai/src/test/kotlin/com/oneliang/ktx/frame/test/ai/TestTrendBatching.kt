package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import java.io.File

class TestTrendBatching(private val fullFilename:String, override val batchSize: Int) : Batching<Pair<Float, Array<Float>>>(batchSize) {

    private var reader = File(this.fullFilename).bufferedReader()

    private var lineCount = 0
    private var lineIndex = 0

    override fun reset() {
        this.lineCount = 0
        this.lineIndex = 0
        this.reader = File(this.fullFilename).bufferedReader()
    }

    private fun parseLine(line: String): Pair<Float, Array<Float>> {
        val rowDataList = line.split(Constants.Symbol.COMMA)
        var result = 0.0f
        val dataArray = Array(rowDataList.size - 1) { 0.0f }
        rowDataList.forEachIndexed { index: Int, string: String ->
            val value = string.trim().toFloat()
            if (index == 0) {
                result = value / 1000
            } else if (index in 1..2 || index == 4 || index == 6 || index in 10..15) {
                dataArray[index - 1] = value / 1000
            } else if (index == 5 || index in 7..9) {
                dataArray[index - 1] = value / 10000
            } else if (index == 16) {
                dataArray[index - 1] = value / 100000
            } else if (index == 17) {
                dataArray[index - 1] = value / 100
            } else {
                dataArray[index - 1] = value
            }
        }
        return result to dataArray
    }

    override fun fetch(): Result<Pair<Float, Array<Float>>> {
        var currentLineCount = 0
        var line = this.reader.readLine() ?: null
        val dataList = mutableListOf<Pair<Float, Array<Float>>>()
        while (line != null) {//break when finished
            if (line.isNotBlank()) {
                if (this.lineIndex == 0) {
                    this.lineIndex++
                    line = this.reader.readLine() ?: null
                    continue
                }
                dataList += parseLine(line)
                currentLineCount++
                this.lineIndex++
                this.lineCount++
                if (currentLineCount == this.batchSize) {
                    break
                }
            }
            line = this.reader.readLine() ?: null
        }
        return if (dataList.isEmpty()) {
            Result(true)
        } else {
            Result(false, dataList)
        }
    }
}