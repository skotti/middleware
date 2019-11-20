package ch.ethz;

import org.apache.log4j.Logger;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class Middleware {
    // connected clients array
    ArrayList<ConnectionStructure> connections;
    //ArrayList<Socket> client;
    // reader for the corresponding client
    /*ArrayList<BufferedReader> readers;
    ArrayList<PrintWriter> writers;*/
    //HashMap<Socket, BufferedReader> readers;
    //HashMap<Socket, PrintWriter> writers;
    // writer for the corresponding client
    // queue for ConnectionAcceptor to add new clients
    BlockingQueue<Socket> queue;
    // queue for adding requests
    LinkedBlockingQueue<QueueStructure> taskQueue;
    // array of worker threads
    ArrayList<WorkerThread> threads;
    // number of worker threads
    int numThreads = -1;
    
    // ip on which middleware is running
    String myIp;
    // port on which middlewae is running
    int myPort = 0;
    // server addresses (with memcached)
    List<String> mcAddresses = null;
    // logger
    final static Logger logger = Logger.getLogger(Middleware.class);
    
    //ConcurrentLinkedQueue<QueueStructure> taskQueue;
    
    public Middleware(String myIp, int myPort, List<String> mcAddresses, int numThreadsPTP, boolean readSharded) {
        
        ConsoleAppender console = new ConsoleAppender();
        String PATTERN = "%p %c{2}: %m";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.DEBUG);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
        
        this.connections = new ArrayList<>();
        this.queue = new LinkedBlockingQueue<>(100);
        this.taskQueue = new LinkedBlockingQueue<>();
        /*this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();*/
        //this.readers = new HashMap<>();
        //this.writers = new HashMap<>();
        
	// args from calling class
	    this.myIp = myIp;
        this.myPort = myPort;
        this.mcAddresses = mcAddresses;
        this.numThreads = numThreadsPTP;
    }
    
    public void dump() {
        for (int i = 0; i < this.numThreads; i++) {
            this.threads.get(i).dump(0, Instant.now());
        }
    }

    public void run () throws Exception {
        
        /* manage threads*/
        this.threads = new ArrayList<>();
        for (int i = 0; i < this.numThreads; i++) {
            try {
                this.threads.add(new WorkerThread(i, this.taskQueue, this.mcAddresses));
            } catch (IOException ex) {
                logger.error("Middleware exception", ex);
            }
            this.threads.get(i).start();
        }
      
        Thread connectionAcceptor = new ConnectionAcceptor(this.queue, this.myIp, this.myPort);
        connectionAcceptor.start();

        HashMap<Socket, Instant> timeOuts = new HashMap<Socket, Instant>();
        
        
        while (true) {
            
            if (this.connections.isEmpty()) {
                Socket s = this.queue.take();
                logger.debug("[MIDDLEWARE] EXPERIMENT STARTED\n");
                ConnectionStructure con = new ConnectionStructure(s, new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                                                     new PrintWriter(s.getOutputStream(), true), new char[256]);
                this.connections.add(con);
                timeOuts.put(s, Instant.now());
            }
            
            for (ListIterator<ConnectionStructure> iter = this.connections.listIterator(); iter.hasNext();) {
                
                ConnectionStructure curConn = iter.next();
                BufferedReader in = curConn.reader;
                StringBuilder finalRequest =  new StringBuilder();
                
                if (in.ready()) {
                    Instant enqueueTime = Instant.now();
                    while (in.ready()) {
                        int n = in.read(curConn.buffer, 0, 256);
                        finalRequest.append(curConn.buffer, 0, n);
                    }
                    if (finalRequest.charAt(finalRequest.length() - 1) != '\n') {
                        continue;
                        //we need to read more until \n is read
                    }

                    QueueStructure st = new QueueStructure(finalRequest.toString(), curConn.socket);
                    st.enqueueTime = enqueueTime;
                    timeOuts.replace(curConn.socket, st.enqueueTime);
                    this.taskQueue.add(st);
                    
                } else if (Duration.between(timeOuts.get(curConn.socket), Instant.now()).toMillis() > 5000) {
                    iter.remove();
                    logger.debug("CLIENT DISCONNECTED\n");
                    continue;
                }
            }

            while (!this.queue.isEmpty()) {
                Socket s = this.queue.take();
                ConnectionStructure con = new ConnectionStructure(s, new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                                                     new PrintWriter(s.getOutputStream(), true), new char[256]);
                connections.add(con);    
                timeOuts.put(s, Instant.now());
            }
        }
    }
}
