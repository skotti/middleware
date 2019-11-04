package ch.ethz;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class Middleware {
    // connected clients array
    ArrayList<Socket> client;
    // reader for the corresponding client
    ArrayList<BufferedReader> readers;
    // writer for the corresponding client
    ArrayList<PrintWriter> writers;
    
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
        String PATTERN = "%m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.DEBUG);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
        
        this.client = new ArrayList<>();
        this.queue = new LinkedBlockingQueue<>(100);
        this.taskQueue = new LinkedBlockingQueue<>();
        this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();

	// args from calling class
	this.myIp = myIp;
        this.myPort = myPort;
        this.mcAddresses = mcAddresses;
        this.numThreads = numThreadsPTP;
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
        
        /* add initial connection*/
        this.client.add(this.queue.take());
        this.readers.add(new BufferedReader( new InputStreamReader(this.client.
                get(this.client.size() - 1).getInputStream())));
        this.writers.add(new PrintWriter(this.client.get(this.client.size() - 1).getOutputStream(), true));
        
        while (true) {
            for (int i = 0; i < this.client.size(); i++) {
                BufferedReader in = this.readers.get(i);
                StringBuilder finalRequest =  new StringBuilder();
                
                /*read input for current client if available*/
                if (in.ready()) {
                    // client could send at max 2 lines
                    // we read 1st line
                    finalRequest.append(in.readLine()).append("\r\n");
                    while (in.ready()) {
                        // read 2nd line
                        finalRequest.append(in.read()).append("\r\n");
                    }

                    QueueStructure st = new QueueStructure(finalRequest.toString(), this.client.get(i));
                    
                    /* enqueue in task queue */
                    st.enqueueTime = Instant.now();
                    this.taskQueue.add(st);
                }
                
                /*add new client if available*/
                while (!this.queue.isEmpty()) {
                    this.client.add(this.queue.take());
                    this.readers.add(new BufferedReader( new InputStreamReader(this.client.
                                                           get(this.client.size() - 1).getInputStream())));
                    this.writers.add(new PrintWriter(this.client.get(this.client.size() - 1).getOutputStream(), true));
                }
            }
        }
    }
}