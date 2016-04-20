import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Bhargav Srinivasan on 4/13/16.
 */
public class Server {

    public static class serverThread implements Runnable {

        private Socket clientSocket;
        String line = new String ();

        public serverThread(Socket clientSock){
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
                        if(clientSocket.isInputShutdown() || line.equals(null)){
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
                return;

            } catch (IOException e1){
                e1.printStackTrace();
            }

        }
    }

    public static void pollConnections(ArrayList<Thread> socketThreads){

        for (int i = 0; i < socketThreads.size(); i++) {
            System.out.println(socketThreads.get(i).getName() + " isAlive status: " + socketThreads.get(i).isAlive());
        }

    }

    public static void reclaimUnusedConnections(ArrayList<Thread> socketThreads, Integer connectionLimit){

        try {
            for (int i = 0; i < socketThreads.size(); i++) {
                socketThreads.get(i).join(10);
                if(!socketThreads.get(i).isAlive()){
                    socketThreads.remove(i);
                }
            }
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }

    }

    public static void acceptNewIncomingConnections(ArrayList<Thread> socketThreads,ServerSocket serverSocket, Integer connectionLimit){

        try {
            while (socketThreads.size() < connectionLimit) {
                Socket clientSocket = serverSocket.accept();   //A=> this is the blocking call which doesn't allow bulk reclamation
                socketThreads.add(new Thread(new serverThread(clientSocket)));
                socketThreads.get(socketThreads.size()-1).start();
            }
        } catch (IOException e4) {
            e4.printStackTrace();
        }

        return;

    }


    public static void main(String args[]) {

        Integer socketPort = new Integer(24002);
        Integer connectionLimit = new Integer(2);
        ArrayList<Thread> socketThreads = new ArrayList<>();
        boolean isAlive = true;
        boolean reclaimed = false;
        boolean maxedOut = false;
        Integer num = 1;


        try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
            System.out.println("Server has been started, press Ctrl+C to exit...");

            for (int i=0; i < connectionLimit; i++){
                Socket clientSocket = serverSocket.accept();
                socketThreads.add(new Thread(new serverThread(clientSocket)));
                //socketThreads.get(i).setDaemon(true);
                socketThreads.get(i).start();
            }

            maxedOut =true;
            reclaimed = false;


            while (isAlive){
                System.out.println("Iteration #"+num);
                pollConnections(socketThreads);
                if(maxedOut) {
                    reclaimUnusedConnections(socketThreads, connectionLimit);
                    if(socketThreads.size() < connectionLimit) {
                        reclaimed = true;
                    }
                }

                //if one of the threads disconnect and you reclaim one socket, it goes to acceptNew.
                //This means, if another one disconnects in the meanwhile and nobody connects, it will be reclaimed only
                //after the one reclaimed socket, now in, acceptNew has been accepted. => A

                //Need a blocking on accept status? Could possibly while loop reclaim

                if(reclaimed) {
                    // blocks until all of the reclaimed sockets have been used.
                    acceptNewIncomingConnections(socketThreads, serverSocket, connectionLimit);
                    if(socketThreads.size() == connectionLimit){
                        maxedOut = true;
                    }
                }

                Thread.sleep(1000);

                num++;
                if(num>20000){
                    num=1;
                }
            }


        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }

    }


}
