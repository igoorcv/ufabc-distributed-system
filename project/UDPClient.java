/*
Nome: Igor Carvalho de Oliveira
RA: 11201920763
 */

import java.io.*;
import java.net.*;

class UDPClient {
	public static void main(String args[]) throws Exception {

		//Infinity loop
		while(true) {

			//Create datagram socket
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");

			//Read a sentence from the console
			BufferedReader inFromUser =	new BufferedReader(new InputStreamReader(System.in));
			String sentence = inFromUser.readLine();
			int numSecondName = sentence.trim().substring(sentence.indexOf(" ") + 1).length(); //Count the characters of the second name

			//Allocate buffers
			byte[] sendData = new byte[sentence.length()];
			byte[] receiveData = new byte[numSecondName + 4]; //Buffer memory adjusted to receive the data standardize

			//Get the bytes of the sentence
			sendData = sentence.getBytes();

			//Send packet to the server
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9000);
			clientSocket.send(sendPacket);

			//Get the response from the server
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);

			//Print the received response
			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("RECEIVED FROM SERVER\n"
					+ "Submissions received by server has " + sentence.length() + " characters.\n"
					+ "The prefix 'RC_' will be included to standardize the data.\n "
					+ "\n"
					+ "Update: "+ modifiedSentence+" containing " + modifiedSentence.length() + " characters."
					+ "\n");

			//Close the socket
			clientSocket.close();

		}
	}
}