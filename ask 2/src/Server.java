//theodoros chalkidis csd4198


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.*;


public class Server {

    public static int CONNECTION_PORT = 0;
    public static ArrayList<Integer> SERVERS;
    public static int MY_PORT = 0;
    public static String HOST = "localhost";
    public static final int TIME_CHECK = 10000;
    public static HashMap<String, Integer> USER_DATA = new HashMap<String, Integer>();
    public static ServerSocket listenSocket ;
    public static int ValueOfRequestedKey = 0;
    public static int KeyFound = 0;

    public static void main(String[] argv) {

        StartingSequence();


        try{
            listenSocket = new ServerSocket(MY_PORT);
        }catch (Exception e){
            System.out.println("Couldn't open a socket , bye bye");
            return;
        }

        //if there was no last port you are the first in the array list
        if (CONNECTION_PORT ==0){
            SERVERS = new ArrayList<>();
            SERVERS.add(MY_PORT);
        }else{ //else you need to fetch the server list

            try{
                GetServerList();
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("The port you gave me doesnt exist, or the host is not able to talk to me exiting...");
                return;
            }
        }

        try {


            //creating health checking thread
            Thread checkNextServerThread = new checkNextServerThread();
            checkNextServerThread.start();

            while (true){
                Socket connectionSocket = listenSocket.accept();

                System.out.println("Someone connected in my port");

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                System.out.println("Deploying a thread to take the request");


                Thread requestHandler = new serverThread(connectionSocket, inFromClient, outToClient);


                requestHandler.start();




            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Port already used or something else went wrong please try again");

            return;
        }



    }

    //takes input from user to set up the server
    public static void StartingSequence(){
        Scanner myInput = new Scanner( System.in );
        System.out.println("Give me my HOST");
        HOST = myInput.nextLine();

        //reading my port
        System.out.println("Give me my port (please above 4000 and below 9999)");
        MY_PORT = myInput.nextInt();

        //error checking in my port
        while (MY_PORT < 4000 || MY_PORT > 9999){
            System.out.println("Give me a VALID port above 4000 and below 9999");
            MY_PORT = myInput.nextInt();
        }

        //reading the last port
        System.out.println("Give me a port to connect to, give 0 in case i am first");
        CONNECTION_PORT = myInput.nextInt();


        //printing info
        System.out.println("You entered MY_PORT " + MY_PORT);
        System.out.println("You entered LAST_PORT " + CONNECTION_PORT);
    }

    /*to get into the server group*/
    public static void GetServerList() throws Exception{
        System.out.println("Fetching the new arraylist of servers");
        SERVERS = new ArrayList<>();
        String sentence = "List "+MY_PORT;
        Socket clientSocket = new Socket(HOST, CONNECTION_PORT);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        outToServer.writeBytes(sentence + '\n');

        String response = inFromServer.readLine();
        if (response.equals("Denied")){
            System.out.println("I got denied my port already exists");
            clientSocket.close();
            return;
        }
        while((response)!=null){

            SERVERS.add(Integer.parseInt(response));
            response = inFromServer.readLine();
        }

        System.out.println("Got this array list :" + SERVERS);

        CONNECTION_PORT = 0; // i have to wait till the updated list comes to me

        clientSocket.close();
    }

    /*received a newbie into the group*/
    public static void AddNewServer(DataOutputStream outToClient, String portToAdd) throws Exception {

        //taking their port
        int nextPort = Integer.parseInt(portToAdd);
        System.out.println("Got a new server to add with port : "+ nextPort);

        if(SERVERS.contains(nextPort)){
            outToClient.writeBytes("Denied");
            System.out.println("Request denied because found duplicate port, resetting socket");
            return;
        }
        //adding it into the list
        SERVERS.add(nextPort);

        //returning the new list
        for (int port : SERVERS){

            outToClient.writeBytes(port+"\r\n");
        }
        System.out.println("Resseting socket");

    }

    /*got and updated server list*/
    public static void UpdateServerList(BufferedReader inFromClient, String size) throws Exception{
        System.out.println("Got a update on the server list");
        System.out.println("Clearing Servers list");
        SERVERS.clear();

        String readClient = inFromClient.readLine();
        StringTokenizer tokenizedLine = new StringTokenizer(readClient);


        for (int i = 0 ; i < Integer.parseInt(size) ; i++){
            String Port = tokenizedLine.nextToken(); //take the next token
            SERVERS.add(Integer.parseInt(Port)); //adding it to the servers
        }
        System.out.println("Updated arraylist : "+SERVERS);



        System.out.println("Resseting socket");
    }

    /*send the updated server list*/
    public static void SendUpdateServers(int StartedUpdateSequence) throws Exception{
        System.out.println("Preparing to send the new updated array list");
        StringBuilder sentence = new StringBuilder("UpdateList " + SERVERS.size() + " " + StartedUpdateSequence + "\n");

        if (SERVERS.size()==1){
            CONNECTION_PORT = 0; // no need to have a neighbour
            System.out.println("I am alone");
            return;
        }else{
            for (int port : SERVERS){
                if (port == MY_PORT) { // to find my connection port
                    int i = SERVERS.indexOf(port) + 1;
                    if (i >= SERVERS.size()){ // in case i am the last in the list
                        CONNECTION_PORT = SERVERS.get(0);
                    }else{
                        CONNECTION_PORT = SERVERS.get(i);
                    }
                    System.out.println("My new connection port is : " + CONNECTION_PORT);
                }
                sentence.append(" ").append(port);
            }
        }


        Socket clientSocket = new Socket(HOST, CONNECTION_PORT);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        outToServer.writeBytes(sentence.toString() + '\n');

        String response = inFromServer.readLine();
        while((response)!=null){
            response = inFromServer.readLine();
        }
        System.out.println("Resetting socket");
        clientSocket.close();


    }

    /*got a post from a client*/
    public static void UpdateUserData(String key , int value) throws Exception {
        System.out.println("Updating map with key = " + key + " and a value = "+ value);
        USER_DATA.put(key,value);
    }

    /*update USER_DATA after post*/
    public static void SendUpdateUserData(int StartedUpdateSequence , String key , int value) throws Exception {
        System.out.println("Preparing to send the new user data");
        StringBuilder sentence = new StringBuilder("UpdateMap " + StartedUpdateSequence + "\n" + key + " " + value);

        System.out.print("[ ");
        for (Map.Entry<String, Integer> me :
                USER_DATA.entrySet()) {

            // Printing keys
            System.out.print(me.getKey() + ":" + me.getValue() + " ");

        }
        System.out.print("]\n");

        System.out.println("Sending package to connection port");


        Socket clientSocket = new Socket(HOST, CONNECTION_PORT);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        outToServer.writeBytes(sentence.toString() + '\n');

        String response = inFromServer.readLine();
        while((response)!=null){
            response = inFromServer.readLine();
        }
        System.out.println("Resetting socket");
        clientSocket.close();
    }

    /*hand a Get from a client*/
    public static void GetFromClient(String key) throws Exception{
        System.out.println("Got a Get request from a client with a key of "+key);
        String value = "empty";

        //if the key exists
        if (USER_DATA.containsKey(key)){
            value = Integer.toString(USER_DATA.get(key));
            System.out.println("I got the value of this key : " + value);
        }else{
            System.out.println("I dont have the value of this key");
        }

        //preparing the sentence
        String sentence = "GetData "+ MY_PORT + " " + key + " " + value + "\n";

        Socket clientSocket = new Socket(HOST, CONNECTION_PORT);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        outToServer.writeBytes(sentence+ '\n');

        String response = inFromServer.readLine();
        while((response)!=null){
            response = inFromServer.readLine();
        }
        System.out.println("Resetting socket");
        clientSocket.close();

        // if key found changes we found the key and we can stop busy waiting
        while (KeyFound == 0);

    }

    /*internal search of Get*/
    public static void GetFromOtherServers(int WhoStartedTheSearch, String key , String ValueInRequest) throws Exception{
        String value = ValueInRequest;
        //if the key exists
        if (USER_DATA.containsKey(key)){
            value = Integer.toString(USER_DATA.get(key));
            System.out.println("I got the value of this key : " + value);
        }else{
            System.out.println("I dont have the value of this key");
        }

        System.out.println("Sending it to the next in the list");

        //preparing the sentence
        String sentence = "GetData "+ WhoStartedTheSearch + " " + key + " " + value + "\n";

        Socket clientSocket = new Socket(HOST, CONNECTION_PORT);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        outToServer.writeBytes(sentence+ '\n');

        String response = inFromServer.readLine();
        while((response)!=null){
            response = inFromServer.readLine();
        }
        System.out.println("Resetting socket");
        clientSocket.close();

    }
}
class serverThread extends Thread {
    final BufferedReader inFromClient;
    final DataOutputStream outToClient;
    final Socket connectionSocket;

    //constructor
    public serverThread(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient ) {
        this.inFromClient = inFromClient;
        this.outToClient = outToClient;
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        try{
            String requestMessageLine;
            requestMessageLine = inFromClient.readLine();
            StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);

            String type = tokenizedLine.nextToken();

            switch (type) {
                case "List":  //if someone requested the list of the servers

                    //add the new server in the list and tell it to the newbie
                    Server.AddNewServer(outToClient, tokenizedLine.nextToken());

                    //close the socket
                    connectionSocket.close();

                    //tell to the next one about the new updated list with initiator my port
                    Server.SendUpdateServers(Server.MY_PORT);
                    break;
                case "UpdateList": { //if someone sent a new updated list

                    // the size of the new list
                    String SizeOfUpdate = tokenizedLine.nextToken();

                    // who started the update sequence
                    int StartedUpdateSequence = Integer.parseInt(tokenizedLine.nextToken());


                    if (StartedUpdateSequence != Server.MY_PORT) { //if i didn't start the update

                        //update my server list
                        Server.UpdateServerList(inFromClient, SizeOfUpdate);

                        //close socket
                        connectionSocket.close();

                        //tell the new updated server list to the next one
                        Server.SendUpdateServers(StartedUpdateSequence);

                    } else {
                        System.out.println("I started this update so i reset the socket");
                        connectionSocket.close();
                    }
                    break;
                }
                case "YouLive":
                    String CheckerPort = tokenizedLine.nextToken();
                    System.out.println(CheckerPort+" checked on me, resseting socket");
                    connectionSocket.close();

                    break;
                case "Post": {
                    //taking the key and value
                    String key = tokenizedLine.nextToken();
                    int value = Integer.parseInt(tokenizedLine.nextToken());

                    System.out.println("Got a post with key: " + key + " and value : " + value);

                    //updating self
                    Server.UpdateUserData(key, value);

                    //closing the socket with the client
                    connectionSocket.close();

                    //sending an update to others
                    Server.SendUpdateUserData(Server.MY_PORT, key, value);


                    break;
                }
                case "UpdateMap": {
                    // who started the update sequence
                    int StartedUpdateSequence = Integer.parseInt(tokenizedLine.nextToken());

                    //going to the next line
                    requestMessageLine = inFromClient.readLine();
                    tokenizedLine = new StringTokenizer(requestMessageLine);

                    //taking the key and value
                    String key = tokenizedLine.nextToken();
                    int value = Integer.parseInt(tokenizedLine.nextToken());

                    if (StartedUpdateSequence != Server.MY_PORT) { //if i didn't start the update

                        //updating self
                        Server.UpdateUserData(key, value);

                        //closing the socket with the previous server
                        connectionSocket.close();

                        //sending an update to others
                        Server.SendUpdateUserData(StartedUpdateSequence, key, value);


                    } else {
                        System.out.println("I started this update so i reset the socket");
                        connectionSocket.close();
                    }
                    break;
                }
                case "Get": {
                    //get the key
                    String key = tokenizedLine.nextToken();

                    // this will start asking the next servers and busy wait till it has an answer
                    Server.GetFromClient(key);
                    if (Server.KeyFound == -1){
                        System.out.println("No one has a answer");
                        outToClient.writeBytes("NOT FOUND\n");
                    }else {
                        System.out.println("I got an answer");
                        outToClient.writeBytes(Server.ValueOfRequestedKey + "\n");
                    }
                    System.out.println("Resetting connection");
                    connectionSocket.close();
                    Server.KeyFound = 0 ;
                    Server.ValueOfRequestedKey = 0;
                    break;
                } case "GetData": {

                    // who started the update sequence
                    int StartedUpdateSequence = Integer.parseInt(tokenizedLine.nextToken());
                    String key = tokenizedLine.nextToken();
                    String value = tokenizedLine.nextToken();

                    //if I started this update
                    if (StartedUpdateSequence == Server.MY_PORT){
                        System.out.println("I started this search");
                        //if we didn't find the answer
                        if (value.equals("empty")){
                            System.out.println("The value was not found");
                            Server.KeyFound = -1 ;
                        }else{
                            System.out.println("The value was found : " + value);
                            Server.ValueOfRequestedKey = Integer.parseInt(value);
                            Server.KeyFound = 1;
                        }

                    }else{
                        // i didnt start the update
                        System.out.println(StartedUpdateSequence +" is searching for key " + key);
                        Server.GetFromOtherServers(StartedUpdateSequence, key ,value);

                    }
                    System.out.println("Resetting the socket");
                    connectionSocket.close();

                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Thread couldn't do the stuff it wanted , something went terribly wrong");
            return;
        }
    }


}

class checkNextServerThread extends Thread {
    @Override
    public void run()
    {
        while(true) {
            try{
                if (Server.CONNECTION_PORT!=0){ //if there is a neighbour check on him
                    System.out.println("Checking my neighbour");
                    Socket clientSocket = new Socket(Server.HOST, Server.CONNECTION_PORT);
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    outToServer.writeBytes("YouLive" +" "+ Server.MY_PORT+'\n');
                    clientSocket.close();
                }
                Thread.sleep(Server.TIME_CHECK);
            }catch (Exception e){
                Server.SERVERS.remove((Integer) Server.CONNECTION_PORT);
                try {
                    System.out.println("My neighbour is closed , initiating update sequence");
                    Server.SendUpdateServers(Server.MY_PORT);
                } catch (Exception ex) {
                    System.out.println("Something went wrong with sending an update");
                }
            }
        }
    }
}