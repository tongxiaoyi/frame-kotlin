package com.oneliang.ktx.frame.ai.cnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.dnn.layer.Layer

open class DropoutLayer<IN : Any, OUT : Any>(
    val mapDepth: Int = 1,
    val inX: Int,
    val inY: Int,
    private val forwardImpl: ((layer: DropoutLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: DropoutLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val forwardResetImpl: ((layer: DropoutLayer<IN, OUT>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: DropoutLayer<IN, OUT>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: DropoutLayer<IN, OUT>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: DropoutLayer<IN, OUT>) -> String) = { Constants.String.BLANK }
) : Layer<IN, OUT>() {

    val outX = this.inX
    val outY = this.inY

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