package ch.ethz;

import java.time.Instant;


public class QueueMessage {
    
    String request;
    Instant time1;
    Instant time2;
    
    public QueueMessage(String request, Instant time1, Instant time2) {
        this.request = request;
        this.time1 = time1;
        this.time2 = time2;
    }
    
    public String toString() {
        return this.request + " "+ this.time1 + " " +this.time2;
    }
}

