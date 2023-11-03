package com.zkrallah.esp32connectiondemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zkrallah.esp32connectiondemo.adapter.MessagesAdapter
import com.zkrallah.esp32connectiondemo.databinding.ActivityMainBinding
import com.zkrallah.esp32connectiondemo.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var socket: Socket? = null
    private val adapter = MessagesAdapter(mutableListOf())
    private var startedReceiving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.connectBtn.setOnClickListener {
            if (socket != null && !socket!!.isClosed) showToast("Already Connected !")
            else if (isNetworkAvailable() && binding.edtIp.text.isNotEmpty()) connectToEsp()
            else showToast("No network connection available!")
        }

        binding.disconnectBtn.setOnClickListener {
            socket?.close()
            startedReceiving = false
        }

        binding.sendBtn.setOnClickListener {
            if (socket != null && socket!!.isClosed) showToast("Socked is Closed !")
            else if (isNetworkAvailable() && socket != null && binding.edtMsg.text.isNotEmpty()) sendMessage()
            else showToast("No network connection available!")
        }

        binding.receiveBtn.setOnClickListener {
            if (socket != null && socket!!.isClosed) showToast("Socked is Closed !")
            else if (socket != null && !startedReceiving) startReceiving()
            else if (socket != null) showToast("Already Started Receiving Packets.")
            else showToast("No network connection available!")
        }
    }

    private fun connectToEsp() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val ip = binding.edtIp.text.toString()
                val port = binding.edtPort.text.toString().toInt()
                socket = Socket(ip, port)
                val outputStream = DataOutputStream(socket!!.getOutputStream())
                outputStream.writeUTF("Hello from Android!")
                if (socket!!.isConnected && !socket!!.isClosed) {
                    withContext(Dispatchers.Main) {
                        showToast("Connected successfully")
                    }
                }
            } catch (e: IOException) {
                Log.e("ConnectionErr", "onCreate: $e")
                withContext(Dispatchers.Main) {
                    showToast("Error connecting : $e")
                }
            }
        }
    }

    private fun sendMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val outputStream = DataOutputStream(socket!!.getOutputStream())
                val message = binding.edtMsg.text.toString()
                outputStream.writeUTF(message)
                withContext(Dispatchers.Main) {
                    showToast("Message sent successfully")
                }
            } catch (e: IOException) {
                Log.e("ConnectionErr", "onCreate: $e")
                withContext(Dispatchers.Main) {
                    showToast("Error sending message $e")
                }
            }
        }
    }

    private fun startReceiving() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream =
                    BufferedReader(InputStreamReader(socket!!.getInputStream()))
                while (socket!!.isConnected) {
                    val receivedMessage = inputStream.readLine()
                    withContext(Dispatchers.Main) {
                        adapter.addMessage(Message(receivedMessage))
                        binding.recyclerMessages.adapter = adapter
                        binding.recyclerMessages.layoutManager =
                            LinearLayoutManager(
                                this@MainActivity,
                                LinearLayoutManager.VERTICAL,
                                true
                            )

                        startedReceiving = true
                    }
                }

            } catch (e: IOException) {
                Log.e("ConnectionErr", "onCreate: $e")
                withContext(Dispatchers.Main) {
                    showToast("Error receiving message: $e")
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        startedReceiving = false
        socket?.close()
    }

}