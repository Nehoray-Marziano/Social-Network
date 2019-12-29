package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class PMMessage extends Message {
    public String userName="";
    public String content="";
    private Vector<Byte> message;
    private int location=0;
    public boolean notify=false;
    public boolean isOK=true;
    public String Sender;

    public PMMessage(PMMessage other){
        this.userName=other.userName;
        this.content=other.content;
        this.message=other.message;
        this.location=other.location;
        this.notify=other.notify;
        this.isOK=other.isOK;
        this.Sender=other.Sender;
    }

    public PMMessage(Vector<Byte> message){
        this.message=message;
        createUserName();
        createContent();
    }
    public void createUserName() {
        byte[] NameArr=new byte[message.size()];
        int i=0;
        while(message.get(i)!=0){
            NameArr[i]=message.get(i);
            i++;
            location=i;
        }
        NameArr[0]=message.get(0);
        String currByte=new String(NameArr,0,location,StandardCharsets.UTF_8);
        userName=currByte;
       /* for (int i = 0; i < message.size(); i++) {
            byte[] tmp = new byte[message.get(i)];
            String currByte = new String(tmp, 1, 2, StandardCharsets.UTF_8);
            if (!currByte.equals("\0"))
                userName += currByte;
            else {
                location = i + 1;
                return;
            }
        }*/
    }

    public void createContent() {
        byte[] tmp = new byte[message.size()];        //-1 because we dont need the name
        int j=0;
        for (int i = location+1; i < message.size(); i++) {
            tmp[j] = message.get(i);
            j++;
        }
        String currByte = new String(tmp, 0, j, StandardCharsets.UTF_8);
        content = currByte;

    }
    public String toString() {
        return "PM "+userName+" "+content;
    }

    public void setSender(String name){
        this.Sender=name;
    }
}
