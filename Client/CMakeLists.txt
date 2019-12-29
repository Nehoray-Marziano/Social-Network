
cmake_minimum_required(VERSION 3.12.3)
project(Client)

set(CMAKE_CXX_STANDARD 14)


find_package(Boost COMPONENTS system filesystem thread REQUIRED)
IF (Boost_FOUND)
    include_directories(${Boost_INCLUDE_DIR})
endif()


SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread -lboost_thread")

include_directories(include)

add_executable(Client src/echoClient.cpp src/ReadFromKeyboard.cpp include/ReadFromKeyboard.h src/connectionHandler.cpp include/connectionHandler.h makefile)


target_link_libraries( Client LINK_PUBLIC ${Boost_LIBRARIES} )







