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
    ArrayList<Socket> client;
    // reader for the corresponding client
    /*ArrayList<BufferedReader> readers;
    ArrayList<PrintWriter> writers;*/
    HashMap<Socket, BufferedReader> readers;
    HashMap<Socket, PrintWriter> writers;
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
        String PATTERN = "%p %c{2}: %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.DEBUG);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
        
        this.client = new ArrayList<>();
        this.queue = new LinkedBlockingQueue<>(100);
        this.taskQueue = new LinkedBlockingQueue<>();
        /*this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();*/
        this.readers = new HashMap<>();
        this.writers = new HashMap<>();
        
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
        System.out.println("num threads ="+this.numThreads);
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

        HashMap<Socket, Instant> timeOuts = new HashMap();
        
        
        while (true) {
            
            if (this.client.isEmpty()) {
                Socket s = this.queue.take();
                this.client.add(s);
                logger.debug("[MIDDLEWARE] EXPERIMENT STARTED");
                this.readers.put(this.client.get(0), 
                                 new BufferedReader( new InputStreamReader(this.client.
                                 get(this.client.size() - 1).getInputStream())));
                this.writers.put(this.client.get(0), 
                                 new PrintWriter(this.client.get(this.client.size() - 1).getOutputStream(), true));
                timeOuts.put(s, Instant.now());
            }
            
            for (ListIterator<Socket> iter = this.client.listIterator(); iter.hasNext();) {
                
                Socket curSocket = iter.next();
                BufferedReader in = this.readers.get(curSocket);
                StringBuilder finalRequest =  new StringBuilder();
                
                if (in.ready()) {
                    
                    finalRequest.append(in.readLine()).append("\r\n");
                    while (in.ready()) {
                        finalRequest.append(in.read()).append("\r\n");
                    }

                    QueueStructure st = new QueueStructure(finalRequest.toString(), curSocket);
                    
                    st.enqueueTime = Instant.now();
                    timeOuts.replace(curSocket, st.enqueueTime);
                    this.taskQueue.add(st);
                    
                } else if (Duration.between(timeOuts.get(curSocket), Instant.now()).toMillis() > 5000) {
                    iter.remove();
                    logger.debug("CLIENT DISCONNECTED");
                    continue;
                }
                
                while (!this.queue.isEmpty()) {
                    Socket newSocket = this.queue.take();
                    iter.add(newSocket);

                    this.readers.put(newSocket,
                                     new BufferedReader(new InputStreamReader(newSocket.getInputStream())));                   
                    this.writers.put(newSocket, 
                                     new PrintWriter(newSocket.getOutputStream(), true));      
                    timeOuts.put(newSocket, Instant.now());
                }
            }
        }
    }
}