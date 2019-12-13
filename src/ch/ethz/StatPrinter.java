package ch.ethz;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TimerTask;
import org.apache.log4j.Logger;

class StatPrinter extends TimerTask {
    ArrayList<WorkerThread> threads;

    final static Logger logger = Logger.getLogger(StatPrinter.class);

    ArrayList<WorkerStats> curStats;
    ArrayList<WorkerStats> prevStats;

    Instant timeOfLastDump;

    StatPrinter(ArrayList<WorkerThread> threads) {
        this.threads = threads;
        prevStats = new ArrayList<WorkerStats>();
        curStats = new ArrayList<WorkerStats>();
        for (int i = 0; i < this.threads.size(); i++) {
            prevStats.add(new WorkerStats(0, 0));
            curStats.add(new WorkerStats(0, 0));
        }
        timeOfLastDump = Instant.now();
    }

    public void dumpAtShutdown(Instant time) {
        StringBuilder globalStats = new StringBuilder();

        for (int i = 0; i < this.threads.size(); i++) {
            this.threads.get(i).getStats().copy(curStats.get(i));
        }
        for (int i = 0; i < this.threads.size(); i++) {
            WorkerStats cur = curStats.get(i);
            WorkerStats prev = prevStats.get(i);

            globalStats.append("\n").append(curStats.get(i).worker).append(" ");
            globalStats.append(cur.timeInQueue - prev.timeInQueue).append(" ");
            globalStats.append(cur.timeInParseAndSend - prev.timeInParseAndSend).append(" ");
            globalStats.append(cur.timeInServer - prev.timeInServer).append(" ");
            globalStats.append(cur.timeToProcessRequest - prev.timeToProcessRequest).append(" ");
            globalStats.append(cur.timeToProcessRequestAndQueueTime - prev.timeToProcessRequestAndQueueTime).append(" ");
            globalStats.append(cur.sizeOfQueue - prev.sizeOfQueue).append(" ");
            globalStats.append(cur.requestsLeftQueue - prev.requestsLeftQueue).append(" ");
            globalStats.append(cur.successfulRequests - prev.successfulRequests).append(" ");
            for (int j = 0; j < cur.nServers; j++) {
                globalStats.append(cur.requestsPerServer[j] - prev.requestsPerServer[j]).append(" ");
            }
            globalStats.append(cur.cacheMisses - prev.cacheMisses).append(" ");
            if (cur.errors.length() != prev.errors.length()) {
                globalStats.append("\nERRORS:\n");
                globalStats.append(cur.errors.substring(cur.errors.length() - prev.errors.length()));
            }
            long timeElapsed = Duration.between(
                    timeOfLastDump, 
                    time).toMillis();
            globalStats.append(timeElapsed);
        }
        logger.debug(globalStats.toString()+"\n");
        for (int i = 0; i < this.threads.size(); i++) {
            try {
                prevStats.set(i, (WorkerStats)curStats.get(i).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        StringBuilder globalStats = new StringBuilder();

        for (int i = 0; i < this.threads.size(); i++) {
            this.threads.get(i).getStats().copy(curStats.get(i));
        }
        timeOfLastDump = Instant.now();
        for (int i = 0; i < this.threads.size(); i++) {
            WorkerStats cur = curStats.get(i);
            WorkerStats prev = prevStats.get(i);

            globalStats.append("\n").append(curStats.get(i).worker).append(" ");
            globalStats.append(cur.timeInQueue - prev.timeInQueue).append(" ");
            globalStats.append(cur.timeInParseAndSend - prev.timeInParseAndSend).append(" ");
            globalStats.append(cur.timeInServer - prev.timeInServer).append(" ");
            globalStats.append(cur.timeToProcessRequest - prev.timeToProcessRequest).append(" ");
            globalStats.append(cur.timeToProcessRequestAndQueueTime - prev.timeToProcessRequestAndQueueTime).append(" ");
            globalStats.append(cur.sizeOfQueue - prev.sizeOfQueue).append(" ");
            globalStats.append(cur.requestsLeftQueue - prev.requestsLeftQueue).append(" ");
            globalStats.append(cur.successfulRequests - prev.successfulRequests).append(" ");
            for (int j = 0; j < cur.nServers; j++) {
                globalStats.append(cur.requestsPerServer[j] - prev.requestsPerServer[j]).append(" ");
            }
            globalStats.append(cur.cacheMisses - prev.cacheMisses);
            if (cur.errors.length() != prev.errors.length()) {
                globalStats.append("\nERRORS:\n");
                globalStats.append(cur.errors.substring(cur.errors.length() - prev.errors.length()));
            }
            globalStats.append(" ").append(5000);
            
        }
        logger.debug(globalStats.toString()+"\n");
        for (int i = 0; i < this.threads.size(); i++) {
            try {
                prevStats.set(i, (WorkerStats)curStats.get(i).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
}
