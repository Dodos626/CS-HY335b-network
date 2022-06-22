package ask2;
/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross,
 * All Rights Reserved.
 **/

//theodoros chalkidis csd4198

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

class WebServer {
    public static int PORT = 4198;

    public static void main(String[] argv) throws Exception {

        String requestMessageLine;
        String fileName;

        ServerSocket listenSocket = new ServerSocket(PORT);

        while (true) {
            Socket connectionSocket = listenSocket.accept();

            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            requestMessageLine = inFromClient.readLine();

            StringTokenizer tokenizedLine =
                    new StringTokenizer(requestMessageLine);

            String type = tokenizedLine.nextToken();
            if (type.equals("GET")) {

                fileName = tokenizedLine.nextToken();


                if (fileName.startsWith("/"))
                    fileName = fileName.substring(1);


                System.out.println("Requested file name: " + fileName);

                String case_of_not_found_file_name = fileName;

                //changing the name to point in the right directory
                fileName =  fileName;

                //opening file
                File file = new File(fileName);

                //if file doesnt exist
                if (!file.exists()) {
                    outToClient.writeBytes("HTTP/1.1 404 Not Found\nFile:"+ case_of_not_found_file_name +" doesnt exist\r\n");
                    connectionSocket.close();
                    continue;
                }

                //measuring file's size
                int numOfBytes = (int) file.length();

                //puting file's contents into an array
                byte[] fileInBytes = fileToByteArray(file, numOfBytes, fileName);

                outToClient.writeBytes("HTTP/1.1 200 Document Follows\r\n");

                if (fileName.endsWith(".jpg")) {
                    outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                } else if (fileName.endsWith(".gif")) {
                    outToClient.writeBytes("Content-Type: image/gif\r\n");
                } else if (fileName.endsWith(".html")) {
                    outToClient.writeBytes("Content-Type: text/html\r\n");
                }

                outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                outToClient.writeBytes("\r\n");
		
                outToClient.write(fileInBytes, 0, numOfBytes);
		System.out.println("\nRequested file containts:");
		System.out.println(new String(fileInBytes)+"\n");
		outToClient.writeBytes("\n--------- end of transmission ---------\r\n");
		


            } else if (type.equals("PUT")) {
                System.out.println("caught put request");

                String response="HTTP/1.1 ";

                fileName = tokenizedLine.nextToken();


                if (fileName.startsWith("/"))
                    fileName = fileName.substring(1);


                System.out.println("Requested file name: " + fileName);

                //changing the name to point in the right directory
                String fileNameWithPath =  fileName;

                //opening file
                File file = new File(fileNameWithPath);

                //checking if it exists
                if (file.createNewFile()) {
                    System.out.println("File not found thus created");
                    response = response + "201 CREATED\n"+"Content-Location: "+fileNameWithPath;
                }else{
                    System.out.println("File already exists");
                    response = response + "200 OK\n"+"Content-Location: "+fileNameWithPath;


                    //creating file writer to open file that already exists
                    FileWriter myWriter = new FileWriter(fileNameWithPath,true);

                    //and create a new line at the end of the file
                    myWriter.write("\n");

                    //and close it, will open elsewhere
                    myWriter.close();
                }
                int body_size=0;

                //rest of the packet
                System.out.println("-----------REMAINING CLIENT PACKET TO PARSE-----------");
                while (true){
                    //read a line
                    requestMessageLine=inFromClient.readLine();
                    //print it
                    System.out.println(requestMessageLine);

                    //if we found the body size line
                    if (requestMessageLine.startsWith("Content-length:")){
                        //tokenize it
                        StringTokenizer contents = new StringTokenizer(requestMessageLine);

                        //this token is the Content-length
                        contents.nextToken();

                        //this is the actual size
                        body_size = Integer.parseInt(contents.nextToken());

                        //next line is empty and then is the body
                        requestMessageLine=inFromClient.readLine();
                        System.out.println("This is empty --> "+ requestMessageLine);

                        //this is the actual body
                        requestMessageLine=inFromClient.readLine();
                        System.out.println("<--ACTUAL BODY STARTS HERE-->\n"+ requestMessageLine);
                        break;
                    }
                }

                //creating a string builder
                StringBuilder put_in_file = new StringBuilder();


                do {
                    //appending the whole body into the string builder
                    put_in_file.append(requestMessageLine);

                    //reducing the remaining size of the body
                    body_size = body_size - requestMessageLine.length();

                    //if the body continues get next line to unload
                    if (body_size>0){
                        requestMessageLine = inFromClient.readLine();
                    }
                } while (body_size > 0);

                //creating file writer
                FileWriter myWriter = new FileWriter(fileNameWithPath,true);


                myWriter.write(put_in_file.toString());
                myWriter.close();
                System.out.println("wrote in file");


                outToClient.writeBytes(response+"\r\n");


            } else {
                System.out.println("Bad Request Message");
                outToClient.writeBytes("ERROR 400 BAD REQUEST\r\n");
            }
	    System.out.println("Resseting socket");
	    //connectionSocket.getOutputStream().close();	
            //connectionSocket.getInputStream().close();
	    connectionSocket.close();
        }
    }


    private static byte[] fileToByteArray(File file, int numOfBytes, String fileName) throws IOException {


        FileInputStream inFile = new FileInputStream(fileName);

        byte[] fileInBytes = new byte[numOfBytes];
        inFile.read(fileInBytes);
	inFile.close();
        return fileInBytes;
    }
}
