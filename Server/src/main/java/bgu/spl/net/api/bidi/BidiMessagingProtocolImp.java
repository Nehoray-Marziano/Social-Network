package bgu.spl.net.api.bidi;

import bgu.spl.net.api.*;
import bgu.spl.net.srv.DataBase;
import bgu.spl.net.srv.User;

import java.util.LinkedList;

public class BidiMessagingProtocolImp implements BidiMessagingProtocol<Message> {
    public int connectionId;
    public ConnectionsImpl connection;
    public boolean shouldTerminate = false;
    public DataBase dataBase;
    public Integer hashcode;
    public User user;
    public User ActiveUser;

    public BidiMessagingProtocolImp(DataBase db) {
        this.dataBase = db;
    }

    public BidiMessagingProtocolImp() {
        this.dataBase = new DataBase();
    }

    @Override
    public void start(int connectionId, Connections connections) {
        this.connectionId = connectionId;
        this.connection = ((ConnectionsImpl) connections);
    }

    @Override
    public void process(Message message) {
        Message msg = (Message) message;
        if (msg instanceof RegisterMessage) {             //REGISTER
            RegisterMessage RM = (RegisterMessage) msg;
            synchronized (dataBase.allUsers) {      //made to sync only the register without other usefull field
                if (dataBase.allUsers.containsKey(RM.userName)) {
                    RM.isOK = false;
                    connection.send(connectionId, RM);
                } else {
                    user = new User(RM.userName, RM.password);
                    hashcode = user.hashCode();
                    user.Id = hashcode;
                    dataBase.addUser(user);
                    connection.send(connectionId, RM);
                }
            }
        }
        if (msg instanceof LoginMessage) {        // LOGIN     todo: what if the user disconnects while notifying
            LoginMessage LM = (LoginMessage) msg;
            synchronized (dataBase.LoggedIn) {      //made to sync only the register without other usefull field
                if (ActiveUser == null) {
                    if (dataBase.allUsers.containsKey(LM.userName) && dataBase.allUsers.get(LM.userName).password.equals(LM.password)) {
                        if (!dataBase.LoggedIn.containsKey(dataBase.allUsers.get(LM.userName).Id)) {// if the user exists and is not logged in
                            while (!dataBase.allUsers.get(LM.userName).pm2notify.isEmpty()) {
                                synchronized (connection.SenderLock) {
                                    connection.send(connectionId, dataBase.allUsers.get(LM.userName).pm2notify.poll());
                                }
                            }
                            while (!dataBase.allUsers.get(LM.userName).post2notify.isEmpty()) {
                                synchronized (connection.SenderLock) {
                                    connection.send(connectionId, dataBase.allUsers.get(LM.userName).post2notify.poll());
                                }
                            }
                            ActiveUser = dataBase.allUsers.get(LM.userName);
                            ActiveUser.connectionId = connectionId;
                            dataBase.LoggedIn.putIfAbsent(ActiveUser.Id, ActiveUser);
                            synchronized (connection.SenderLock) {
                                connection.send(connectionId, LM);
                            }
                        } else {        //if he does not exist or he is already logged in
                            LM.isOK = false;
                            synchronized (connection.SenderLock) {
                                connection.send(connectionId, LM);
                            }
                        }
                    } else {
                        LM.isOK = false;
                        synchronized (connection.SenderLock) {
                            connection.send(connectionId, LM);
                        }
                    }
                } else {
                    LM.isOK = false;
                    synchronized (connection.SenderLock) {
                        connection.send(connectionId, LM);       //if he does not exist or the password is incorrect
                    }
                }
            }
        }

        if (msg instanceof LogoutMessage) {       //  Logout
            LogoutMessage LO = (LogoutMessage) msg;
            if (ActiveUser != null && dataBase.LoggedIn.containsKey(ActiveUser.Id) && (connection.handlers.containsKey(ActiveUser.connectionId))) {
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, LO);
                }
                dataBase.LoggedIn.remove(ActiveUser.Id);
                this.shouldTerminate = true;
            } else {
                LO.isOK = false;
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, LO);
                }
            }
        }
        if (msg instanceof FollowMessage) {       //Follow
            FollowMessage fllMsg = (FollowMessage) msg;
            if (ActiveUser != null && dataBase.LoggedIn.containsKey(ActiveUser.Id)) {       //todo:should we sync ActiveUser for follow+logout case at the same time?
                int counter = 0;
                if (fllMsg.follow == true) {
                    for (String name : fllMsg.toFollowList) {
                        if (dataBase.allUsers.containsKey(name) && !(ActiveUser.following.contains(dataBase.allUsers.get(name)))) {      //the fllUser is registered,fllUser not follow already,if the currUser is logedin
                            ActiveUser.following.add(dataBase.allUsers.get(name));
                            dataBase.allUsers.get(name).followers.add(ActiveUser);        //adding this user to the followed user's following list
                            fllMsg.successfullFollow++;     //increasing the amout of the successfull follow
                            fllMsg.succecfullFollowUserNames.add(name);  //adding the name of the followed to the list
                            counter++;
                        }
                    }
                    if (counter == 0)
                        fllMsg.isOK = false;
                    synchronized (connection.SenderLock) {
                        connection.send(connectionId, fllMsg);
                    }
                } else {                 //UNFOLLOW case
                    for (String name : fllMsg.toFollowList) {
                        if (dataBase.allUsers.containsKey(name) && ActiveUser.following.contains(dataBase.allUsers.get(name)) && dataBase.LoggedIn.containsKey(ActiveUser.Id)) {
                            for (int i = 0; i < ActiveUser.following.size(); i++)
                                if (ActiveUser.following.get(i).userName.equals(name)) {
                                    ActiveUser.following.remove(i);
                                    counter++;
                                    fllMsg.successfullFollow++;     //increasing the amout of the successfull unfollow
                                    fllMsg.succecfullFollowUserNames.add(name);  //adding the name of the unfollowed to the list
                                }
                            for (int i = 0; i < dataBase.allUsers.get(name).followers.size(); i++) {
                                if (ActiveUser.Id == dataBase.allUsers.get(name).followers.get(i).Id)
                                    dataBase.allUsers.get(name).followers.remove(i);
                            }
                        }
                    }

                    if (counter == 0)
                        fllMsg.isOK = false;
                    synchronized (connection.SenderLock) {
                        connection.send(connectionId, fllMsg);
                    }
                }
            } else {
                fllMsg.isOK = false;
                connection.send(connectionId, fllMsg);
            }
        }

        if (msg instanceof PostMessage) {         //Post
            PostMessage PM = (PostMessage) msg;
            PM.setSender(this.ActiveUser.userName);
            if (ActiveUser != null && dataBase.LoggedIn.containsKey(ActiveUser.Id)) {     //if the poster user is  login
                synchronized (dataBase.LoggedIn.get(ActiveUser.Id)) {         //sync the case that i upload a post and logout at the same time
                    if (!dataBase.posts.containsKey(ActiveUser)) {    //if he did not post anything yet
                        synchronized (dataBase.posts) {     //sync the case that 2 first post sent in the same time
                            dataBase.posts.put(ActiveUser, new LinkedList<PostMessage>());
                        }
                    }
                }
                dataBase.posts.get(ActiveUser).add(PM);
                ActiveUser.postCounter++;
                for (User follower : ActiveUser.followers) {//going over his followers in order to send them the post
                    if (dataBase.allUsers.containsKey(follower.userName) && !dataBase.LoggedIn.containsKey(follower.Id)) {
                        synchronized (dataBase.LoggedIn) {     //sync the case that the follower login in the same time of the post upload
                            PostMessage cloned = new PostMessage(PM);
                            cloned.notify = true;
                            follower.post2notify.add(cloned);        //adding the message to all of his followers notifications if the users not logged in
                        }
                    }
                    if (dataBase.allUsers.containsKey(follower.userName) && dataBase.LoggedIn.containsKey(follower.Id)) {
                        synchronized (dataBase.LoggedIn.get(follower.Id)) {         //sync the case that i send a post and my follower logout at the same time
                            PostMessage cloned = new PostMessage(PM);
                            cloned.notify = true;
                            synchronized (connection.SenderLock) {
                                connection.send(follower.connectionId, cloned);
                            }
                        }
                    }
                }
                for (String name : PM.HashTags) {//sends a notification to the tagged users
                    if (dataBase.allUsers.containsKey(name) && !dataBase.LoggedIn.containsKey(dataBase.allUsers.get(name).Id) && !ActiveUser.followers.contains(dataBase.allUsers.get(name))) {
                        synchronized (dataBase.allUsers.get(name)) {        //sync the case that the tagged user is trying to log in while the posting
                            synchronized (ActiveUser.followers) {       //in case the user starts to follow after we saw that he does not
                                PostMessage cloned = new PostMessage(PM);
                                cloned.notify = true;
                                dataBase.allUsers.get(name).post2notify.add(cloned);
                            }
                        }
                    }
                    if (dataBase.allUsers.containsKey(name) && dataBase.LoggedIn.containsKey(dataBase.allUsers.get(name).Id)) {
                        synchronized (dataBase.LoggedIn.get(dataBase.allUsers.get(name).Id)) {        //same reason as above
                            synchronized (ActiveUser.followers) {       //same
                                PostMessage cloned = new PostMessage(PM);
                                cloned.notify = true;
                                synchronized (connection.SenderLock) {
                                    connection.send(dataBase.allUsers.get(name).connectionId, cloned);
                                }
                            }
                        }
                    }
                }
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, PM);
                }
            } else {
                PM.isOK = false;
                PM.notify = false;
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, PM);
                }
            }
        }
        if (msg instanceof PMMessage) {       //PrivateMessage
            PMMessage PMM = (PMMessage) msg;
            PMM.setSender(this.ActiveUser.userName);
            if (ActiveUser == null || (!dataBase.allUsers.containsKey(ActiveUser.userName)) || (!dataBase.allUsers.containsKey(PMM.userName))) {      //error case in pm
                PMM.isOK = false;
                PMM.notify = false;
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, PMM);
                }
            } else {
                if (!dataBase.pms.containsKey(ActiveUser)) {//if he did not send anything yet
                    synchronized (dataBase.pms) {       //sync the case that 2 first private messages sent in the same time
                        dataBase.pms.put(ActiveUser, new LinkedList<PMMessage>());
                    }
                }
                dataBase.pms.get(ActiveUser).add(PMM);

                PMMessage cloned = new PMMessage(PMM);
                cloned.notify = true;
                if (!dataBase.LoggedIn.containsKey(dataBase.allUsers.get(PMM.userName).Id))//if the user the message was sent to is not online
                    dataBase.allUsers.get(cloned.userName).pm2notify.add(cloned);
                else {
                    synchronized (connection.SenderLock) {
                        connection.send(dataBase.allUsers.get(PMM.userName).connectionId, cloned);       //if he is logged in
                    }
                }
                synchronized (connection.SenderLock) {
                    connection.send(ActiveUser.connectionId, PMM);           //ack to the sender
                }
            }
        }
        if (msg instanceof UserListMessage) {         //UserList
            UserListMessage ULM = (UserListMessage) msg;
            if (ActiveUser != null && dataBase.allUsers.containsKey(ActiveUser.userName)) {
                synchronized (dataBase.allUsers.get(ActiveUser.userName)) {     //sync the case that im asking for userlist and while processing im trying to logout
                    for (User curruser : dataBase.allUsers.values()) {
                        ULM.userNamesList.add(curruser.userName);
                    }
                }
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, ULM);
                }
            } else {
                ULM.isOK = false;
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, ULM);
                }
            }
        }
        if (msg instanceof StatMessage) {             //Stat
            StatMessage SM = (StatMessage) msg;
            if (ActiveUser == null || !dataBase.LoggedIn.containsKey(ActiveUser.Id) || !dataBase.allUsers.containsKey(SM.UserName)) {
                SM.isOK = false;
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, SM);
                }
            } else {
                User otherUser = dataBase.allUsers.get(SM.UserName);
                if (dataBase.posts.get(otherUser) != null)
                    SM.numOfPosts = dataBase.posts.get(otherUser).size();//num of posts he posted
                SM.numOfFollowers = otherUser.followers.size();
                SM.numOfFollowing = otherUser.following.size();
                synchronized (connection.SenderLock) {
                    connection.send(connectionId, SM);
                }
            }
        }


    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
