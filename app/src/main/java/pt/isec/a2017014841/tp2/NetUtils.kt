package com.github.onikenx.networkandroid

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

private const val LIMIT = 4096

object NetUtils {

    private fun getData(strUrl: String?): String? {
        val resp = StringBuilder()
        var conn: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            val code: Int = conn.responseCode
            if (code == HttpURLConnection.HTTP_OK /*200*/) {
                var count = 0
                conn.inputStream.bufferedReader().forEachLine {
                    count++
                    if (count >= LIMIT)
                        return@forEachLine
                    resp.append(it + "\n")
                }
            } else {
                resp.append("Error: $code")
            }
        } catch (_: Exception) {
            return null
        } finally {
            conn?.inputStream?.close()
            conn?.disconnect()
        }
        return resp.toString()
    }



    fun getDataAsync(strURL: String, result: MutableLiveData<String?>) {
        thread {
            val strContent = getData(strURL)
            result.postValue(strContent)
        }
    }
    fun verifyNetworkState(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = connMgr?.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected)
            return true
        return false
    }

    fun verifyNetworkStateV2(context: Context):Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkCapabilities(network).apply {
                if (this?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                        this?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
                    return true
            }
        }
        return false
    }

}