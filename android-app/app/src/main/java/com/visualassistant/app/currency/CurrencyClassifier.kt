package com.visualassistant.app.currency

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class CurrencyClassifier(context: Context) {

    companion object {
        private const val TAG = "CurrencyClassifier"

        private const val MODEL_FILE =
            "currency_v2_mobilenetv2.tflite"

        private const val INPUT_SIZE = 224
        private const val NUM_CLASSES = 6

        private val LABELS = arrayOf(
            "10",
            "20",
            "50",
            "100",
            "200",
            "500"
        )
    }

    private val interpreter: Interpreter

    init {
        val model = loadModel(context)

        interpreter = Interpreter(model)

        Log.d(
            TAG,
            "Currency V2 model loaded successfully"
        )

        Log.d(
            TAG,
            "Input shape: ${interpreter.getInputTensor(0).shape().contentToString()}"
        )

        Log.d(
            TAG,
            "Output shape: ${interpreter.getOutputTensor(0).shape().contentToString()}"
        )
    }

    private fun loadModel(context: Context): ByteBuffer {

        val fileDescriptor =
            context.assets.openFd(MODEL_FILE)

        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)

        val fileChannel =
            inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun classify(bitmap: Bitmap): Result {

        // -----------------------------------------
        // RESIZE
        // -----------------------------------------

        val resizedBitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                INPUT_SIZE,
                INPUT_SIZE,
                true
            )

        // -----------------------------------------
        // INPUT BUFFER
        // -----------------------------------------

        val inputBuffer =
            ByteBuffer.allocateDirect(
                1 *
                        INPUT_SIZE *
                        INPUT_SIZE *
                        3 *
                        4
            ).order(ByteOrder.nativeOrder())

        inputBuffer.rewind()

        // -----------------------------------------
        // MOBILENETV2 PREPROCESSING
        //
        // Training used:
        //
        // mobilenet_v2.preprocess_input()
        //
        // which converts:
        // [0,255] -> [-1,1]
        // -----------------------------------------

        for (y in 0 until INPUT_SIZE) {

            for (x in 0 until INPUT_SIZE) {

                val pixel =
                    resizedBitmap.getPixel(x, y)

                val r =
                    ((pixel shr 16) and 0xFF).toFloat()

                val g =
                    ((pixel shr 8) and 0xFF).toFloat()

                val b =
                    (pixel and 0xFF).toFloat()

                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        inputBuffer.rewind()

        // -----------------------------------------
        // OUTPUT
        // -----------------------------------------

        val output =
            Array(1) {
                FloatArray(NUM_CLASSES)
            }

        // -----------------------------------------
        // RUN MODEL
        // -----------------------------------------

        interpreter.run(
            inputBuffer,
            output
        )

        val probabilities =
            output[0]

        // -----------------------------------------
        // DEBUG LOG
        // -----------------------------------------

        Log.d(
            TAG,
            "MODEL RAN! " +
                    "10=${probabilities[0]}, " +
                    "20=${probabilities[1]}, " +
                    "50=${probabilities[2]}, " +
                    "100=${probabilities[3]}, " +
                    "200=${probabilities[4]}, " +
                    "500=${probabilities[5]}"
        )

        // -----------------------------------------
        // FIND BEST CLASS
        // -----------------------------------------

        var bestIndex = 0

        for (i in 1 until NUM_CLASSES) {

            if (
                probabilities[i] >
                probabilities[bestIndex]
            ) {
                bestIndex = i
            }
        }

        val denomination =
            LABELS[bestIndex]

        val confidence =
            probabilities[bestIndex]

        Log.d(
            TAG,
            "Prediction: ₹$denomination " +
                    "confidence=$confidence"
        )

        // -----------------------------------------
        // RESULT
        // -----------------------------------------

        return Result(
            denomination = denomination,
            confidence = confidence
        )
    }

    fun close() {
        interpreter.close()

        Log.d(
            TAG,
            "Currency V2 interpreter closed"
        )
    }

    data class Result(
        val denomination: String,
        val confidence: Float
    )
}