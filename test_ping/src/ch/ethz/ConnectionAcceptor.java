package ch.ethz;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

public class ConnectionAcceptor extends Thread {
    // socket for the middleware
    ServerSocket serverSocket;
    // queue for adding new clients
    BlockingQueue<Socket> queue;
    // ip address of middleware
    InetAddress ip;
    // port on which middleware is running
    int port;
    
    public ConnectionAcceptor(BlockingQueue<Socket> queue, String ip, int port) throws UnknownHostException {
        
        this.queue = queue;
        this.ip = InetAddress.getByName(ip);  
        this.port = port;
    }
    
    @Override
    public void run() {
           
        try {
            serverSocket = new ServerSocket(port, 0, ip);
            while (true) {
                queue.put(serverSocket.accept());
            }
        } catch (IOException | InterruptedException ex) {
        }
        
    }
}
