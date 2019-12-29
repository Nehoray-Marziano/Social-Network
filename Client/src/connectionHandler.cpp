//#include <connectionHandler.h>
#include "../include/connectionHandler.h"


using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
 
ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_),shouldTerminate(false){}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\0');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\0');
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
	bool result=sendBytes(frame.c_str(),frame.length());
	if(!result) return false;
	return sendBytes(&delimiter,1);
}
 
// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

short ConnectionHandler::bytesToShort(char* bytesArr){
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

void ConnectionHandler::shortToBytes(short num, char* bytesArr){
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

void ConnectionHandler::addShort2Vec(char *arr, std::vector<char> des){
        des.push_back(arr[0]);
        des.push_back(arr[1]);
}

std::vector<std::string> ConnectionHandler::split(std::string& s, char delimiter) {
    std::vector<std::string> tokens;
    std::string token;
    std::istringstream tokenstream(s);
    while (std::getline(tokenstream, token, delimiter)) {
        tokens.push_back(token);
    }
    return tokens;
}
void  ConnectionHandler::RegisterCase(std::string name, std::string password){
    short opCode=1;
    char byteArr[2];
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);
    sendLine(name);
    sendLine(password);


}

void ConnectionHandler::AddStringAsBytes(std::string input, std::vector<char> decoded){
    std::vector<char> bytes(input.begin(), input.end());//converting the string to a vector of bytes
    for(unsigned int i=0;i< bytes.size();i++)
        decoded.push_back(bytes[i]);
}

void ConnectionHandler::LoginCase(std::string name,std::string password){
    short opCode=2;
    char byteArr[2];
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);
    sendLine(name);
    sendLine(password);

}

void ConnectionHandler::LogoutCase() {
    short opCode=3;
    char byteArr[]={0,0};
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);

}

void ConnectionHandler::FollowCase(std::vector<string> output){
    std::vector<char> decoded;       //todo: delete
    short opCode=4;
    char byteArr[]={0,0};
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);
    if(output[1]=="0") {
        char FollowUnfollow[]={0};
        sendBytes(FollowUnfollow,1);
    }
    else {
        char FollowUnfollow[]={1};
        sendBytes(FollowUnfollow,1);
    }
    short numOfFollow=static_cast<short>(stoi(output[2]));
    char stoied[2];     //todo: delete?
    shortToBytes(numOfFollow,stoied);
    sendBytes(stoied,2);
    for(unsigned int i=3;i<output.size();i++)
        sendLine(output[i]);

}
void ConnectionHandler::PostCase(std::vector<string> output){           //PostCase
    short opCode=5;
    char byteArr[]={0,0};
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);
    std::string finalOutput="";         // this string will contain all the post's content
    for(unsigned int i=1;i<output.size()-1;i++)
        finalOutput+=output[i]+" ";         // a whitespace between each word, except for the last one
    finalOutput+=output[output.size()-1];
    sendLine(finalOutput);
}


void ConnectionHandler::PrivateMessageCase(std::vector<std::string> output){        //Private Mesage Case
    short opCode=6;
    char byteArr[]={0,0};
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);
    sendLine(output[1]);
    string ToSend="";
    for(unsigned int i=2;i<output.size()-1;i++)
        ToSend+= output[i]+" ";
    ToSend+=output[output.size()-1];
    sendLine(ToSend);

}

void ConnectionHandler::UserListCase() {
    short opCode=7;
    char byteArr[]={0,0};
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);

}

void ConnectionHandler::StatCase(std::vector<std::string> output){
    short opCode=8;
    char byteArr[]={0,0};
    shortToBytes(opCode,byteArr);
    sendBytes(byteArr,2);
    sendLine(output[1]);

}









