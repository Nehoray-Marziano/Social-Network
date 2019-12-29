package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class StatMessage extends Message {
    public String UserName="";
    public boolean isOK=true;
    public int numOfFollowers=0;
    public int numOfFollowing=0;
    public int numOfPosts=0;



    public StatMessage(Vector<Byte> message){
        byte[] tmp=new byte[message.size()];
        for(int i=0;i<message.size();i++){
            tmp[i]=message.get(i);
        }
        UserName=new String(tmp,0,message.size(), StandardCharsets.UTF_8);
    }
    public String toString() {
        return "STAT " + UserName;
    }
}
