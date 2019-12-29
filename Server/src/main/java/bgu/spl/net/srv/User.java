package bgu.spl.net.srv;

import bgu.spl.net.api.PMMessage;
import bgu.spl.net.api.PostMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
    public String userName;
    public String password;
    public List<User> followers=new LinkedList<>();
    public List<User> following =new LinkedList<>();
    public LinkedBlockingQueue<PMMessage>pm2notify=new LinkedBlockingQueue<>();
    public LinkedBlockingQueue<PostMessage>post2notify=new LinkedBlockingQueue<>();
    public int postCounter=0;
    public Integer Id;
    public int connectionId=0;
    private boolean isOnline;

    public User(String name,String password){
        this.userName=name;
        this.password=password;
    }

    public boolean isFollowing(User other){
        return following.contains(other);
    }

    public boolean isFollower(User other){
        return followers.contains(other);
    }

    public void setID(int id){
        this.Id=id;
    }

    public void login(){
        this.isOnline=true;
    }

    public void logout(){
        this.isOnline=false;
    }

    public boolean isOnline(){
        return this.isOnline;
    }


}
