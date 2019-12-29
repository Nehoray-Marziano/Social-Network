//
// Created by achikamm@wincs.cs.bgu.ac.il on 1/1/19.
//

#ifndef BOOST_ECHO_CLIENT_READFROMKEYBOARD_H
#define BOOST_ECHO_CLIENT_READFROMKEYBOARD_H


#include "../include/connectionHandler.h"

class ReadFromKeyboard {
public:
    ReadFromKeyboard(ConnectionHandler* CH);
    void run();

private:
    ConnectionHandler* CH;
};


#endif //BOOST_ECHO_CLIENT_READFROMKEYBOARD_H
