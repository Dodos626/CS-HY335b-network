
import java.io.*;
import java.net.*;
import java.util.Objects;

//theodoros chalkidis csd4198
public class TCPClient {
    public static int PORT = 4198;
    public static void main(String[] argv) throws Exception
    {

        String get_sentence=    "GET /index.html HTTP/1.1\n" +
                            "Host: localhost\n" +
                            "Accept-Language: en-us" ;


        String sentence ;

        String modifiedSentence;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("147.52.19.54", PORT);

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));

        System.out.println("Write GET or PUT other words will be treated as an automated GET request of the /index.html");
        String type_of_req = inFromUser.readLine();

        if(type_of_req.equals("PUT")){
            System.out.println("enter file name with a / in the start");
            String file_str = inFromUser.readLine();
            System.out.println("enter content");
            String content = inFromUser.readLine();
            sentence = "PUT "+file_str+" HTTP/1.1\n" +
                    "Content-type: text/html\n" +
                    "Content-length: "+content.length()+"\n" +
                    "\n"+
                    content;

        }else if(type_of_req.equals("GET")){
            System.out.println("enter file name with a / in the start");
            String file_str = inFromUser.readLine();
            sentence = "GET "+file_str+" HTTP/1.1\n" +
                    "Host: localhost\n" +
                    "Accept-Language: en-us" ;
        }else{
            sentence = get_sentence;
        }
        System.out.println("-------------------------SENDING-------------------------\n"+sentence);

        outToServer.writeBytes(sentence + '\n');
	
        System.out.println("\n---------------------SERVER RESPONSE---------------------");
        modifiedSentence = inFromServer.readLine();
       	while((modifiedSentence)!=null){ 
            System.out.println(modifiedSentence);
	    modifiedSentence = inFromServer.readLine();
        }




        clientSocket.close();

    }
}
