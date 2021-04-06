package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.file.fileExists
import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.file.write
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

object Trainer {
    private val logger = LoggerManager.getLogger(Trainer::class)

    fun train(
        batching: Batching,
        neuralNetwork: NeuralNetwork,
        learningRate: Double,
        epochs: Int,
        printPeriod: Int = 500,
        modelFullFilename: String = Constants.String.BLANK
    ) {
        val layerList = neuralNetwork.getLayerList()
        val (inputLayer, outputLayer, model) = getInputAndOutputLayer(layerList, modelFullFilename)
        for (epoch in 1..epochs) {
            var totalDataSize = 0L
            while (true) {
                val result = batching.fetch()
                if (result.finished) {
                    batching.reset()
                    break
                }
                val inputDataList = result.dataList
                totalDataSize += inputDataList.size
                inputDataList.forEach {
                    val (y, xArray) = it
                    //forward include backward(backPropagation)
                    forward(inputLayer, xArray, y, true)
                    backward(outputLayer, y)
                }
            }
            //update all weight, gradient descent
            update(layerList, epoch, printPeriod, totalDataSize, learningRate)
            if (epoch % printPeriod == 0) {
                saveModel(layerList, modelFullFilename, (model?.times ?: 0) + epoch)
                logger.debug("times:%s, total data size:%s", epoch, totalDataSize)
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun getInputAndOutputLayer(layerList: List<Layer<*, *>>, modelFullFilename: String = Constants.String.BLANK): Triple<Layer<Any, Any>, Layer<Any, Any>, Model?> {
        var model: Model? = null
        if (modelFullFilename.isNotBlank() && modelFullFilename.fileExists()) {
            val modelJson = modelFullFilename.toFile().readContentIgnoreLine()
            model = modelJson.jsonToObject(Model::class)
        }
        val layerModelMap = model?.layerModels?.toMap { it.index to it } ?: emptyMap()
        var inputLayer: Layer<Any, Any>? = null
        var outputLayer: Layer<Any, Any>? = null
        var inputLayerCount = 0
        for (layerIndex in 0 until layerList.size - 1) {//no need to iterate last layer
            val layer = layerList[layerIndex] as Layer<Any, Any>
            val nextLayer = layerList[layerIndex + 1] as Layer<Any, Any>
            layer.nextLayer = nextLayer
            if (layer.previousLayer == null) {
                inputLayerCount++
                inputLayer = layer
            }
            if (nextLayer.nextLayer == null) {//last layer, output layer
                outputLayer = nextLayer
            }
            logger.info("this:%s, previousLayer:%s, nextLayer:%s", layer, layer.previousLayer, layer.nextLayer)
            val layerLabel = layerIndex + 1
            val layerModel = layerModelMap[layerIndex]
            if (layerModel == null) {
                logger.info("no model for layer:%s initialize", layerLabel)
                continue
            }
            logger.info("initialize layer:%s data:%s", layerLabel, layerModel.data)
            layer.initializeLayerModelData(layerModel.data)
        }
        if (inputLayer == null) {
            error("input layer is null, previous layer is null can be a input layer")
        }
        if (inputLayerCount > 1) {
            error("input layer is more than one, now size:%s, only support one input layer".format(inputLayerCount))
        }
        return Triple(inputLayer, outputLayer!!, model)
    }

    @Suppress("UNCHECKED_CAST")
    private fun forward(inputLayer: Layer<Any, Any>, xArray: Array<Double>, y: Double, training: Boolean) {
        inputLayer.doForward(xArray, y, training)
    }

    @Suppress("UNCHECKED_CAST")
    private fun backward(outputLayer: Layer<Any, Any>, y: Double) {
        outputLayer.doBackward(y)
    }

    private fun update(layerList: List<Layer<*, *>>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            layer.update(epoch, printPeriod, totalDataSize, learningRate)
        }
    }

    private fun saveModel(layerList: List<Layer<*, *>>, fullFilename: String, epochs: Int) {
        if (fullFilename.isBlank()) {
            return
        }
        val list = mutableListOf<Model.LayerModel>()
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            list += Model.LayerModel().apply {
                this.index = layerIndex
                this.data = layer.getLayerModelData()
            }
        }
        val model = Model(epochs, list.toTypedArray())
        fullFilename.toFile().write(model.toJson().toByteArray())
    }

    fun test(
        batching: Batching,
        neuralNetwork: NeuralNetwork,
        modelFullFilename: String = Constants.String.BLANK,
    ) {
        while (true) {
            val result = batching.fetch()
            if (result.finished) {
                logger.warning("Data is empty. Batch may be finished")
                break
            }
            val layerList = neuralNetwork.getLayerList()
            val (inputLayer, _) = getInputAndOutputLayer(layerList, modelFullFilename)

            val inputDataList = result.dataList

            inputDataList.forEach { item ->
                val (y, xArray) = item
                forward(inputLayer, xArray, y, false)
            }
        }
    }
}