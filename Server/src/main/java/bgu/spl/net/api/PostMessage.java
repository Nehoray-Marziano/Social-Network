package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Vector;

public class PostMessage extends Message {
    public String content="";
    public boolean notify=false;
    public boolean isOK=true;
    public LinkedHashSet<String> HashTags=new LinkedHashSet<>();
    public String sender;

    public PostMessage(PostMessage other){
        this.content=other.content;
        this.notify=other.notify;
        this.isOK=other.isOK;
        this.HashTags=other.HashTags;
        this.sender=other.sender;
    }
    public PostMessage(Vector<Byte> message){
        byte[] tmp=new byte[message.size()];
        for(int i=0;i<message.size();i++){
            tmp[i]=message.get(i);
        }
        String tmpS=new String(tmp,0,tmp.length,StandardCharsets.UTF_8);
        content=tmpS;


        this.setHashTags();
    }
    public void setHashTags(){
        String userName="";
        for(int i=0;i<content.length();i++){
            if(content.charAt(i)=='@'){
                for(int j=i+1;j<content.length();j++){
                    if (content.charAt(j)!=' ')
                        userName+=content.charAt(j);
                    else {
                        HashTags.add(userName);
                        userName="";
                        break;
                    }
                }
            }
            HashTags.add(userName);
        }
    }
    public String toString() {
        return ("POST " + content);
    }

    public void setSender(String name){
        this.sender=name;
    }

}
