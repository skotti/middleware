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
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class Middleware {
    // connected clients array
    ArrayList<Connection> connections;
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
    Timer statsPrinter;

    StatPrinter printer;
    
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
        
	// args from calling class
	    this.myIp = myIp;
        this.myPort = myPort;
        this.mcAddresses = mcAddresses;
        this.numThreads = numThreadsPTP;
    }
    
    public void dump(Instant time) {
        printer.dumpAtShutdown();
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

        printer = new StatPrinter(this.threads);
        Timer timer = new Timer("StatPrinter");
     
        long delay  = 1;
        long period = 5000;
        timer.scheduleAtFixedRate(printer, delay, period);
      
        Thread connectionAcceptor = new ConnectionAcceptor(this.queue, this.myIp, this.myPort);
        connectionAcceptor.start();

        HashMap<Socket, Instant> timeOuts = new HashMap<Socket, Instant>();
        
        
        
        while (true) {
            
            if (this.connections.isEmpty()) {
                Socket s = this.queue.take();
                logger.debug("[MIDDLEWARE] EXPERIMENT STARTED\n");
                Connection con = new Connection(s, new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                                                     new PrintWriter(s.getOutputStream(), true), new char[256]);
                this.connections.add(con);
                timeOuts.put(s, Instant.now());
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
                    Instant enqueueTime = Instant.now();
                    QueueStructure st = new QueueStructure(finalRequest.toString(), curConn.socket);
                    st.enqueueTime = enqueueTime;
                    timeOuts.replace(curConn.socket, st.enqueueTime);
                    this.taskQueue.add(st);
                    curConn.request.setLength(0);
                } else if (Duration.between(timeOuts.get(curConn.socket), Instant.now()).toMillis() > 5000) {
                    iter.remove();
                    logger.debug("CLIENT DISCONNECTED\n");
                    continue;
                }
            }

            while (!this.queue.isEmpty()) {
                Socket s = this.queue.take();
                Connection con = new Connection(s, new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                                                     new PrintWriter(s.getOutputStream(), true), new char[256]);
                connections.add(con);    
                timeOuts.put(s, Instant.now());
            }
        }
    }
}
