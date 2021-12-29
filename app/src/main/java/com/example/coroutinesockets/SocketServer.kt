package com.example.coroutinesockets

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class SocketServer {

    private var socket: ServerSocket? = null
    private var out: PrintWriter? = null
    private var `in`: BufferedReader? = null
    private var port = 4444
    private var inputLine = "0"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val executor = Executors.newCachedThreadPool()
    private val flow = MutableSharedFlow<String>(replay = 1)

    fun open() {
        executor.submit {
            socket = ServerSocket(port)

            while (true) {
                executor.submit(Sender(socket!!.accept()))
            }
        }
    }

    fun close() {
        if (socket?.isClosed == false) {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun send(message: String) {
        scope.launch {
            out?.println(message)
        }
    }

    fun read(): Flow<String> = flow

    private inner class Sender(val socket: Socket) : Runnable {

        override fun run() {
            out = PrintWriter(socket.getOutputStream(), true)
            out?.println("Test")

            executor.submit(Reader(socket))
        }

    }

    private inner class Reader(val socket: Socket) : Runnable {

        override fun run() {
            try {
                `in` = BufferedReader(
                    InputStreamReader(socket.getInputStream())
                )

                while ((`in`!!.readLine().also { inputLine = it }) != null) {
                    Log.d(TAG, inputLine)
                    flow.tryEmit(inputLine)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    companion object {
        const val TAG = "SocketServer"
    }
}