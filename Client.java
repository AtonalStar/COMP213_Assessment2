
import java.io.*;
import java.net.*;

/**
 * The Client class is a Client program.<br>
 * It provides a private constructor of Client with private member fields integer portNumber, String welcome, String accepted, Socket socket, BufferedReader in, PrintWriter out, boolean isAllowedToChat, boolean isServerConnected and String clientName.
 * The method provided are a main method, a start(), an establishConnection(), a handleProfileSetUp(), a handleOutgoingMessages(), a getClientInput(), a handleIncomingMessages() and a closeConnection().
 * @author LZW
 *
 */
public class Client {
	private int portNumber = 6666;
	private String welcome = "Please set your prefered username: ";
	private String accepted = "Your username is accepted.";
	
	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;
	private boolean isAllowedToChat = false;
	private boolean isServerConnected = false;
	private String clientName;
	
	/**
	 The main method of the class Client includes the instantiation of a Client and the calling of method start().
	 * @param args
	 * an array that can be used to pass String arguments to the program when it is started up.
	 * @throws Exception
	 * Handles the exception when error occurs.
	 */
	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.start();
	}
	
	/**
	 * The start of a Client includes the establishment of a connection between a Client socket and a Server socket (establishConnection()), the handling of the outgoing messages which creates outgoing messages thread (handleOutgoingMessages()) as well as the handling of incoming messages, which creates thread for incoming messages from the Server (handleIncomingMessages()). 
	 */
	public void start() {
		establishConnection();
		handleOutgoingMessages();
		handleIncomingMessages();
	}
	
	/**
	 * Establishes the connection between the Client and the Server. <br>
	 * In order to connect to the Server created by the Server class, the initial portNumber has been set to the same value with that Server, after the Client read the IP address of the local host from the user input, the Client socket will then establish the connection, then get the username by handleProfileSetUp(). If not, the Client will throw IOException
	 */
	private void establishConnection() {
			String serverAddress = getClientInput( "What is the address of the server that you wish to connect to?" );
			try {
				socket = new Socket( serverAddress, portNumber );
				in = new BufferedReader( new InputStreamReader( socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				isServerConnected = true;
			} 
			catch (IOException e) {
				System.err.println( "Exception in handleConnection(): " + e );
			}
			handleProfileSetUp();
		} // end of handleConnection()
	
	/**
	 * After establish the connection, the Client will ask the user to set the username and if allowed to chat (not a duplicated username), the Client will output hint to the user (To type message or "\help"). 
	 */
	private void handleProfileSetUp() {
		String line = null;
		while ( ! isAllowedToChat ) {
			try { line = in.readLine(); }
			catch (IOException e) {
				System.err.println( "Exception in handleProfileSetUp:" + e );
			}
			if ( line.startsWith( welcome ) ) {
				out.println( getClientInput( welcome ) );
			} 
			else if (line.startsWith( accepted ) ) {
				isAllowedToChat = true;
				System.out.println( accepted +" You can type messages." );
				System.out.println( "To see a list of commands, type \\help." );
			}
			else System.out.println( line );
		}
	}	// end of handleProfileSetUp()	

	/**
	 * Creates new Thread to get client input and send out.<br>
	 * An anonymous inner class is used when creating the Thread senderThread.<br>
	 * "Thread senderThread = new Thread (new Runnable(){run(){}})" is equal to <br> 
	 * "class Anonymous() implements Runnable{ run(){}} <br>
	 *  Thread senderThread = new Thread (new Anonymous())".<br> 
	 *  When a thread calls its start() method, the JVM will call its run() method automatically.
	 */
	private void handleOutgoingMessages() { //Sender thread
		Thread senderThread = new Thread( new Runnable(){
			public void run() {
				while ( isServerConnected  ){
					out.println( getClientInput( null ) );
				}
			}
		});
		senderThread.start();
	} // end of handleOutgoingMessages()

	/**
	 * Gets the client input using String hint.<br>
	 * When there is hint to instruct the user to input something, the client get the user inputs, and return the input message.
	 * @param hint
	 * Client instruction for user to enter the right message.
	 * @return String message
	 * - the message that user entered.
	 */
	private String getClientInput (String hint) {
		String message = null;
		try {
			BufferedReader reader = new BufferedReader(
				new InputStreamReader( System.in ) );
			if ( hint != null ) { System.out.println( hint ); }
			message = reader.readLine();
			if ( ! isAllowedToChat ) { clientName = message; }
		}
		catch (IOException e) {
			System.err.println( "Exception in getClientInput(): " + e );
		}
		return message;
	} // end of getClientInput()

	/**
	 * Creates new Thread for incoming message from the Server.<br>
	 * An anonymous inner class is used when creating the Thread listenerThread.<br>
	 * @see handleOutgoingMessages()
	 */
	private void handleIncomingMessages() { // Listener thread
		Thread listenerThread = new Thread( new Runnable() {
			public void run() {
				while ( isServerConnected ) {
					String line = null;
					try {
						line = in.readLine();
						if ( line == null ) {
							isServerConnected = false;
							System.err.println( "Disconnected from the server" );
							closeConnection();
							break;
						}
						System.out.println( line );
					}
					catch (IOException e) {
						isServerConnected = false;
						System.err.println( "The Server has disconnected." );
						break;
					}
				}
			}
		});
		listenerThread.start();			
	} // end of handleIncomingMessages() 

	/**
	 * Closes the connection between the current Client and the Server.
	 */
	void closeConnection() {
		try { 
			socket.close(); 
			System.exit(0); // finish the client program
		} 
		catch (IOException e) {
			System.err.println( "Exception when closing the socket" );						
			System.err.println( e.getMessage() );
		}
	} // end of closeConnection()
		
} // end of the class Client