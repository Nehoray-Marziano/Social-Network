package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Arrays;

public class FollowMessage extends Message {
    private int numOfUsers=0;
    public Vector<String> toFollowList=new Vector<>();
    private Vector<Byte> message;
    private int zeros=0;
    public boolean isOK=true;
    public boolean follow=false;
    public int successfullFollow=0;
    public LinkedList<String> succecfullFollowUserNames=new LinkedList<>();

    public FollowMessage(Vector<Byte> message){
        this.message=message;
        createOperation();
        createNumOfUsers();
        createList();
    }

    public void createOperation(){
        if(message.get(0)==0)
            follow=true;
    }

    public void createNumOfUsers(){
        byte[] arr=new byte[2];
        arr[0]=message.get(1);
        arr[1]=message.get(2);
        short op = bytesToShort(arr);
        this.numOfUsers=op;
    }
    public void createList(){
        byte[] tmpMsg=new byte[message.size()-3];
        for(int i=3;i<message.size();i++){
            tmpMsg[i-3]=message.get(i);
        }
        String users=new String(tmpMsg, 0, tmpMsg.length, StandardCharsets.UTF_8);
        String following="";
        for(int i=0;i<users.length();i++) {
            if (users.charAt(i) != '\0')         //todo:what did we mean?  is it \0 or 0???
                following+=users.charAt(i);
            else{
                toFollowList.add(following);
                following="";
            }
        }
    }
    public short bytesToShort(byte[] byteArr){
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public String toString() {
        String[] print=new String[toFollowList.size()];
        for(int i=0;i<toFollowList.size();i++)
            print[i]=toFollowList.get(i);
        return "FOLLOW " + message.toString() + " " + numOfUsers + " " + Arrays.toString(print);
    }
}