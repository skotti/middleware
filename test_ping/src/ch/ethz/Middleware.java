package ch.ethz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Middleware {
    // connected clients array
    ArrayList<Connection> connections;
    // queue for ConnectionAcceptor to add new clients
    BlockingQueue<Socket> queue;
    
    // ip on which middleware is running
    String myIp;
    // port on which middlewae is running
    int myPort = 0;
    // server addresses (with memcached)
    List<String> mcAddresses = null;
    
    public Middleware(String myIp, int myPort, List<String> mcAddresses, int numThreadsPTP, boolean readSharded) {

        this.connections = new ArrayList<>();
        this.queue = new LinkedBlockingQueue<>(100);
        
	// args from calling class
	this.myIp = myIp;
        this.myPort = myPort;
        this.mcAddresses = mcAddresses;
    }
    
    public void run () throws Exception {
        
        Thread connectionAcceptor = new ConnectionAcceptor(this.queue, this.myIp, this.myPort);
        connectionAcceptor.start();

        while (true) {
            
            if (this.connections.isEmpty()) {
                Socket s = this.queue.take();
                Connection con = new Connection(s, new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                                                     new PrintWriter(s.getOutputStream(), true), new char[256]);
                this.connections.add(con);
            }

            for (ListIterator<Connection> iter = this.connections.listIterator(); iter.hasNext();) {
                
                Connection curConn = iter.next();
                BufferedReader in = curConn.reader;
                StringBuilder finalRequest = curConn.request;
                
                if (in.ready()) {
                    while (in.ready()) {
                        int n = in.read(curConn.buffer, 0, 256);
                        finalRequest.append(curConn.buffer, 0, n);
                    }

                    // if we managed to read one line, then we just forward it.
                    // it will either correct GET requests or just something
                    if (finalRequest.charAt(finalRequest.length() - 1) != '\n') {
                        continue;
                        //we need to read more until \n is read
                    }
                    curConn.request.setLength(0);
		    curConn.writer.write("END\r\n");
        	    curConn.writer.flush();
                }
            }

            while (!this.queue.isEmpty()) {
                Socket s = this.queue.take();
                Connection con = new Connection(s, new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                                                     new PrintWriter(s.getOutputStream(), true), new char[256]);
                connections.add(con);    
            }
        }
    }
}
