package pt.isec.a2017014841.tp2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

const val SERVER_PORT = 9999

class GameViewModel: ViewModel(){
    enum class State {
        STARTING, PLAYING_BOTH, PLAYING_ME, PLAYING_OTHER, ROUND_ENDED, GAME_OVER
    }
    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }
    val connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    private var serverSocket: ServerSocket? = null
    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null

    private var threadComm: Thread? = null

    val state = MutableLiveData(State.STARTING)

    val numCli = Number

    fun startServer() {
        if (serverSocket != null ||
            socket != null ||
            connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return
        connectionState.postValue(ConnectionState.SERVER_CONNECTING)
        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.apply {
                try {
                    startComm(serverSocket!!.accept())
                } catch (_: Exception) {
                    connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                } finally {
                    serverSocket?.close()
                    serverSocket = null
                }
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
    }

    fun stopGame() {
        try {
            state.postValue(State.GAME_OVER)
            connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            threadComm?.interrupt()
            threadComm = null
        } catch (_: Exception) { }
    }

}
