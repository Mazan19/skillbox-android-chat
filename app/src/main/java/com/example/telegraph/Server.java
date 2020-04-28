package com.example.telegraph;


import android.util.Log;

import androidx.core.util.Consumer;
import androidx.core.util.Pair;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private WebSocketClient wsClient; //передастер с сервером
    private Map<Long,String> names = new ConcurrentHashMap<>(); //записываем все имена и id сюда (пока работает приложение)

    //реакция на происходящее
    private Consumer<Pair<String,String>> onMessageReceived;
    //private Consumer<Pair<String,String>> onPrivateMsgRecieved;
    private Consumer<Pair<String,Integer>> onStatusesUpd;

    public Server(Consumer<Pair<String, String>> onMessageReceived, Consumer<Pair<String,Integer>> onStatusesUpd ) {
        this.onMessageReceived = onMessageReceived;
        this.onStatusesUpd = onStatusesUpd;
    }

    public void connect(){
        URI uri;
        try {
            uri=new URI("ws://35.214.1.221:8881");
        }catch (URISyntaxException e){
            e.printStackTrace();
            return;
        }

        wsClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SOCKET_MSG","YA RODILSA");
            }

            @Override
            public void onMessage(String socketMsg) {
                Log.i("SOCKET_MSG","Get message " +socketMsg);
                int type = Protocol.getType(socketMsg);
                switch (type) {
                    case Protocol.MESSAGE: processIncomingMsg(Protocol.unpackMessage(socketMsg)); break;
                    case Protocol.USER_STATUS: processStatus(Protocol.unpackStatus(socketMsg)); break;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SOCKET_MSG","THAT IS ALL");
            }

            @Override
            public void onError(Exception ex) {
                Log.e("SOCKET_MSG","ERROR: "+ex.getMessage(),ex);
            }
        };
        wsClient.connect();
        Log.i("SOCKET_MSG","HERE");
    }

    public void disconnect(){
        wsClient.close();
    }


    public void sendName(String name) {
        Protocol.UserName usrName = new Protocol.UserName(name);
        if (wsClient !=null && wsClient.isOpen()) {
            wsClient.send(Protocol.packName(usrName));
        }
    }
    public void sendMsg(String strMsg) {
        Protocol.Message msg = new Protocol.Message(strMsg);
        if (wsClient !=null && wsClient.isOpen()) {
            wsClient.send(Protocol.packMessage(msg));
        }
    }

    private void processStatus(Protocol.UserStatus status){
        Protocol.User usr = status.getUser();
        if (status.isConnected()) {
            names.put(usr.getId(),usr.getName());
            onStatusesUpd.accept(new Pair<>(usr.getName(),names.size()));
        } else {
            names.remove(usr.getId());
            onStatusesUpd.accept(new Pair<>("",names.size()));
        }
    }

    private void processIncomingMsg(Protocol.Message message){
        String name = names.get(message.getSender());
        if (name == null) {
            name = "Hide User";
        }
        String text = null;
        text=message.getEncodedText();
        onMessageReceived.accept(new Pair<>(name,text));
    }
}
