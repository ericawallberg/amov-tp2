package pt.isec.a2017014841.tp2.helpers

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class curlip {

       fun getIp() {
            val url = URL("http://ifconfig.me")
            try {
                BufferedReader(InputStreamReader(url.openStream(), "UTF-8")).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        println(line)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
}