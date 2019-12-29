package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.api.EncDec;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImp;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.DataBase;
import bgu.spl.net.srv.Server;

public class ThreadPerClientMain {
    public static void main(String[] args){
        DataBase dataBase=new DataBase();
        Server.threadPerClient(
                7777, //port
                () ->  new BidiMessagingProtocolImp(dataBase), //protocol factory
                EncDec::new //message encoder decoder factory
        ).serve();
    }
}
