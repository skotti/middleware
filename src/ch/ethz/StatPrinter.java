package ch.ethz;

import java.util.ArrayList;
import java.util.TimerTask;
import org.apache.log4j.Logger;



class StatPrinter extends TimerTask {
    ArrayList<WorkerThread> threads;

    final static Logger logger = Logger.getLogger(StatPrinter.class);

    ArrayList<WorkerStats> curStats;
    ArrayList<WorkerStats> prevStats;
    

    StatPrinter(ArrayList<WorkerThread> threads) {
        this.threads = threads;
        prevStats = new ArrayList<WorkerStats>();
        curStats = new ArrayList<WorkerStats>();
        for (int i = 0; i < this.threads.size(); i++) {
            prevStats.add(new WorkerStats(0, 0));
            curStats.add(new WorkerStats(0, 0));
        }
    }

    public void dumpAtShutdown() {

    }

    @Override
    public void run() {
        StringBuilder globalStats = new StringBuilder();
        for (int i = 0; i < this.threads.size(); i++) {
            curStats.set(i, this.threads.get(i).getStats());
        }
        for (int i = 0; i < this .threads.size(); i++) {
            WorkerStats cur = curStats.get(i);
            WorkerStats prev = prevStats.get(i);

            globalStats.append("\n").append(Long.toString(curStats.get(i).worker)).append(" ");
            globalStats.append(Long.toString(cur.timeInQueue - prev.timeInQueue)).append(" ");
            globalStats.append(Long.toString(cur.timeInParseAndSend - prev.timeInParseAndSend)).append(" ");
            globalStats.append(Long.toString(cur.timeInServer - prev.timeInServer)).append(" ");
            globalStats.append(Long.toString(cur.timeToProcessRequest - prev.timeToProcessRequest)).append(" ");
            globalStats.append(Long.toString(cur.timeToProcessRequestAndQueueTime - prev.timeToProcessRequestAndQueueTime)).append(" ");
            globalStats.append(Long.toString(cur.sizeOfQueue - prev.sizeOfQueue)).append(" ");
            globalStats.append(Long.toString(cur.requestsLeftQueue - prev.requestsLeftQueue)).append(" ");
            globalStats.append(Long.toString(cur.successfulRequests - prev.successfulRequests)).append(" ");
            for (int j = 0; j < cur.nServers; j++) {
                globalStats.append(Long.toString(cur.requestsPerServer[j] - prev.requestsPerServer[j])).append(" ");
            }
            //logger.debug(globalStats);
        }
	logger.debug(globalStats.toString()+"\n");
    }
}
