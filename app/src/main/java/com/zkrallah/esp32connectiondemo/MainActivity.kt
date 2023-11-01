package com.zkrallah.esp32connectiondemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.zkrallah.esp32connectiondemo.databinding.ActivityMainBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.connectBtn.setOnClickListener {
            if (isNetworkAvailable() && binding.edtIp.text.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val ip = binding.edtIp.text.toString()
                        socket = Socket(ip, 80)
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
            if (socket != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val inputStream =
                            BufferedReader(InputStreamReader(socket!!.getInputStream()))
                        val receivedMessage = inputStream.readLine()
                        withContext(Dispatchers.Main) {
                            binding.responseTxt.text = receivedMessage
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            binding.responseTxt.text = "Error receiving message: $e"
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
        socket?.close()
    }

}