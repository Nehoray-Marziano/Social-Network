#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <thread>
#include "../include/ReadFromKeyboard.h"

using namespace std;
bool shouldTerminate=false;
bool LogedIn=false;
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/



int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    ReadFromKeyboard keyboard(&connectionHandler);
    std::thread thread1(&ReadFromKeyboard::run,&keyboard);

    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

	//From here we will see the rest of the ehco client implementation:
    while (!shouldTerminate) {

 
        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character
        std::string answer;
        //****************************DECODE CASES*******************************************
        char reader[2];
        connectionHandler.getBytes(reader,2);//     Getting the opCode from the server
        short FirstOpCode=connectionHandler.bytesToShort(reader);
        if(FirstOpCode==9){          //NOTIFICATION case
            answer+="NOTIFICATION ";
            char pmORpost[1];
            connectionHandler.getBytes(pmORpost,1);
            if(pmORpost[0]=='0'){     //Private Message
                answer+="PM ";
                string tmpS;
                connectionHandler.getFrameAscii(answer,0);      //ADD the posterName
                answer=answer.substr(0,answer.length()-1)+" "; //we dont want to include the 0 byte in our answer
                connectionHandler.getFrameAscii(answer,0);      //Add the content
                answer=answer.substr(0,answer.length()-1); //we dont want to include the 0 byte in our answer

                cout<<answer<< endl;            //Printing the answer
            }
            else{           //this is a Post
                answer+="Public ";
                connectionHandler.getFrameAscii(answer,0);      //ADD the posterName
                answer=answer.substr(0,answer.length()-1)+" "; //we dont want to include the 0 byte in our answer
                connectionHandler.getFrameAscii(answer,0);      //Add the content
                answer=answer.substr(0,answer.length()-1); //we dont want to include the 0 byte in our answer

                cout<<answer<< endl;            //Printing the answer
            }
        }
        else if(FirstOpCode==10){         //ACK case
            answer+="ACK ";
            connectionHandler.getBytes(reader,2);//     Getting the opCode from the server
            short SecondOpCode=connectionHandler.bytesToShort(reader);//getting the second opCode

            //*******************************ALL ACK CASES**********************************

            if(SecondOpCode==1){// register ack
                answer+="1 ";
                cout<< answer << endl;
            }
            else if(SecondOpCode==2){//login ack
                answer+="2 ";
                cout<<answer<< endl;
                LogedIn=true;
            }
            else if(SecondOpCode==3){//logout case . we need to make sure that the operation was a success
                answer+="3";
                cout<<answer<< endl;
                connectionHandler.close();
                if(LogedIn)
                    shouldTerminate=true;
            }
            else if(SecondOpCode==4){//follow/unfollow case
                answer+="4 ";

                char numOfUsers[2];
                connectionHandler.getBytes(numOfUsers,2);
                short UsersAmount=connectionHandler.bytesToShort(numOfUsers);       //getting the amount of successful follow/unfollow
                int temp=UsersAmount;
                answer=answer+to_string(temp)+" ";

                string names;
                for(int i=0;i<temp;i++){//getting all the names (depending on the number of them) and adding them to the answer string
                    connectionHandler.getLine(names);
                    names=names.substr(0,names.length()-1);
                    answer += names + " ";
                    names="";
                }

                cout<<answer<< endl;
            }
            else if(SecondOpCode==5){//post case
                answer+="5 ";
                cout<<answer<< endl;
            }
            else if(SecondOpCode==6){// pm case
                answer+="6 ";
                cout<<answer<< endl;
            }
            else if(SecondOpCode==7){//userList case
                answer+="7 ";
                connectionHandler.getBytes(reader,2);
                short numOfUser=connectionHandler.bytesToShort(reader);//getting the number of users
                answer+=to_string(numOfUser)+" ";
                string userName="";
                string tmp="";
                for(int i=0;i<numOfUser;i++){
                    connectionHandler.getFrameAscii(userName,0);
                    tmp=userName.substr(0,userName.size()-1);
                    answer+=tmp+" ";
                    tmp="";
                    userName="";
                }
                cout<<answer<< endl;
            }
            else if(SecondOpCode==8){//stat case
                answer+="8 ";
                connectionHandler.getBytes(reader,2);
                short numOfPosts=connectionHandler.bytesToShort(reader);//getting the number of users
                answer+=to_string(numOfPosts)+" ";
                connectionHandler.getBytes(reader,2);
                short numOfFollowers=connectionHandler.bytesToShort(reader);//getting the number of users
                answer+=to_string(numOfFollowers)+" ";
                connectionHandler.getBytes(reader,2);
                short numOfFollowing=connectionHandler.bytesToShort(reader);//getting the number of users
                answer+=to_string(numOfFollowing);
                cout<<answer<< endl;
            }

        }
        //*******************************ERROR CASE*************************************

        else if(FirstOpCode==11){                   //opCode=11 , Error case
            connectionHandler.getBytes(reader,2);//     Getting the opCode from the server
            short SecondOpCode=connectionHandler.bytesToShort(reader);//getting the second opCode
            answer="ERROR ";
            if(SecondOpCode==1){// register case

                answer+="1";
                cout<<answer<<endl;
            }
            else if(SecondOpCode==2){// login case
                answer+="2";
                cout<<answer<<endl;
            }
            else if(SecondOpCode==3){// logout case
                answer+="3";
                shouldTerminate=false;
                cout<<answer<<endl;
            }
            else if(SecondOpCode==4){// follow/unfollow case
                answer+="4";
                cout<<answer<<endl;
            }
            else if(SecondOpCode==5){// post case
                answer+="5";
                cout<<answer<<endl;
            }
            else if(SecondOpCode==6){// pm case
                answer+="6";
                cout<<answer<<endl;
            }
            else if(SecondOpCode==7){// userlist case
                answer+="7";
                cout<<answer<<endl;
            }
            else if(SecondOpCode==8){// stats case
                answer+="8";
                cout<<answer<<endl;
            }
        }



        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (shouldTerminate) {           //if (!connectionHandler.getLine(answer))
            break;
        }

		// A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
		// we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
       // answer.resize(len-1);
       /* std::cout << "/usr/include/boost: " << answer << " " << len << " bytes " << std::endl << std::endl;*/
        if (answer == "bye") {
            std::cout << "Exiting...\n" << std::endl;
            break;
        }
    }
    thread1.detach();
    return 0;
}
