//theodoros chalkidis csd4198
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;


public class Client {
    public static int PORT = 4000;
    public static String HOST = "localhost";
    public static void main(String[] argv) throws Exception
    {

        String sentence=    "Post " ;

        Scanner myInput = new Scanner( System.in );
        System.out.println("Give me an ip to connect to");
        HOST = myInput.nextLine();

        System.out.println("Give me a port to connect to");
        PORT = myInput.nextInt();

        System.out.println("Post or Get ?");
        String type = myInput.nextLine();

        while (!type.equals("Post") && !type.equals("Get")){
            System.out.println("Post or Get ?");
            type = myInput.nextLine();
        }

        System.out.println("Give me a key");
        String key = myInput.nextLine();
        while (key.contains(" ")){
            System.out.println("Key must not contain a whitespace, try again");
            key = myInput.nextLine();
        }

        sentence = type +  " " + key ;

        if (type.equals("Post")){
            System.out.println("Give me a value");
            int value = myInput.nextInt();
            sentence = sentence + " " + value ;
        }

        sentence = sentence+ " \n";



        Socket clientSocket = new Socket(HOST, PORT);

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));



        System.out.println("-------------------------SENDING-------------------------\n"+sentence);

        outToServer.writeBytes(sentence + '\n');

        System.out.println("\n---------------------SERVER RESPONSE---------------------");

        String modifiedSentence = inFromServer.readLine();
        while((modifiedSentence)!=null){
            System.out.println(modifiedSentence);
            modifiedSentence = inFromServer.readLine();
        }

        System.out.println("success");


        clientSocket.close();

    }
}
