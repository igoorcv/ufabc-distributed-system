import java.io.*;
import java.net.*;
//import java.util.ArrayList;
//import java.util.List;

public class serverTCP {
    
    public static void main (String args[]) throws Exception {
		// Throws Exception here because don't want to deal with errors in the rest of the code for simplicity. This isn't a good practice!

         // Welcome socket  ---- SOCKET 1 --- Waits for a new connection. Accepts connection from multiple peers.
		 ServerSocket serverSocket = new ServerSocket(9000);
		 System.out.println("Servidor Centralizado iniciado. Aguardando conexões na porta 9000.");

		 while (true) {
			 
			// Connection socket  --- SOCKET 2
			 Socket s = serverSocket.accept();
			 System.out.println("Connection established from " + s.getInetAddress());
			 
			 // Create a BufferedReader object to read strings from the socket. (read strings FROM CLIENT)
			 BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			 String input = br.readLine();
			 
			 // Create output stream to write to/send TO CLIENT.
             DataOutputStream output = new DataOutputStream(s.getOutputStream());
			 
			 Socket clientSocket = serverSocket.accept();
			 System.out.println("Conexão estabelecida com um peer: " + clientSocket.getInetAddress());
			 
			 // Keep repeating until an empty line is read. --> REGRA DE neógcios
			 while (input.compareTo("") != 0) {

				 // Convert input to upper case and echo back to client.
				 output.writeBytes(input.toUpperCase() + "\n");
				 input = br.readLine();
			}
			// Close current connection.
			s.close();
		 }
    }
}

/*
class UDPServer {

	public static void main(String args[]) throws Exception {
		//BlackList
		List<String> blacklist = new ArrayList<String>();
		blacklist.add("192.168.0.1");
		blacklist.add("127.0.0.1");
		
		//Create server socket
		DatagramSocket serverSocket = new DatagramSocket(9000);
		
		while(true) {
			byte[] receiveData = new byte[1024];
			
			//block until packet is sent by client
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivedPacket);
			
			//Get the information about the datagram of the client
			InetAddress IPAddress = receivedPacket.getAddress();
			int port = receivedPacket.getPort();
			
			//Check if the IP is contained in BlackList
			if (blacklist.contains(IPAddress.getHostAddress())) {
				System.out.println("Pacote descartado - endereço IP na lista negra: " + IPAddress.getHostAddress());
				continue;
			}
			
			//Get the data of the packet
			String receivedSentence = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
			String sentenceUp = new String(receivedSentence.toUpperCase());
			char letterInitial = sentenceUp.charAt(0);
			
			//Create method to response the UDPClient
			class responseUpdated {
				public static void method(String sentence, DatagramSocket serverSocket, InetAddress IPAddress, int port) throws Exception{
					
					//Change the data to capital letters
					String capitalizedSentence = sentence;
					byte[] sendData = new byte[sentence.length()];
					sendData = capitalizedSentence.getBytes();
					
					//Send back the response to the client
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				}
			}
			
			//Create logic to build default username
			String[] sentenceSplit = sentenceUp.split(" ");
			if (sentenceSplit.length >= 2) {
			    String secondName = sentenceSplit[1];
			    String sentence = new String("RC_" + letterInitial + secondName);
			    System.out.println("RECEIVED FROM CLIENT " + IPAddress.getHostAddress() + ":\n"
			    		+ "Default login = " + sentence
			    		+ "\n");
			    responseUpdated.method(sentence, serverSocket, IPAddress, port); //Call method to response the UDPCliente
			    
			} else {
				String firstName = sentenceSplit[0];
				String sentence = new String("RC_" + letterInitial + firstName);
				System.out.println("RECEIVED FROM CLIENT " + IPAddress.getHostAddress() + ":\n"
						+ "Default login = " + sentence
						+ "\n");
				responseUpdated.method(sentence, serverSocket, IPAddress, port); //Call method to response the UDPCliente
			}			
		}
	}
}
*/