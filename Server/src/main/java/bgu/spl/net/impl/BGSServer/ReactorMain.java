package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.EncDec;
import bgu.spl.net.api.Message;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImp;
import bgu.spl.net.srv.DataBase;
import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main(String[] args){
        DataBase dataBase=new DataBase();
        Server.reactor(Runtime.getRuntime().availableProcessors(),7777,()->new BidiMessagingProtocolImp(dataBase), ()->new EncDec()).serve();
    }
}
