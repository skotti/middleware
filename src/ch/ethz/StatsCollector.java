package ch.ethz;

import java.time.Instant;


public class StatsCollector {
    
    String request;
    Instant time1;
    Instant time2;
    
    public StatsCollector(String request, Instant time1, Instant time2) {
        this.request = request;
        this.time1 = time1;
        this.time2 = time2;
    }
    
    public String toString() {
        return this.request + " "+ this.time1 + " " +this.time2;
    }
}

