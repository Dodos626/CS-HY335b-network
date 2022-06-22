package ask1;/*
 * Simple example ask1.TCPServer
 *
 * @author K&R
 */

//theodoros chalkidis csd4198
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String[] argv) throws Exception {
        String clientSentence="";
        String capitalizedSentence;
        int port = 6789;

        ServerSocket welcomeSocket = new ServerSocket(port);

        while (true) {


            System.out.println("Server ready on " + port);

            Socket connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient =
                    new BufferedReader(
                            new InputStreamReader(connectionSocket.getInputStream()));

            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());




            do {
                //response
                StringBuilder Token_builder = new StringBuilder();

                //what did the client said
                clientSentence = inFromClient.readLine();

                //split at \n
                String[] tokens = clientSentence.split("\\\\n");


                //builds response
                Token_builder.append("Server received [");
                for (String token : tokens){
                    Token_builder.append(token+",");
                }
                Token_builder.deleteCharAt(Token_builder.length()-1);
                Token_builder.append("]\n");

                System.out.println(Token_builder);

                //sends response
                outToClient.writeBytes(Token_builder.toString());

                //ends if client sentence ends with $
                if (clientSentence.endsWith("$")){

                    break;
                }





            }while (true);

            connectionSocket.getInputStream().close();
        }
    }
}