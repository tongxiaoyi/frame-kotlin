package com.oneliang.ktx.frame.ai.cnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.dnn.layer.LossLayer
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap

open class OutputLayer<IN : Any, OUT : Any, LOSS : Any>(
    val typeCount: Int,
    private val forwardImpl: ((layer: OutputLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: OutputLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val forwardResetImpl: ((layer: OutputLayer<IN, OUT, LOSS>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: OutputLayer<IN, OUT, LOSS>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: OutputLayer<IN, OUT, LOSS>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: OutputLayer<IN, OUT, LOSS>) -> String) = { Constants.String.BLANK },
) : LossLayer<IN, OUT, LOSS>() {

    var sumLoss = AtomicMap<String, DoubleWrapper>()//: Double = 0.0

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Double, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Double) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
    }

    override fun forwardResetImpl(dataId: Long) {
        this.forwardResetImpl?.invoke(this, dataId)
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
        this.updateImpl?.invoke(this, epoch, printPeriod, totalDataSize, learningRate)
    }

    override fun initializeLayerModelDataImpl(data: String) {
        this.initializeLayerModelDataImpl.invoke(this, data)
    }

    override fun saveLayerModelDataImpl(): String {
        return this.saveLayerModelDataImpl.invoke(this)
    }
}