package com.example.coroutinesockets

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class SocketClient {

    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var `in`: BufferedReader? = null
    private var ip: String = ""
    private var port: Int = 4444
    private val scope = CoroutineScope(Dispatchers.IO)
    private val flow = MutableSharedFlow<String>(replay = 1)
    private val executor = Executors.newSingleThreadExecutor()

    fun connect(ip: String, port: Int = 4444) {
        this.ip = ip
        this.port = port

        executor.submit { connect() }
    }

    fun disconnect() {
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

    fun read() = flow

    private fun connect() {
        try {
            if (socket == null) {
                socket = Socket(ip, port)
            } else {
                socket?.connect(InetSocketAddress.createUnresolved(ip, port))
            }

            out = PrintWriter(socket!!.getOutputStream(), true)
            executor.submit(Reader(socket!!))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private inner class Reader(val socket: Socket) : Runnable {

        override fun run() {
            `in` = BufferedReader(
                InputStreamReader(socket.getInputStream())
            )

            var inputLine: String
            while ((`in`!!.readLine().also { inputLine = it }) != null) {
                Log.d(TAG, inputLine)
                flow.tryEmit(inputLine)
            }
        }
    }

    companion object {
        const val TAG = "SocketClient"
    }
}