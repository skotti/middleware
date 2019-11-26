package ch.ethz;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
    Structure, that contains:
    1) Socket for the connection;
    2) Objects for reading and writing to this connection
    3) Buffer for the read() call
    4) String buffer which stores intermediate results

*/
public class Connection {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    char[] buffer;
    StringBuilder request;
    
    public Connection(Socket socket, BufferedReader reader, PrintWriter writer, char[] buffer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.buffer = buffer;
        this.request = new StringBuilder();
    }
}