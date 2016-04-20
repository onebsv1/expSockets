import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Bhargav Srinivasan on 4/13/16.
 */
public class Server {

    private static class serverThread implements Runnable {

        private Socket clientSocket;
        String line;

        serverThread(Socket clientSock){
            this.clientSocket = clientSock;
        }


        @Override
        public void run() {

            try {
                BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintStream os = new PrintStream(clientSocket.getOutputStream());

                while (clientSocket.isConnected()) {
                    try {
                        line = is.readLine();
                        os.println("From Server: " + line);
                        if(clientSocket.isInputShutdown() || line.isEmpty()){
                            Thread.sleep(100);
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (NullPointerException n){
                        clientSocket.close();
                        return;
                    }

                }

                clientSocket.close();

            } catch (IOException e1){
                e1.printStackTrace();
            }

        }
    }

    private static void pollConnections(ConcurrentLinkedDeque<Thread> socketThreads){

        for (Thread sockThread : socketThreads) {
            System.out.println(sockThread.getName() + " isAlive status: " + sockThread.isAlive());
        }

    }

    private static void reclaimUnusedConnections(ConcurrentLinkedDeque<Thread> socketThreads){

        try {
            for (Thread sockThread: socketThreads) {
                sockThread.join(10);
                if(!sockThread.isAlive()){
                    socketThreads.remove(sockThread);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void acceptNewIncomingConnections(ConcurrentLinkedDeque<Thread> socketThreads,ServerSocket serverSocket, Integer connectionLimit){

        try {
            while (socketThreads.size() < connectionLimit) {
                Socket clientSocket = serverSocket.accept();
                socketThreads.add(new Thread(new serverThread(clientSocket)));
                socketThreads.getLast().start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[]) {

        Integer socketPort = 24002;
        Integer connectionLimit = 3;
        Integer serverIteration = 1;
        ConcurrentLinkedDeque<Thread> socketThreads = new ConcurrentLinkedDeque<>();
        boolean reclaimed;


        try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
            System.out.println("Server has been started, press Ctrl+C to exit...");

            for (int i=0; i < connectionLimit; i++){
                Socket clientSocket = serverSocket.accept();
                socketThreads.add(new Thread(new serverThread(clientSocket)));
                socketThreads.getLast().start();
            }


            while (true){
                System.out.println("Iteration #"+serverIteration);

                pollConnections(socketThreads);
                reclaimUnusedConnections(socketThreads);
                reclaimed = (socketThreads.size() < connectionLimit);

                if(reclaimed) {
                    // blocks until all of the reclaimed sockets have been used.
                    acceptNewIncomingConnections(socketThreads, serverSocket, connectionLimit);
                }

                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }

                serverIteration = (serverIteration>20000)?1:serverIteration+1;
            }


        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }


}
