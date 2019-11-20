package ch.ethz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;



public class WorkerThread extends Thread {
    /* worker thread needs three connections
        - one to write to server
        - one to read from server
        - one to write to client
    */
    ArrayList<ConnectionStructure> connections;
    /*ArrayList<Socket> servers;
    ArrayList<PrintWriter> outputs;
    ArrayList<BufferedReader> inputs;*/
    
    // we don't need private queue for self managed workers
    LinkedBlockingQueue<QueueStructure> taskQueue;
    // thread id
    int threadNumber;
    // number of servers, to which this thread is connected
    int nServers;
    // number of server, which should receive and send message currently
    int serverIndex;
    // time when worker started working ( for statistics dump)
    Instant startTime;
    
    // requests, that returned from the server with positive answer
    int successfulRequests;
    
    // we don't want to write delta, spend in queue, by every element
    // so we accumulate
     
    // accumulated time, spent by reqeusts in queue
    long timeInQueue;
    // accumulated sizes of queue after each dequeing
    long sizeOfQueue;
    // requests, for which we are counting this time
    long requestsLeftQueue;
    // accumulated service processing (server + sending) time for successful requests
    long timeInServer;
    // accumulated processin time between getting from queue ans sending to server
    long timeInParseAndSend;
    // accumulated overall response time in middleware
    long timeToProcessRequest;
    // accumulated time to process request plus queue time
    long timeToProcessRequestAndQueueTime;
    // accumulated cache misses
    int cacheMisses;
    // accumulated error strings
    StringBuilder errors;
    // used for calculating the think time
    long allRequestsLeftQueue = 0;
    
    // logger
    final static Logger logger = Logger.getLogger(WorkerThread.class);
    
    // for internal debug
    int[] requestsPerServer;
    
    // for think time
    long timeSendReceive;
    Instant timeSend;
    
    //left variables, that may be used later
    //BlockingQueue<QueueStructure> queue;
    //ConcurrentLinkedQueue<QueueStructure> taskQueue;
    //number of get Operations during all execution
    //int getOperationsNumber;
    //FileWriter writer;
    //String fileName;
    //ConcurrentLinkedQueue<StatsCollector> stats;
    
    public WorkerThread(int i, LinkedBlockingQueue<QueueStructure> taskQueue,
                        /*ConcurrentLinkedQueue<QueueStructure> taskQueue*/
                        List<String> serverAddrs) throws IOException {
        
        /* set up predefined addresses currently, for local testing*/
        this.taskQueue = taskQueue;
        nServers = serverAddrs.size();
        
        ArrayList<Integer> portArray = new ArrayList<>();
        ArrayList<String> ipArray = new ArrayList<>();
        for (int j = 0; j < serverAddrs.size(); j++) {
            String[] data = serverAddrs.get(j).split(":");
            ipArray.add(data[0]);
            portArray.add(Integer.parseInt(data[1]));
        }

        requestsPerServer = new int[nServers]; 

        /* connect to the servers*/
        connections = new ArrayList<>();
        //servers = new ArrayList<>();
        //outputs = new ArrayList<>();
        //inputs = new ArrayList<>();
        for (int j = 0; j < nServers; j++) {
            Socket s = new Socket(ipArray.get(j), portArray.get(j));
            ConnectionStructure con = 
                                    new ConnectionStructure(s,
                                    new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                    new PrintWriter(s.getOutputStream(), true),
                                    new char[256]);
            connections.add(con);
            //servers.add(new Socket(ipArray.get(j), portArray.get(j)));
            //outputs.add(new PrintWriter(servers.get(j).getOutputStream(), true));
            //inputs.add(new BufferedReader(new InputStreamReader(servers.get(j).getInputStream())));
            requestsPerServer[j] = 0;
        }
        // thread id
        threadNumber = i;
        // start sending to server 0
        serverIndex = 0;
        
        // for requests statistics
        successfulRequests = 0;
        // for statistics ( all for accumulation)
        timeInQueue = 0;
        sizeOfQueue = 0;
        requestsLeftQueue = 0;
        timeInServer = 0;
        timeToProcessRequest = 0;
        timeToProcessRequestAndQueueTime = 0;
        cacheMisses = 0;
        allRequestsLeftQueue = 0;
        errors = new StringBuilder();
        timeSendReceive = 0;
    }
    
    
    public void dump(long diff, Instant time) {
        
        if (diff == 0) {
             diff = time.toEpochMilli()- startTime.toEpochMilli();
        }
        StringBuilder log = new StringBuilder();
        log.append("d ").append(threadNumber)
              .append(" ").append(Long.toString(timeInQueue))
              .append(" ").append(Long.toString(timeInParseAndSend))
              .append(" ").append(Long.toString(timeInServer))
              .append(" ").append(Long.toString(timeToProcessRequest))
              .append(" ").append(Long.toString(timeToProcessRequestAndQueueTime))
              .append(" ").append(Long.toString(sizeOfQueue))
              .append(" ").append(Long.toString(requestsLeftQueue))
              .append(" ").append(Long.toString(successfulRequests))
	      .append(" ");

	/*for (int j = 0; j < nServers; j++) {
            log.append(requestsPerServer[j]).append(" ");
	}*/
        log.append(Long.toString(diff)).append("\n");
	
        logger.debug(log);
        
        successfulRequests = 0;
        sizeOfQueue = 0;
        requestsLeftQueue = 0;
        timeInQueue = 0;
        timeInServer = 0;
        timeInParseAndSend = 0;
        timeToProcessRequest = 0;
        timeToProcessRequestAndQueueTime = 0;
        cacheMisses = 0;

	for (int j = 0; j < nServers; j++) {
            requestsPerServer[j] = 0;
        }
 
        startTime = Instant.now();
    }
    
    public void sendResponse(String answerString, OutputStream stream) {
        PrintWriter outputClient = new PrintWriter(stream, true);
        // for measuring once a think time
        timeSend = Instant.now();
        
        outputClient.write(answerString);
        outputClient.flush();
    }
    
    @Override
    public void run() {
        QueueStructure st = null;
        startTime = Instant.now();
        
        while (true) {
            // record time exactly after dequeing, even if the reqeust erroneous
            try {
                st = taskQueue.take();
            } catch (InterruptedException ex) {
                logger.error("WorkerThread exception", ex);
            }
            
            //-------------------------------------collect stats
            Instant dequeueTime = Instant.now();    //--------------------------------------------------------------------------end POINT1, time in queue
            Instant startParseAndSend = dequeueTime;//--------------------------------------------------------------------------start POINT2, time parse and send
            int sizeOfCurrentQueue = taskQueue.size();
            //-------------------------------------

            // check if get request and that we have only one key
            if (!st.request.startsWith("get ") || 
                (st.request.startsWith("get ") && !(st.request.split(" ").length == 2))) {
                logger.error("NOT GET REQUEST (OR NOT CORRECT GET):\n" + st.request);
                PrintWriter outputClient = null;
                try {
                    outputClient = new PrintWriter(st.connection.getOutputStream(), true);
                } catch ( IOException ex) {
                    logger.error("WorkerThread exception", ex);
                }
                outputClient.write("END\r\n");
                //outputClient.write("ERROR\r\n");
                outputClient.flush();
                continue;
            }

            //-------------------------------------collect stats
            long timeElapsed = Duration.between(
                    st.enqueueTime, 
                    dequeueTime).toNanos();
            timeInQueue += timeElapsed;
            requestsLeftQueue += 1;
            sizeOfQueue += sizeOfCurrentQueue;

            // see if we should dump
            if (timeInQueue > Long.MAX_VALUE / 2 ||
                sizeOfQueue > Long.MAX_VALUE / 2 ||
                requestsLeftQueue > Long.MAX_VALUE / 2) {
                long diff = Instant.now().toEpochMilli()- startTime.toEpochMilli();
                dump(diff, null);
            }
            //-------------------------------------
            //if (st != null) {
            
            // send request to one of the server in round robin manner
            // for correct answer to get request we will have three parts
            StringBuilder answerString = new StringBuilder();
            String part1, part3;
            //String part1, part3;

            connections.get(serverIndex).writer.write(st.request);
            connections.get(serverIndex).writer.flush();
            
            //-------------------------------------collect stats
            requestsPerServer[serverIndex] += 1;
            Instant endParseAndSend = Instant.now();//-------------------------------------------------------------------------end POINT2, time parse and send
            timeInParseAndSend += Duration.between(
                    startParseAndSend, 
                    endParseAndSend).toNanos();
            Instant serverProcessStart = endParseAndSend;//--------------------------------------------------------------------start POINT3, server processing
            //-------------------------------------

            try {

                BufferedReader in = connections.get(serverIndex).reader;
                part1 = in.readLine();
                //--------------------------------------------------------------------------------------------------------------end POINT3, server processing
                //Instant serverProcessEnd = Instant.now();
                //---------------------------------------
                String[] parts = part1.toString().split(" ");
                if (!parts[0].equals("VALUE")) {
                    if (parts[0].startsWith("END")) {
                        //cache miss
                        logger.info("CACHE MISS\n");
                        cacheMisses += 1;
                        answerString.append("END\r\n");
                        sendResponse(answerString.toString(), 
                                     st.connection.getOutputStream());
                        continue;  
                    } else {
                        // some error
                        // read until the end all response
                        logger.info("UKNOWN ERROR\n");
                        answerString.append(part1).append("\r\n");
                        while (in.ready()) {
                            answerString.append(in.read());
                        }
                        answerString.append("\r\n");
                        sendResponse(answerString.toString(),
                                     st.connection.getOutputStream());
                        continue;
                    }
                } else {
                    // get numBytes to read as data
                    
                    int numBytes = Integer.parseInt(parts[3]);

                    int bytesToBeRead = numBytes + 2; // reserve two for \r\n
                    char[] part2 = new char[bytesToBeRead];

                    int offset = 0;
                    while (offset != bytesToBeRead) {
                        offset = in.read(part2, offset, bytesToBeRead - offset);
                    }
                    
                    part3 = in.readLine();// END

                    if (!part3.startsWith("END")) {
                        // some error
                        // read until the end all response
                        logger.info("UNKNOWN ERROR");
                        answerString.append(part1)
                                    .append("\r\n")
                                    .append(part2)
                                    .append(part3)
                                    .append("\r\n");
                        while (in.ready()) {
                            answerString.append(in.read());
                        }
                        sendResponse(answerString.toString(),
                                     st.connection.getOutputStream());
                        continue;
                        // error, read until the end and send the whole answer
                    }

                    answerString.append(part1)
                                .append("\r\n")
                                .append(part2)
                                .append(part3)
                                .append("\r\n");
                }

                //----------------------------------------collect stats
		        sendResponse(answerString.toString(),
                             st.connection.getOutputStream());

		        Instant serverProcessEnd = Instant.now();
                timeToProcessRequest += Duration.between(
                                       dequeueTime, 
                                       serverProcessEnd).toNanos();
                timeToProcessRequestAndQueueTime += Duration.between(
                                                    st.enqueueTime, 
                                                    serverProcessEnd).toNanos();
                timeInServer += Duration.between(
                                        serverProcessStart,
                                        serverProcessEnd).toNanos();
                successfulRequests += 1;
                //----------------------------------------

                //sendResponse(answerString.toString(),
                //             st.connection.getOutputStream());
            } catch (IOException ex) {
                logger.error("WorkerThread exception", ex);
            }
            serverIndex = (serverIndex + 1) % nServers;

            long diff = Instant.now().toEpochMilli()- startTime.toEpochMilli();
            if (diff > 5000) {
                dump(diff, null);
            }
        }
    } 
}
