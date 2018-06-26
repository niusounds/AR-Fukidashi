package com.niusounds.arfukidashi

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val ar: ArFragment
        get() = arFragment as ArFragment

    private val arSceneView: ArSceneView
        get() = ar.arSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recButton.setOnClickListener {
            onRecButtonTap()
        }
    }

    /**
     * RECボタンをタップしたときの処理。
     */
    private fun onRecButtonTap() {

        // 音声認識をする
        recognizeSpeech(onError = { errorCode ->
            recButton.visibility = View.VISIBLE

            Toast.makeText(this, "Error code: $errorCode", Toast.LENGTH_SHORT).show()

        }) { results ->

            // 結果が取得できたら吹き出しを作る
            if (results.isNotEmpty()) {
                createFukidashi(results[0])
            }

            showRecButton()
        }

        hideRecButton()
    }

    private fun hideRecButton() {
        recButton.visibility = View.GONE
    }

    private fun showRecButton() {
        recButton.visibility = View.VISIBLE
    }

    private fun recognizeSpeech(preferOffline: Boolean = true, onError: ((Int) -> Unit)? = null, callback: (List<String>) -> Unit) {

        // 音声認識の設定
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                onError?.invoke(error)
            }

            override fun onResults(results: Bundle) {
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                callback(recData)
            }
        })

        recognizer.startListening(intent)
    }

    private fun createFukidashi(text: String) {

        ViewRenderable.builder()
                .setView(this, R.layout.fukidashi)
                .build()
                .thenAccept { viewRenderable ->

                    viewRenderable.view.findViewById<TextView>(R.id.text).text = text

                    // 視線の前方のフキダシ出現位置を計算する
                    val forward = arSceneView.scene.camera.forward
                    val cameraPosition = arSceneView.scene.camera.worldPosition
                    val position = cameraPosition + forward

                    // フキダシの向きをカメラ方向に向ける。高さはフキダシの出現位置に合わせる
                    val direction = cameraPosition - position
                    direction.y = position.y

                    Node().apply {
                        worldPosition = position
                        renderable = viewRenderable
                        setParent(arSceneView.scene)
                        setLookDirection(direction)
                    }
                }
    }

    operator fun Vector3.plus(other: Vector3): Vector3 {
        return Vector3.add(this, other)
    }

    operator fun Vector3.minus(other: Vector3): Vector3 {
        return Vector3.subtract(this, other)
    }
}
