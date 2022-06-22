/*
 * Simple example ask1.TCPClient
 *
 * @author K&R
 */

//theodoros chalkidis csd4198
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPClient {

    public static void main(String[] argv) throws Exception {
        String sentence="";
        String modifiedSentence = null;

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("147.52.19.54", 4198);

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));




        do{
            //if we sent the last packet we dont need to send another one
            if (!sentence.endsWith("$")){
                //reads user packet
                sentence = inFromUser.readLine();
                //sends it
                outToServer.writeBytes(sentence + '\n');
            }


            //reads the response
            modifiedSentence=inFromServer.readLine();

            //if client closed the socket
            if (modifiedSentence==null){
                System.out.println("SERVER CLOSED SOCKET");
                break;
            }

            //prints it
            System.out.println("FROM SERVER: " + modifiedSentence);



        }while (true);




        clientSocket.close();

    }
} 
