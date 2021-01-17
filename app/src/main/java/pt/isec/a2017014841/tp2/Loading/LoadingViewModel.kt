package pt.isec.a2017014841.tp2.Loading

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.isec.a2017014841.tp2.Dados
import pt.isec.a2017014841.tp2.Dados.actualLocation
import pt.isec.a2017014841.tp2.Dados.nomeDaEquipa
import pt.isec.a2017014841.tp2.Dados.userNumber
import pt.isec.a2017014841.tp2.R
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class LoadingViewModel : ViewModel() {
    lateinit var thisContext: Context
    fun setContext(context: Context) {
        thisContext = context
    }

    val SERVER_PORT = 9999
    val MOVE_NONE = 0


    /**
     * dados de uma ligacao de um servidor para um cliente
     */
    private var socket: Socket? = null
    private var threadComm: Thread? = null
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()
    private val socketI: InputStream?
        get() = socket?.getInputStream()

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
    data class ServerClientConnection(val socket: Socket, val threadComm: Thread);

    val serverClientConnections = mutableListOf<ServerClientConnection>()

    val state = MutableLiveData(State.STARTING)
    val nClients = MutableLiveData(0)

    //cria o server
    fun startServer() {
        if (serverSocket != null ||
            serverClientConnections.isNotEmpty() ||
            connectionState.value != ConnectionState.SETTING_PARAMETERS
    ***REMOVED***
            return
        connectionState.postValue(ConnectionState.SERVER_CONNECTING)
        thread {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
            } catch (_: Exception) {
            }
            serverSocket?.apply {
                try {
                    while (state.value != State.GAME_OVER) {
                        addClient(serverSocket!!.accept())
                    }
                } catch (e: Exception) {
                    Log.e("começar server", e.stackTrace.toString());
                    e.printStackTrace()
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

    fun getListOfUsers(): HashMap<String, String> {
        val listofusers = HashMap<String, String>()
        var userN = 2
        serverClientConnections.forEach {
            try {
                val sI = it.socket.getInputStream()
                val sO = it.socket.getOutputStream()

                sO.run {
                    write(userN.toString().to)
                    write(Dados.nomeDaEquipa.toByteArray())
                }
                lateinit var valoresByteArray: ByteArray
                sI.run {
                    valoresByteArray = this.readBytes()
                }
                listofusers[userN++.toString()] = String(valoresByteArray)
            } catch (e: Exception) {
                e.printStackTrace()
                serverClientConnections.remove(it)
            }
        }
        return listofusers
    }

    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {
        if (socket != null || connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return
        thread {
            connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {
                val newsocket = Socket(serverIP, serverPort)
                //TODO: enviar as coordenadas ao server e verificar se estao a menos de 100m
                startComm(newsocket)
            } catch (e: Exception) {
                Log.e("começar cliente", e.stackTrace.toString());
                connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                //TODO: dar trigger a algo para que na view dé um aviso de erro
                errorText.postValue(thisContext.getString(R.string.CantConnectToServer))
            }
        }
    }

    /**
     * Começa uma conecao com client/server
     */
    private fun startComm(newSocket: Socket) {
        //condicao para nao fazer conecao
        //    return

        socket = newSocket
        serverClientConnections.add(ServerClientConnection(newSocket, thread {
            try {
                if (socketI == null)
                    return@thread
                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                val bufI = socketI!!.bufferedReader()
                while (state.value != State.GAME_OVER) {
                    Dados.userNumber = bufI.readLine().toInt()
                    if (userNumber > 0) {
                        nomeDaEquipa = bufI.readLine()
                        if (move > 0) {
                            userNumber = move
                            socketO!!.write(Dados.locationToString(actualLocation).toByteArray())
                        }
                    }

                }
            } catch (_: Exception) {
                errorText.postValue(thisContext.getString(R.string.LostConnectioWithServer))
            }
        }))
    }

    //mensagem de erro vindo da view model
    var errorText = MutableLiveData<String>("")

    /**
     * Aceita connecao de um novo cliente
     */
    fun addClient(newSocket: Socket) {
        val newThread = thread {
            try {
                if (newSocket.getInputStream() == null)
                    return@thread
                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                val bufI = newSocket.getInputStream()!!.bufferedReader()
//                TODO: recebe informações do client
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ClientThread", "a apagar elemento da lista")
                var nclient = 0
                serverClientConnections.forEach {
                    if (it.socket == socket) {
                        serverClientConnections.remove(it)
                    }
                    nclient++
                }

                errorText.postValue(thisContext.getString(R.string.LostConnectionServerWithClient))
            }
        }
        serverClientConnections.add(ServerClientConnection(newSocket, newThread))
        nClients.postValue(serverClientConnections.size)
        Log.i("conecao do cliente", "cleintes conectados: " + serverClientConnections.size)
    }


}
