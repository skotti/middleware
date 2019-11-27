package ch.ethz;

class WorkerStats implements Cloneable {
     //thread number
     int worker;
     // requests, that returned from the server with positive answer
     int successfulRequests;
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
     
     int[] requestsPerServer;

     int nServers = 0;

     StringBuilder errorString;

     public WorkerStats(int worker, int nServers) {

	     this.worker = worker;
          successfulRequests = 0;
          timeInQueue = 0;
          sizeOfQueue = 0;
          requestsLeftQueue = 0;
          timeInServer = 0;
          timeInParseAndSend = 0;
          timeToProcessRequest = 0;
          timeToProcessRequestAndQueueTime = 0;
          cacheMisses = 0;
          errors = new StringBuilder();

          requestsPerServer = new int[3];
          for (int j = 0; j < nServers; j++) {
               requestsPerServer[j] = 0;
          }

          this.nServers = nServers;
     }

     @Override
     protected Object clone() throws CloneNotSupportedException {
          WorkerStats cloned;
          cloned = (WorkerStats) super.clone();
          cloned.requestsPerServer = cloned.requestsPerServer.clone();
          return cloned;
    }

     public void copy(WorkerStats other) {
          synchronized(this) {
               other.worker = this.worker;
               other.successfulRequests = this.successfulRequests;
               other.timeInQueue = this.timeInQueue;
               other.sizeOfQueue = this.sizeOfQueue;
               other.requestsLeftQueue = this.requestsLeftQueue;
               other.timeInServer = this.timeInServer;
               other.timeInParseAndSend = this.timeInParseAndSend;
               other.timeToProcessRequest = this.timeToProcessRequest;
               other.timeToProcessRequestAndQueueTime = this.timeToProcessRequestAndQueueTime;

               other.nServers = this.nServers;
               other.requestsPerServer = new int[3];
               for (int j = 0; j < nServers; j++) {
                    other.requestsPerServer[j] = this.requestsPerServer[j];
               }

               other.cacheMisses = this.cacheMisses;
               other.errors.setLength(0);
               other.errors.append(this.errors);
          }
     }
}
