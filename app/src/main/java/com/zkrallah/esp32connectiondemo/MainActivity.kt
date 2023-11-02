package com.zkrallah.esp32connectiondemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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

    companion object {
        private var started = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.connectBtn.setOnClickListener {
            if (isNetworkAvailable() && binding.edtIp.text.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val ip = binding.edtIp.text.toString()
                        val port = binding.edtPort.text.toString().toInt()
                        socket = Socket(ip, port)
                        val outputStream = DataOutputStream(socket!!.getOutputStream())
                        outputStream.writeUTF("Hello from Android!")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Connected successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("ConnectionErr", "onCreate: $e")
                        withContext(Dispatchers.IO) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error connecting : $e",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.disconnectBtn.setOnClickListener {
            socket?.close()
            started = false
        }

        binding.sendBtn.setOnClickListener {

            if (isNetworkAvailable() && socket != null && binding.edtMsg.text.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val outputStream = DataOutputStream(socket!!.getOutputStream())
                        val message = binding.edtMsg.text.toString()
                        outputStream.writeUTF(message)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Message sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("ConnectionErr", "onCreate: $e")
                        withContext(Dispatchers.IO) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error sending message $e",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.receiveBtn.setOnClickListener {
            if (socket != null && !started) {
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

                                started = true
                            }
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error receiving message: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    override fun onDestroy() {
        super.onDestroy()
        started = false
        socket?.close()
    }

}