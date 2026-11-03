MyRedis 2.0

A Redis-inspired key-value database built from scratch using Java.

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

Technologies Used

- Java
- Socket Programming
- Multithreading
- ConcurrentHashMap
- File Handling
- RESP Protocol

How to Run

Compile the project:

mkdir out
javac -d out src/*.java

Start the server:

java -cp out RedisServer

Open another terminal and start the client:

java -cp out RedisClient

Example Commands

SET name Aditya
GET name
EXISTS name
EXPIRE name 30
TTL name
DEL name
DBSIZE
PING

Project Purpose

This project was developed to understand how a simple Redis-like database works internally, including networking, command handling, expiration, concurrency and persistence.

Limitations

This is an educational Redis-inspired implementation. It is not a complete replacement for Redis.MyRedis 2.0

A Redis-inspired key-value database built from scratch using Java.

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

Technologies Used

- Java
- Socket Programming
- Multithreading
- ConcurrentHashMap
- File Handling
- RESP Protocol

How to Run

Compile the project:

mkdir out
javac -d out src/*.java

Start the server:

java -cp out RedisServer

Open another terminal and start the client:

java -cp out RedisClient

Example Commands

SET name Aditya
GET name
EXISTS name
EXPIRE name 30
TTL name
DEL name
DBSIZE
PING

Project Purpose

This project was developed to understand how a simple Redis-like database works internally, including networking, command handling, expiration, concurrency and persistence.

Limitations

This is an educational Redis-inspired implementation. It is not a complete replacement for Redis.