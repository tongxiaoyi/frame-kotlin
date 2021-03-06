package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import java.io.File

class TestBatching(override val batchSize: Int) : Batching<Pair<Float, Array<Float>>>(batchSize) {

    private var reader = File("/C:/Users/Administrator/Desktop/temp/data.csv").bufferedReader()

    init {
        //a=2,b=3, y=ax0+bx1
        val test1 = 13.0 to arrayOf(2.0, 3.0) //x0=2,x1=3,y=13
        val test2 = 23.0 to arrayOf(4.0, 5.0)
        val test3 = 33.0 to arrayOf(6.0, 7.0)
        val test4 = 43.0 to arrayOf(8.0, 9.0)
        val dataList = listOf(test1, test2, test3, test4)

    }

    private var lineCount = 0
    override fun reset() {
        this.lineCount = 0
        this.reader = File("/C:/Users/Administrator/Desktop/temp/data.csv").bufferedReader()
    }

    private fun parseLine(line: String): Pair<Float, Array<Float>> {
        val rowDataList = line.split(Constants.Symbol.COMMA)
        return rowDataList[0].toFloat() to arrayOf(rowDataList[1].toFloat(), rowDataList[2].toFloat())
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
            Result(true)
        } else {
            Result(false, dataList)
        }
    }
}