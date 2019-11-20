package ch.ethz;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionStructure {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    char[] buffer;
    
    public ConnectionStructure(Socket socket, BufferedReader reader, PrintWriter writer, char[] buffer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.buffer = buffer;
    }
}