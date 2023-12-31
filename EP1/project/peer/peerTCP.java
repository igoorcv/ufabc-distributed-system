import java.io.*;
import java.net.*;

public class peerTCP {

	public static void main (String args[]) throws Exception {
		// Throws Exception here because don't want to deal with errors in the rest of the code for simplicity. This isn't a good practice!

		// Connect to the server process running at localhost:9000
		Socket s = new Socket("localhost", 9000);
        
	    // The next 2 lines create a output stream we can write to SERVER.
		OutputStream os= s.getOutputStream();
		DataOutputStream serverWriter = new DataOutputStream(os);

		// The next 2 lines create a buffer reader that reads from the standard input. (to read stream FROM SERVER)
		InputStreamReader isrServer = new InputStreamReader(s.getInputStream());
		BufferedReader serverReader = new BufferedReader(isrServer);

        // Create buffer reader to read input from user. Read the user input to string 'sentence'.
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        String sentence;  
        sentence = inFromUser.readLine();

        // Keep repeating until an empty line is read.
		while (sentence.compareTo("") != 0) {

           // Send a user input to server.
           serverWriter.writeBytes(sentence +"\n");

		   // Server should convert to upper case and reply.Read server's reply below and output to screen.
           String response = serverReader.readLine();
		   System.out.println(response);

           //Read user input again.
           sentence = inFromUser.readLine();
        }
		// Send an empty line to server to end communication.
		serverWriter.writeBytes("\n");

		//Close the socket.
		s.close();
	}
}