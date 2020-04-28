package com.example.telegraph;

import com.google.gson.Gson;

public class Protocol {
//Типы взаимодействия
    public static final byte USER_STATUS=1; // 1{connected:"true", user:{name:"", id: ""}}
    public static final byte MESSAGE=2; // 2{encodedText: "" , sender : ""}
    public static final byte USER_NAME=3; // 3{name: ""}


//Класс для сообщений
    static class Message {
        public final static byte GROUP_CHAT = 1;

        private long receiver = GROUP_CHAT;

        //как в json
        private long sender;
        private String encodedText;

        //Конструкторы
        public Message() {
            this("Пользователь хотел вам что то написать");
        }

        public Message(String encodedText) {
            this.encodedText = encodedText;
        }

        //Геттеры сеттеры
        public String getEncodedText() {
            return encodedText;
        }

        public void setEncodedText(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getReceiver() {
            return receiver;
        }

        public void setReceiver(long receiver) {
            this.receiver = receiver;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

    }

    //Для статуса
    static class UserStatus {
        private User user;
        private boolean connected;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }
    }
    static class User {
        private long id;
        private String name;

        public User() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    //Для имени текущего пользователя
    static class UserName{
        private String name;

        public UserName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    //Протокол обработки

    //Получаем тип операции
    public static int getType (String json) {
        if (json==null || json.length()==0 ) {
            return -1;
        }
        return Integer.valueOf(json.substring(0,1));
    }

    public static String packAny(byte type,String json){
        return type+json;
    }
    public static String packMessage(Message msg){
        return packAny(MESSAGE,(new Gson()).toJson(msg));
    }
    public static String packName(UserName uname){
        return packAny(USER_NAME,(new Gson()).toJson(uname));
    }
    //packStatus не нужен - его мы только получаем

    public static Message unpackMessage(String json){
        return (new Gson()).fromJson(json.substring(1),Message.class);
    }

    public static UserStatus unpackStatus(String json){
        return (new Gson()).fromJson(json.substring(1),UserStatus.class);
    }
    //unpack Name не нужен

}
