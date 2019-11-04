package ch.ethz;

import java.net.Socket;
import java.time.Instant;

public class QueueStructure {
    String request;
    Socket connection;
    Instant enqueueTime;
    
    public QueueStructure (String request, Socket connection) {
        this.request = request;
        this.connection = connection;
    }
}