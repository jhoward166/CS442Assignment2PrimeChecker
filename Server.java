import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class Server{
    private static ArrayList<Thread> threadsRunning = new ArrayList<Thread>();
    private static ArrayList<ClientConnection> clientsConnected = new ArrayList<ClientConnection>();
    private static Hashtable<String, ArrayList<Integer>> history = new Hashtable<String, ArrayList<Integer>>();
    private static int quitFlag = 0;
    public static class SocketListener extends Thread{
        private ServerSocket serverSocket;
        public SocketListener(ServerSocket sSocketIn){
            super("SocketListener");
            serverSocket = sSocketIn;
        }
        public void run(){ 
            while(!serverSocket.isClosed()){
                try{
                   ClientConnection clientHandler = new ClientConnection(serverSocket.accept());
                    Thread menuThread = new Thread(clientHandler);
                    synchronized(this){
                        threadsRunning.add(menuThread);
                        clientsConnected.add(clientHandler);
                    }
                    menuThread.start();
                }catch(IOException ioe){ 
                }
            }
        }
    }
    
    public static class ClientConnection extends Thread{
        private Socket clientSocket;
        private String clientName = "";
        public ClientConnection(Socket socketIn){
            super("ClientConnection");
            clientSocket = socketIn;
        }
        public void close(){
            try{
                clientSocket.close();
            }catch(IOException ioe){
            }
        }
        public void run(){
            String client;
            String userInput="";
            String clientSentence = "";
            String queryResponse;
            int[] primes= {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,67,71,73,79,83,89,97};
			try(
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
			){
				while(!clientSocket.isClosed()){
					//System.out.println("sup"); 
                    queryResponse = "";
                    clientSentence = inFromClient.readLine();
                    if(clientSentence != null){
                        String[] clientStringSplit = clientSentence.split(":");
                        if(clientStringSplit[1].trim().equals("")){
                            queryResponse = "No number sent";
							outToClient.println(queryResponse);
                        }else{
                            String[] numbersStringSplit = clientStringSplit[1].trim().split(" ");
							for(int i =0; i<numbersStringSplit.length; i++){
								queryResponse = "";
								int clientNumber = Integer.parseInt(numbersStringSplit[i]);
								if(clientStringSplit[0].trim().equals("")){
									queryResponse = "No client name found. Please enter a client name and try again.";
								}else if(clientName.equals(clientStringSplit[0].trim())){
									ArrayList<Integer> tempHistory = history.get(clientName);
									if(tempHistory != null){
										tempHistory.add(clientNumber);
									}else{
										tempHistory = new ArrayList<Integer>();
										tempHistory.add(clientNumber);
										history.put(clientName, tempHistory);
									}
								}else{							
									ArrayList<Integer> tempHistory = history.get(clientStringSplit[0].trim());
									if(tempHistory!=null){
										queryResponse = "Client name already in use. Please enter a different client name and try again.";
									}else{
										clientName = clientStringSplit[0].trim();
										tempHistory = new ArrayList<Integer>();
										tempHistory.add(clientNumber);
										history.put(clientName, tempHistory);
									}
								}
								if(queryResponse.equals("")){
									if(clientNumber%2 == 0 && clientNumber != 2){
										queryResponse = String.format("%d is not prime", clientNumber);
									}else if(clientNumber < 100){
										for(int j=0; j< primes.length; j++){
											if(clientNumber == primes[j]){
												queryResponse = String.format("%d is prime", clientNumber);
											}
										}
										if(queryResponse.equals("")){
											queryResponse = String.format("%d is not prime", clientNumber);
										}
									}else{
										queryResponse = String.format("Sorry, I'm not sure if %d is prime", clientNumber);
									}
								}
								outToClient.println(queryResponse);
							}
						}
                    }else{
						break;
                    }
				}
            }catch(IOException ioe){
            } 
			synchronized(this){
                for(int i=0; i<clientsConnected.size(); i++){
                    if(this == clientsConnected.get(i)){
                        clientsConnected.remove(i);
                        threadsRunning.remove(i);
                    }
                }
            }  
        }
    }

    public static void main(String argv[]) throws Exception{
        try(
            ServerSocket serverSocket = new ServerSocket(6789); 
            ){
        
            SocketListener listenForClients = new SocketListener(serverSocket); 
            Thread listenThread = new Thread(listenForClients);
            listenThread.start();
            while(true){
                String userInput = "";
                System.out.println("\nMenu:");
                System.out.println("[1] Print Connected Client Name");
                System.out.println("[2] Print Query History");
                System.out.println("[3] Quit");
                BufferedReader menuOption = new BufferedReader(new InputStreamReader(System.in));
            
                try{
                    userInput = menuOption.readLine();
                }catch(IOException ioe){
                }
				if(userInput.equals("1")){
					System.out.print("Enter name to lookup: ");
					BufferedReader nameLookupListener = new BufferedReader(new InputStreamReader(System.in));
					String nameLookup = nameLookupListener.readLine();
					ArrayList<Integer> userValues = history.get(nameLookup);
					if(userValues == null){
						System.out.println("Client name '"+nameLookup+"' was not found in history.");
					}else{
						System.out.print("Here are the values checked by "+nameLookup);
						System.out.println(userValues);
					}
				}else if(userInput.equals("2")){
					System.out.println("Printing the contents of history:");
					String historyOutput = history.toString();
					historyOutput = historyOutput.replace("{","");
					historyOutput = historyOutput.replace("}","");
					historyOutput = historyOutput.replace("], ","]\n");
					System.out.println(historyOutput+"\n");
                }else if(userInput.equals("3")){
                    quitFlag = 1;
                    for(int i=0; i<threadsRunning.size(); i++){
                        clientsConnected.get(i).close();
                        threadsRunning.get(i).join();
                    }
                    serverSocket.close();
                    listenThread.join();
                    System.exit(1);
                }
            }
        }catch(IOException ioe){ 
        }
    }
}
