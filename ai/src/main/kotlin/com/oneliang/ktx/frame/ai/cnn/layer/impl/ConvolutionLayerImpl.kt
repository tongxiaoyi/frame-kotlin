package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.activation.sigmoid
import com.oneliang.ktx.frame.ai.cnn.layer.ConvolutionLayer
import com.oneliang.ktx.util.common.doubleIteration
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.math.matrix.add
import com.oneliang.ktx.util.math.matrix.innerProduct

class ConvolutionLayerImpl(
    previousLayerMapDepth: Int,//1
    mapDepth: Int,//32
    inX: Int,
    inY: Int,
    x: Int,
    y: Int,
    padding: Int = 0,
    stride: Int = 1
) : ConvolutionLayer<Array<Array<Array<Float>>>, Array<Array<Array<Float>>>>(previousLayerMapDepth, mapDepth, inX, inY, x, y, padding, stride) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        val outputNeuron = Array(this.mapDepth) { Array(this.outY) { Array(this.outX) { 0.0f } } }
        singleIteration(this.mapDepth) { mapIndex ->
            var mapValuesSum: Array<Array<Float>>? = null
            //先计算上一层每个图层与卷积的内积(convolutional()含了内积),然后将上一层各个图层卷积后的内积值求和
            singleIteration(this.previousLayerMapDepth) { parentMapIndex ->
                val maps = inputNeuron[parentMapIndex]
                val filters = this.filters[parentMapIndex][mapIndex]
                val rows = this.outY //28-5+1=24
                val columns: Int = this.outX //28-5+1=24
                val values = convolutional(rows, columns, maps, filters)
                mapValuesSum = mapValuesSum?.add(values) ?: values
            }
            //求和后的每个值进行一次sigmoid
            outputNeuron[mapIndex] = sigmoidBias(mapValuesSum!!, this.biases[mapIndex])
        }
//        println("output data size:[%s][%s][%s]".format(outputNeuron.size, outputNeuron[0].size, outputNeuron[0][0].size))
        return outputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float) {
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }

    private fun convolutional(rows: Int, columns: Int, maps: Array<Array<Float>>, filters: Array<Array<Float>>): Array<Array<Float>> { //map是原图,filters是卷积核,过滤器
        val result = Array(rows) { Array(columns) { 0.0f } }
        doubleIteration(rows, columns) { row, column ->
            result[row][column] = maps.innerProduct(filters, row, column)
        }
        return result
    }

    private fun sigmoidBias(mapValues: Array<Array<Float>>, bias: Float): Array<Array<Float>> {
        doubleIteration(mapValues.size, mapValues[0].size) { row, column ->
            val value = mapValues[row][column] + bias
            mapValues[row][column] = sigmoid(value)//1 / (1 + exp(-bias - m[row][column]))
        }
        return mapValues
    }
}