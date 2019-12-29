//
// Created by achikamm@wincs.cs.bgu.ac.il on 1/1/19.
//

#include "../include/ReadFromKeyboard.h"

ReadFromKeyboard::ReadFromKeyboard(ConnectionHandler *CH) : CH(CH) {}

void ReadFromKeyboard::run() {
    while (!CH->shouldTerminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        std::vector<std::string> sentence = ConnectionHandler::split(line,
                                                                     ' ');// splitting the string from the keyboard
        std::vector<char> put;

        //*****************************ENCODING CASES********************************
        if (sentence[0] == "REGISTER") {
            CH->RegisterCase(sentence[1], sentence[2]);
        } else if (sentence[0] == "LOGIN")
            CH->LoginCase(sentence[1], sentence[2]);

        else if (sentence[0] == "LOGOUT") {
            CH->LogoutCase();
        }

        else if (sentence[0] == "FOLLOW")
            CH->FollowCase(sentence);
        else if (sentence[0] == "POST")
            CH->PostCase(sentence);
        else if (sentence[0] == "PM")
            CH->PrivateMessageCase(sentence);
        else if (sentence[0] == "USERLIST")
            CH->UserListCase();
        else if (sentence[0] == "STAT")
            CH->StatCase(sentence);

    }
}