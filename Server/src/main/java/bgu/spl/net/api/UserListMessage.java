package bgu.spl.net.api;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class UserListMessage extends Message {
    public boolean isOK=true;
    public Vector<String> userNamesList=new Vector<>();

}
