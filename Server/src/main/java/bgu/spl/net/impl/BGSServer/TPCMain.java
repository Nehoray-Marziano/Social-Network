package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.EncDec;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImp;
import bgu.spl.net.srv.DataBase;
import bgu.spl.net.srv.Server;


public class TPCMain {
    public static void main(String[] args){
        DataBase dataBase=new DataBase();
        Server.threadPerClient(
                7777, //port
                ()->new BidiMessagingProtocolImp(dataBase), //protocol factory
                ()->new EncDec() //message encoder decoder factory
        ).serve();
    }
}
