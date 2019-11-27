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
    ArrayList<Connection> connections;
    
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
    
    WorkerStats stats;

    final static Logger logger = Logger.getLogger(WorkerThread.class);
    
    public WorkerThread(int i, LinkedBlockingQueue<QueueStructure> taskQueue,
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

        /* connect to the servers*/
        connections = new ArrayList<>();

        for (int j = 0; j < nServers; j++) {
            Socket s = new Socket(ipArray.get(j), portArray.get(j));
            Connection con = 
                                    new Connection(s,
                                    new BufferedReader(new InputStreamReader(s.getInputStream())), 
                                    new PrintWriter(s.getOutputStream(), true),
                                    new char[256]);
            connections.add(con);
        }
        // thread id
        threadNumber = i;
        // start sending to server 0
        serverIndex = 0;
        
        stats = new WorkerStats(threadNumber, nServers);
    }
    
    public WorkerStats getStats() {
        return this.stats;
    }
    
    public void sendResponse(String answerString, OutputStream stream) {
        PrintWriter outputClient = new PrintWriter(stream, true);
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
                outputClient.flush();
                continue;
            }

            //-------------------------------------collect stats
            long timeElapsed = Duration.between(
                    st.enqueueTime, 
                    dequeueTime).toNanos();

            // send request to one of the server in round robin manner
            // for correct answer to get request we will have three parts
            StringBuilder answerString = new StringBuilder();
            String part1, part3;
            //String part1, part3;

            Instant serverProcessStart = Instant.now();
            connections.get(serverIndex).writer.write(st.request);
            connections.get(serverIndex).writer.flush();
            
            //-------------------------------------
            Instant endParseAndSend = Instant.now();//-------------------------------------------------------------------------end POINT2, time parse and send
            //Instant serverProcessStart = endParseAndSend;//--------------------------------------------------------------------start POINT3, server processing
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
                        stats.cacheMisses += 1;
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
                Instant serverProcessEnd = Instant.now();
		        sendResponse(answerString.toString(),
                             st.connection.getOutputStream());

                Instant workerProcessEnd = Instant.now();

                // collect all stats------------------------------------------------------------
                synchronized(stats) {
                    stats.requestsPerServer[serverIndex] += 1;
                    stats.timeInParseAndSend += Duration.between(
                            startParseAndSend, 
                            endParseAndSend).toNanos();
                    
                    stats.timeInQueue += timeElapsed;
                    stats.requestsLeftQueue += 1;
                    stats.sizeOfQueue += sizeOfCurrentQueue;
                    stats.timeToProcessRequest += Duration.between(
                                        dequeueTime, 
                                        workerProcessEnd).toNanos();
                    stats.timeToProcessRequestAndQueueTime += Duration.between(
                                                        st.enqueueTime, 
                                                        workerProcessEnd).toNanos();
                    stats.timeInServer += Duration.between(
                                            serverProcessStart,
                                            serverProcessEnd).toNanos();
                    stats.successfulRequests += 1;
                }
                //----------------------------------------
            } catch (IOException ex) {
                logger.error("WorkerThread exception", ex);
            }
            serverIndex = (serverIndex + 1) % nServers;
        }
    } 
}
