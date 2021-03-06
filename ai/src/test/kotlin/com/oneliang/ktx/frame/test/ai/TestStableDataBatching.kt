package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.base.Batching

class TestStableDataBatching(override val batchSize: Int) : Batching<Pair<Double, Array<Double>>>(batchSize) {

    private val dataList: List<Pair<Double, Array<Double>>>
    private var fetchTimes = 0

    init {
        //a=2,b=3, y=ax0+bx1
        val test1 = 12.0 to arrayOf(1.0, 1.0, 1.0)
        val test2 = 38.0 to arrayOf(2.0, 4.0, 4.0)
        val test3 = 10.0 to arrayOf(0.0, 1.0, 2.0)
        dataList = listOf(test1, test2, test3)

    }

    override fun reset() {
        this.fetchTimes = 0
    }

    override fun fetch(): Result<Pair<Double, Array<Double>>> {
        return if (this.fetchTimes == 0) {
            this.fetchTimes++
            Result(false, dataList)
        } else {
            Result(true)
        }
    }
}