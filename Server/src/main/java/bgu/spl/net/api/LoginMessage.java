package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class LoginMessage extends Message {
    public String userName = "";
    public String password = "";
    private Vector<Byte> message = new Vector<>();
    private int location = 0;
    public boolean isOK=true;


    public LoginMessage(Vector<Byte> message) {
        this.message = message;
        createUserName();
        createPassword();

    }

    public void createUserName() {
        byte[] tmp = new byte[1];
        for (int i = 0; i < message.size(); i++) {
            tmp[0]=message.get(i);
            String currByte = new String(tmp, 0, 1, StandardCharsets.UTF_8);
            if (!currByte.equals("\0"))
                userName += currByte;
            else {
                location = i + 1;
                break;
            }
        }
    }

    public void createPassword() {
        byte[] tmp = new byte[1];
        for (int i = location; i < message.size(); i++) {
            tmp[0]=message.get(i);
            String currByte = new String(tmp, 0, 1, StandardCharsets.UTF_8);
            password += currByte;

        }
    }
    public String toString() {
        return "LOGIN " + userName + " " + password;
    }
}







