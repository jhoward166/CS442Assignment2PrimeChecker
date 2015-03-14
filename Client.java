import java.io.*;
import java.net.*;

public class Client{
    public static void main(String argv[]) throws IOException{
        String hostName = "localhost";
        int portNum = 6789;
        String input = "0";
        String clientName = "";
        String queryNumber = "";
        String sentence;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        try{
            Socket clientSocket = new Socket(hostName, portNum);
            while(!input.equals("4")){
                //System.out.println(clientSocket.isClosed());
                //System.out.println(clientSocket);
                System.out.println("\nMenu:");
                System.out.println("[1] Set Client Name");
                System.out.println("[2] Enter a number or numbers to query for prime");
                System.out.println("[3] Send query to server");
                System.out.println("[4] Quit");
                BufferedReader menuOption = new BufferedReader(new InputStreamReader(System.in));
                input = menuOption.readLine();
                if(input.equals("1")){
                    System.out.printf("Enter a new client name: ");
                    BufferedReader setClientName = new BufferedReader(new InputStreamReader(System.in));
                    clientName = setClientName.readLine();
                }else if(input.equals("2")){
                    System.out.printf("Enter an integer for the query: ");
                    BufferedReader setQueryNumber = new BufferedReader(new InputStreamReader(System.in));
                    queryNumber = setQueryNumber.readLine();
                }else if(input.equals("3")){
                    PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                    String numbersToServer[] = queryNumber.split(" ");	
					BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					outToServer.println(clientName+": "+queryNumber);
					for(int i =0; i<numbersToServer.length; i++){
						modifiedSentence = inFromServer.readLine();
						if(modifiedSentence == null){
							System.out.println("Connection to the server lost.\nExiting program.");
							System.exit(1);
						}
						System.out.println("FROM SERVER: " + modifiedSentence);
					}
                }else if(! input.equals("4")){
                    System.out.println("Input not recognized.");
                }
            }
        }catch(UnknownHostException uhe){
            System.err.println(uhe.getMessage());
            System.exit(1);
        }catch(IOException ioe){
            System.err.println(ioe.getMessage());
            System.exit(1);
        }
        System.out.println("Good Bye!");
        //clientSocket.close();
    }
}
