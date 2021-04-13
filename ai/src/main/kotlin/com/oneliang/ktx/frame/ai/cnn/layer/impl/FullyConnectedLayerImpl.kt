package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.calculateOutSize
import com.oneliang.ktx.frame.ai.cnn.layer.FullyConnectedLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer

open class FullyConnectedLayerImpl(
    mapDepth: Int//32
) : FullyConnectedLayer<Array<Array<Array<Double>>>, Array<Array<Array<Double>>>>(mapDepth) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double, training: Boolean): Array<Array<Array<Double>>> {
        return emptyArray()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double) {
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}