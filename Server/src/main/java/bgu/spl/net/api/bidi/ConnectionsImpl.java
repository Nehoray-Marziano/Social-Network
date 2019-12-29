package bgu.spl.net.api.bidi;

import bgu.spl.net.api.LogoutMessage;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.User;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    public ConcurrentHashMap<Integer, ConnectionHandler> handlers= new ConcurrentHashMap<>();      //integer=the hashcode of a spesific user
    public int UniqueId=0;
    public Object SenderLock=new Object();


    @Override
    public boolean send(int connectionId, T msg) {
        if (!handlers.containsKey(connectionId)) {
            throw new IllegalArgumentException("connection id not exist");
        }
        handlers.get(connectionId).send(msg);
        if(msg instanceof LogoutMessage && ((LogoutMessage) msg).isOK)
            disconnect(connectionId);       //we are doing it here in order to be able to send the user the ack message
        return true;
    }

    @Override
    public void broadcast(Object msg) {
        if(msg!=null)                       //make sure that the message isn't null
            for(Integer i:handlers.keySet())
                handlers.get(i).send(msg);
    }

    @Override
    public void disconnect(int connectionId) {           //before we will call this method we will ensure that the user is already registered and his id is valid!  if it's not possible we will send  an error and we won't call this method
        handlers.remove(connectionId);
    }

    public int addConnection(ConnectionHandler handler){
        handlers.putIfAbsent(UniqueId,handler);
        UniqueId++;
        return UniqueId-1;
    }
}
