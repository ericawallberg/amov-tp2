package pt.isec.a2017014841.tp2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

const val SERVER_PORT = 9999
const val MOVE_NONE = 0

class LoadingServerViewModel : ViewModel() {
    enum class State {
        STARTING /*comecar o jogo*/, GAME_OVER /*o jogo acabou lol*/
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    val connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    private var serverSocket: ServerSocket? = null

    /**
     * dados de uma ligacao de um servidor para um cliente
     */
    inner class ServerClientConnection(private var socket: Socket, private var threadComm: Thread) {
        private val socketO: OutputStream?
            get() = socket.getOutputStream()
        private val socketI: InputStream?
            get() = socket.getInputStream()
    }

    val serverClientConnections = mutableListOf<ServerClientConnection>()

    val state = MutableLiveData(State.STARTING)

    //cria o server
    fun startServer() {
        if (serverSocket != null ||
            serverClientConnections.isNotEmpty() ||
            connectionState.value != ConnectionState.SETTING_PARAMETERS
    ***REMOVED***
            return
        connectionState.postValue(ConnectionState.SERVER_CONNECTING)
        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.apply {
                try {
                    while (state.value != State.GAME_OVER) {
                        addClient(serverSocket!!.accept())
                    }
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

//  como nao é aqui que se define o fim do jogo
//    fun stopGame() {
//        try {
//            state.postValue(State.GAME_OVER)
//            connectionState.postValue(ConnectionState.CONNECTION_ERROR)
//            socket?.close()
//            socket = null
//            for (i in threadComms) {
//                i.interrupt();
//            }
//            threadComms.clear()
//        } catch (_: Exception) {
//        }
//    }
//

//    coisas do client nao pertencem aqui TODO: remover daqui, e mover para o sitio certo
//    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {
//        if (socket != null || connectionState.value != ConnectionState.SETTING_PARAMETERS)
//            return
//        thread {
//            connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
//            try {
//                val newsocket = Socket(serverIP, serverPort)
//                startComm(newsocket)
//            } catch (_: Exception) {
//                connectionState.postValue(ConnectionState.CONNECTION_ERROR)
//            }
//        }
//    }


//    /**
//     * Começa uma conecao com client/server
//     */
//    private fun startComm(newSocket: Socket) {
//        //condicao para nao fazer conecao
//        //    return
//
//        var tempocket = newSocket
//
//        threadComms.add(thread {
//            try {
//                if (new fe == null)
//                    return@thread
//
//                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
//                val bufI = socketI!!.bufferedReader()
//                while (state.value != State.GAME_OVER) {
//                    val message = bufI.readLine()
//                    val move = message.toIntOrNull() ?: MOVE_NONE
//                    fazAcao(move)
//                }
//
//            } catch (_: Exception) {
//            } finally {
//                //acaba o jogo do coiso
//                stopGame()
//            }
//        })
//    }

    /**
     * Aceita connecao de um novo cliente
     */
    fun addClient(newSocket : Socket) {

        serverClientConnections.add(ServerClientConnection(newSocket, thread{
            try {
                if (newSocket.getInputStream() == null)
                    return@thread

                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                val bufI = newSocket.getInputStream()!!.bufferedReader()
//                TODO: recebe informações do client
//                while (state.value != State.GAME_OVER) {
//                    val message = bufI.readLine()
//                    val move = message.toIntOrNull() ?: MOVE_NONE
//                    fazAcao(move)
//                }

        }catch (_:Exception){}
        }))

    }

    fun fazAcao(movimentosensual: Int) {
        //TODO
    }
}
