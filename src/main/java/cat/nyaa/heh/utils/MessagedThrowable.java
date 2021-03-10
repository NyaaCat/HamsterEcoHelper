package cat.nyaa.heh.utils;

import cat.nyaa.nyaacore.Message;

public class MessagedThrowable extends RuntimeException{
    protected Message message;

    public MessagedThrowable(Message message){
        this.message = message;
    }

    public Message getCustomMessage(){
        return message;
    }
}
