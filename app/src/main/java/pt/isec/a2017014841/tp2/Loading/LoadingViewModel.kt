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
    var serverThread : Thread? = null
    //cria o server
    fun startServer() {
        if (serverSocket != null ||
            serverClientConnections.isNotEmpty() ||
            connectionState.value != ConnectionState.SETTING_PARAMETERS
        )
        connectionState.postValue(ConnectionState.SERVER_CONNECTING)
        serverThread = thread {
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
        var copyserverclients = serverClientConnections.toMutableList()
        while(copyserverclients.isNotEmpty()){
            var it = copyserverclients[0]
            try {
                val sI = it.socket.getInputStream()
                val sO = it.socket.getOutputStream()
                sO.run {
                    write((userN.toString()+"\n").toByteArray())
                    write((Dados.nomeDaEquipa+"\n").toByteArray())
                }
                lateinit var valoresByteArray: ByteArray
                sI.run {
                    valoresByteArray = this.readBytes()
                }
                //se nao ouve exceptions incrementar o user
                listofusers[userN++.toString()] = String(valoresByteArray)
                copyserverclients.removeAt(0)
            } catch (e: Exception) {
                e.printStackTrace()
                serverClientConnections.remove(it)
                copyserverclients.removeAt(0)
            }
        }
        return listofusers
    }

    /**
     * Começa o client
     */
    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {
        if (socket != null || connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return
        thread {
            connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {
                socket = Socket(serverIP, serverPort)
                //TODO: enviar as coordenadas ao server e verificar se estao a menos de 100m
//                startComm(newsocket)
                thread {
                    try {
                        if (socketI == null)
                            return@thread
                        connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                        val bufI = socketI!!.bufferedReader()
                        while (state.value != State.GAME_OVER) {
                            Dados.userNumber = bufI.readLine().toInt()
                            if (userNumber > 0) {
                                nomeDaEquipa = bufI.readLine()
                                socketO!!.write(Dados.locationToString(actualLocation).toByteArray())
                            }
                        }
                    } catch (_: Exception) {
                        errorText.postValue(thisContext.getString(R.string.LostConnectioWithServer))
                    }
                }
            } catch (e: Exception) {
                Log.e("começar cliente", e.stackTrace.toString());
                connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                //TODO: dar trigger a algo para que na view dé um aviso de erro
                errorText.postValue(thisContext.getString(R.string.CantConnectToServer))
            }
        }
    }
    var clientError = MutableLiveData(false)
    var clientSucess = MutableLiveData(false)
    //mensagem de erro vindo da view model
    var errorText = MutableLiveData<String>("")

    /**
     * Aceita connecao de um novo cliente no servidor
     */
    fun addClient(newSocket: Socket) {
        val newThread = thread {
            try {
                if (newSocket.getInputStream() == null)
                    throw Exception()
                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
//                val bufI = newSocket.getInputStream()!!.bufferedReader()
//                TODO: recebe informações do client
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ClientThread", "a apagar elemento da lista")
                serverClientConnections.forEach {
                    if (it.socket == socket) {
                        serverClientConnections.remove(it)
                    }
                }
                errorText.postValue(thisContext.getString(R.string.LostConnectionServerWithClient))
            }
        }
        serverClientConnections.add(ServerClientConnection(newSocket, newThread))
        nClients.postValue(serverClientConnections.size)
        Log.i("conecao do cliente", "cleintes conectados: " + serverClientConnections.size)
    }
}
