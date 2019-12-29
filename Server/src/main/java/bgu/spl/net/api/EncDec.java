package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class EncDec<T> implements MessageEncoderDecoder {
    private byte[] opcode;
    private int counter;
    private int zeros = 0;
    public int followcnt=0;
    private Vector<Byte> message=new Vector<>();
    public LinkedBlockingQueue<Message> msgQueue=new LinkedBlockingQueue<>();       //todo: NEED TO IMPLEMENT WITHOUT THIS FIELD
    public String currUser="";
    public int secZero=0;


    public EncDec() {
        this.opcode = new byte[2];
        this.counter = 0;
    }

    @Override
    public T decodeNextByte(byte nextByte) {
        if (counter < 2) {
            opcode[counter] = nextByte;
            counter++;
            if(counter==2){         //these are 2 special commands which does not have anything else than opcode so we dont want them to 'waste' next part of the opcode for the next command
                short op3AND7=bytesToShort(opcode);
                if(op3AND7==3){                 //LOGOUT
                    LogoutMessage lgOut=new LogoutMessage();
                    msgQueue.add(lgOut);
                    message.clear();
                    zeros=0;
                    counter=0;
                    return (T)lgOut;
                }
                if(op3AND7==7) {                //USERLIST
                    UserListMessage list=new UserListMessage();
                    msgQueue.add(list);
                    message.clear();
                    zeros=0;
                    counter=0;
                    return (T)list;
                }
            }
            return null;
        }
        if (nextByte=='\0')
            zeros++;

        short op=bytesToShort(opcode);
        if (op==1) {             //REGISTER
            if (zeros < 2)
                message.add(nextByte);
            if (zeros == 2) {
                RegisterMessage rg=new RegisterMessage(message);
                msgQueue.add(rg);
                message.clear();
                zeros=0;
                counter=0;
                return (T)rg;
            }
            return null;

        }
        else if (op==2) {             //LOGIN
                if (zeros < 2)
                    message.add(nextByte);
                if (zeros == 2) {
                    LoginMessage tmp=new LoginMessage(message);
                    msgQueue.add(tmp);
                    message.clear();
                    zeros=0;
                    counter=0;
                    return (T)tmp;
                }
                return null;
        }

        else if(op==4){                 //FOLLOW
            if(followcnt<3){        //reading the first 3 bytes after the opCode
                message.add(nextByte);
                followcnt++;
                return null;
            }
            byte[] arr=new byte[2];
            arr[0]=message.get(1);
            arr[1]=message.get(2);
            short numOfUser=bytesToShort(arr);
            if(secZero<numOfUser) {
                if(nextByte=='\0')
                    secZero++;
                message.add(nextByte);
            }
            if(secZero==numOfUser) {
                FollowMessage fll=new FollowMessage(message);
                msgQueue.add(fll);
                message.clear();
                zeros=0;
                counter=0;
                secZero=0;
                followcnt=0;

                return (T)fll;
            }
            return null;
        }
        else if(op==5){                 //POST
            if(nextByte!=0)
                message.add(nextByte);
            else {
                PostMessage post=new PostMessage(message);
                msgQueue.add(post);
                message.clear();
                zeros = 0;
                counter = 0;
                return (T)post;
            }
            return null;
        }
        else if(op==6){                 //PM  todo:ASAP it's not working!!!  need to read correctly the name and the content
            if(zeros<=2 & nextByte!=0)
                message.add(nextByte);
            if(zeros==1){
                byte del=0;     //made to split between the name and the content
                message.add(del);
                zeros++;        //i increased zeros so we wont add twice or more the delimiter char
            }

            if(zeros>=3 && nextByte==0){

                PMMessage pm=new PMMessage(message);
                msgQueue.add(pm);
                message.clear();
                zeros=0;
                counter=0;
                return (T)pm;
            }
            return null;
        }

        else if(op==8){                 //STAT
            if(zeros<1)
                message.add(nextByte);
            else {
                StatMessage stat=new StatMessage(message);
                msgQueue.add(stat);
                message.clear();
                zeros=0;
                counter=0;
                return (T)stat;
            }

            return null;

        }
        counter=0;
        zeros=0;
        followcnt=0;
        currUser="";
        return null;
    }
        @Override
 //   ********************************************************ENCODE**********************************************************
        public byte[] encode (Object message){
        Vector<Byte> output=new Vector<>();
        //addBackSlash(output);
        String opString="";

        //*************************** NOTIFICATION CASE******************************
            if(message instanceof PMMessage&& ((PMMessage) message).notify){//if we do need to notify
                Vector<Byte> tmp= NotificationShortcut("0",((PMMessage) message).Sender);
                add2Vec(tmp,output);

                byte[] content=((PMMessage) message).content.getBytes(StandardCharsets.UTF_8);
                for(int i=0;i<content.length;i++)
                    output.add(content[i]);
                addBackSlash(output);
                byte[] arr2add=vec2arr(output);
                return arr2add;
        }
            if(message instanceof PostMessage&&((PostMessage) message).notify){//if we do need to notify
                Vector<Byte> tmp= NotificationShortcut("1",((PostMessage) message).sender);
                add2Vec(tmp,output);
                byte[] content=((PostMessage) message).content.getBytes(StandardCharsets.UTF_8);
                for(int i=0;i<content.length;i++)
                    output.add(content[i]);
                addBackSlash(output);
                byte[] arr2add=vec2arr(output);
                return arr2add;
            }
           // ***********************************************************************

            //**************************************ACK CASE********************************************
            if(message instanceof RegisterMessage&&((RegisterMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=1;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof LoginMessage&&((LoginMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=2;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                this.currUser=((LoginMessage) message).userName;            // only if the logging in succeded
                return vec2arr(output);
            }
            if(message instanceof LogoutMessage&&((LogoutMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=3;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof FollowMessage&&((FollowMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=4;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                short numOfFoll=(short)((FollowMessage) message).successfullFollow;
                byte[] arr =shortToBytes(numOfFoll);
                AddFromArr2Vec(arr,output);
                for(String name:((FollowMessage) message).succecfullFollowUserNames){       //adding all the successfully followed
                    byte[] stringArr=name.getBytes();
                    AddFromArr2Vec(stringArr,output);
                    byte[] zero={0};
                    AddFromArr2Vec(zero,output);
                }

                return vec2arr(output);
            }
            if(message instanceof PostMessage&&((PostMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=5;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof PMMessage&&((PMMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=6;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                byte[] arr=((PMMessage) message).content.getBytes();
                for(int i=0;i<arr.length;i++)
                    output.add(arr[i]);
                addBackSlash(output);
                return vec2arr(output);
            }
            if(message instanceof UserListMessage&&((UserListMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=7;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                short size= (short)((UserListMessage) message).userNamesList.size();
                byte[] tempArr=shortToBytes(size);
                AddFromArr2Vec(tempArr,output);     //number of registered users
                byte[] deli={'\0'};
                for(String name:((UserListMessage) message).userNamesList){     //adding the users Names List
                   AddFromArr2Vec(name.getBytes(),output);
                   AddFromArr2Vec(deli,output);
                }
                return vec2arr(output);
            }
            if(message instanceof StatMessage&&((StatMessage) message).isOK){// if there is no error(protocol's decision)
                short x=10; short y=8;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                short numOfPosts=(short)((StatMessage) message).numOfPosts;
                byte[] tempArr1=shortToBytes(numOfPosts);
                AddFromArr2Vec(tempArr1,output);
                short numOfFollowers=(short)((StatMessage) message).numOfFollowers;
                byte[] tempArr2=shortToBytes(numOfFollowers);
                AddFromArr2Vec(tempArr2,output);
                short numOfFollowing=(short)((StatMessage) message).numOfFollowing;
                byte[] tempArr3=shortToBytes(numOfFollowing);
                AddFromArr2Vec(tempArr3,output);
                return vec2arr(output);
            }
            //*********************************************************

            //********************************ERROR CASE*********************************
            if(message instanceof RegisterMessage&&!((RegisterMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=1;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof LoginMessage&&!((LoginMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=2;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof LogoutMessage&&!((LogoutMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=3;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof FollowMessage&&!((FollowMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=4;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof PostMessage&&!((PostMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=5;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof PMMessage&&!((PMMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=6;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof UserListMessage&&!((UserListMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=7;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            if(message instanceof StatMessage&&!((StatMessage) message).isOK){// if there is an error(protocol's decision)
                short x=11; short y=8;
                Vector<Byte> tmp=firstShortCut(x,y);
                add2Vec(tmp,output);
                return vec2arr(output);
            }
            //***********************************************************
            return null;

        }

        public void addBackSlash(Vector<Byte> output){
            output.add((byte)(0));
        }

        public byte[] vec2arr(Vector<Byte> output){
        byte[] total=new byte[output.size()];
        for(int i=0;i<output.size();i++)
            total[i]=output.get(i);
        return total;
        }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    public Vector<Byte> firstShortCut(short first,short sec){
        Vector<Byte> output=new Vector<>();
        short s=first;
        byte[] tmp=shortToBytes(s);
        output.add(tmp[0]);
        output.add(tmp[1]);
        short s1=sec;
        byte[] tmp1=shortToBytes(s1);
        output.add(tmp1[0]);
        output.add(tmp1[1]);
        return output;
    }
    public void add2Vec(Vector<Byte> source,Vector<Byte> dest){
        for(int i=0;i<source.size();i++)
            dest.add(source.get(i));
    }
    public Vector<Byte> NotificationShortcut(String x,String name){
        Vector<Byte> output=new Vector<>();
        short s=9;              // notification opcode
        byte[] tmp=shortToBytes(s);
        output.add(tmp[0]);
        output.add(tmp[1]);
        tmp=x.getBytes(StandardCharsets.UTF_8);
        output.add(tmp[0]);
        byte[] senderName=name.getBytes(StandardCharsets.UTF_8);
        for(int i=0;i<senderName.length;i++)
            output.add(senderName[i]);
        addBackSlash(output);
        return output;
    }

    public void AddFromArr2Vec(byte[] arr,Vector<Byte> vec){
        for(int i=0;i<arr.length;i++)
            vec.add(arr[i]);
    }

}

