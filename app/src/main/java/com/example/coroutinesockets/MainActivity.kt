package com.example.coroutinesockets

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val socketClient = SocketClient()
    private val socketServer = SocketServer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.connect)?.setOnClickListener {
            socketServer.open()
            socketClient.connect(
                findViewById<EditText>(R.id.ip).text.toString()
            )
            MainScope().launch {
               socketServer.read().collect {
                   findViewById<TextView>(R.id.input).text = it
               }
            }
            MainScope().launch {
                socketClient.read().collect {
                    findViewById<TextView>(R.id.input).text = it
                }
            }
        }

        findViewById<View>(R.id.send)?.setOnClickListener {
            socketClient.send(findViewById<EditText>(R.id.output).text.toString())
            socketServer.send(findViewById<EditText>(R.id.output).text.toString())
        }
    }

    override fun onDestroy() {
        socketClient.disconnect()
        socketServer.close()
        super.onDestroy()
    }
}