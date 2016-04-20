import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static java.lang.System.exit;

/**
 * Created by Bhargav Srinivasan on 4/13/16.
 */
public class Client {

    public static void main(String args[]){

        Integer clientPort = new Integer(24002);
        String userInput = new String();

        try{
            Socket clientSocket = new Socket();
            InetAddress serverAddr = InetAddress.getByName(null);
            SocketAddress serverSockAddr = new InetSocketAddress(serverAddr,clientPort);
            try {
                clientSocket.connect(serverSockAddr,1000);
            } catch (IOException e1) {
                e1.printStackTrace();
            }



            BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            PrintStream os = new PrintStream(clientSocket.getOutputStream());


            System.out.print("Client:    ");

            while ((userInput = stdIn.readLine()) != null){
                if(userInput.equals("EXIT")){
                    clientSocket.close();
                    exit(0);
                }
                os.println(userInput);
                System.out.println("Echo: "+ is.readLine());
                System.out.print("Client:    ");

            }

            clientSocket.close();

        } catch (IOException e){
            e.printStackTrace();
        }


    }
}
