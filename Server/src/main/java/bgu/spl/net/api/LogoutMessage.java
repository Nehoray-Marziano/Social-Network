package bgu.spl.net.api;

public class LogoutMessage extends Message{
    public boolean isOK=true;
    public String toString() {
        return "LOGOUT";
    }
    public LogoutMessage(LogoutMessage other){
        this.isOK=other.isOK;
    }
    public LogoutMessage(){
        this.isOK=true;
    }
}
