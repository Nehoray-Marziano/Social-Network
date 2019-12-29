package bgu.spl.net.srv;

import bgu.spl.net.api.PMMessage;
import bgu.spl.net.api.PostMessage;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

// this class will store all the relevant information (HashMaps etc...) of each server
public class DataBase {
    public ConcurrentHashMap<String,User> allUsers=new ConcurrentHashMap<>();// holds all the users on the server
    public ConcurrentHashMap<Integer, User> LoggedIn =new ConcurrentHashMap<>();// holds all the active users
    public ConcurrentHashMap<User, LinkedList<PostMessage>> posts=new ConcurrentHashMap<>();// holds all the user's posts
    public ConcurrentHashMap<User, LinkedList<PMMessage>> pms=new ConcurrentHashMap<>();//holds all the the pms that the user send
    public ConcurrentHashMap<Integer,User> clientsUsers =new ConcurrentHashMap<>();//in case 1 client has logged in to more than 1 user


    public DataBase(){}

    public void addUser(User user){
        this.allUsers.putIfAbsent(user.userName,user);
    }
}
