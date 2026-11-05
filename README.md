MyRedis 2.0

A Redis-inspired key-value database built from scratch using Java.

Overview

MyRedis is an educational project that demonstrates how a simple in-memory key-value database works. It uses Java socket programming to communicate between a client and server.

Features

- TCP client-server communication
- SET, GET and DEL commands
- EXISTS and KEYS commands
- EXPIRE and TTL support
- Automatic expiration cleanup
- Multithreaded client handling
- Data persistence using file handling
- Database recovery after server restart
- RESP2 protocol subset
- SAVE, FLUSHDB and DBSIZE commands

Technologies Used

- Java
- Socket Programming
- Multithreading
- ConcurrentHashMap
- File Handling
- RESP Protocol

Project Structure

MyRedis/
├── Data/
├── Out/
├── Src/
│   ├── RedisServer.java
│   ├── RedisClient.java
│   ├── RedisCommandHandler.java
│   ├── RedisDatabase.java
│   ├── RedisEntry.java
│   ├── PersistenceManager.java
│   └── RespProtocol.java
├── .gitignore
└── README.md

How to Run

Make sure Java JDK is installed.

Open the terminal in the project root folder.

1. Compile the project

mkdir Out
javac -d Out Src\*.java

2. Start the server

java -cp Out RedisServer

3. Start the client

Open another terminal in the project root folder:

java -cp Out RedisClient

Example Commands

PING
SET name Aditya
GET name
EXISTS name
EXPIRE name 30
TTL name
KEYS *
DBSIZE
DEL name
SAVE
FLUSHDB

Testing

The project was manually tested for:

- Basic SET and GET operations
- Key deletion and existence checks
- Key expiration
- Database size and key listing
- Saving and recovering data after server restart
- Multiple client connections
- Invalid command handling

Project Purpose

This project was developed to understand how a simple Redis-like database works internally, including networking, command handling, expiration, concurrency and persistence.

Limitations

This is an educational Redis-inspired implementation. It supports only a subset of Redis commands and RESP2 functionality. It is not a complete replacement for Redis.

Future Improvements

- Add automated unit tests
- Improve error handling
- Add more Redis commands
- Improve client interface