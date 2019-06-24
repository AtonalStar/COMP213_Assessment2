import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	//Initialization
	private int portNumber = 6666;
	private long startTime;
	private String welcome = "Welcome to ChatRoom. Please enter your username:";
	private String accepted = "The username is accepted.";
	private String[] commands = { "\\help", "\\quit","\\serverTime","\\clientTime","\\IPAddress","\\clientNumber"};
	private ServerSocket ss;
	private HashSet<String> clientNames = new HashSet<String>();
	private HashSet<PrintWriter> clientWriters = new HashSet<PrintWriter>();
	
	//Main(), the program entry
	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.start();	
	}
	
	//Define Server method start() to create connection, accept client's request, handle session thread and shutdown server finally.
	private void start() throws IOException{
		ss = new ServerSocket(portNumber);
		startTime = System.currentTimeMillis();
		Socket socket;
		Thread thread;
		System.out.println("Server "+InetAddress.getLocalHost()+" is waiting for connection...");
		try {
			while(true) {
				socket = ss.accept();
				thread = new Thread(new HandleSession(socket));
				thread.start(); //JVM automatically calls the run() method of this thread.
			}
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
		finally {
			shutDownServer();
		}
		
	}
	
	//try to close the server socket.
	private void shutDownServer() {
		try {
			ss.close();
			System.out.println("The server shuts down.");
		}
		catch(Exception e1) {
			System.out.println("Problems occur during shutting down of the server.");
			System.out.println(e1.getMessage());
		}
	}
	
	//A private inner class HandleSession to handle clients' input and output.
	private class HandleSession implements Runnable{
		private Socket socket = new Socket(); //Client socket
		private long clientStartTime; 
		String name; //Client username
		BufferedReader in = null;
		PrintWriter out = null;
		
		//Constructor
		public HandleSession(Socket socket) {
			this.socket = socket;
		}
		//A class implements Runnable interface must define the run method.
		public void run(){
			try {
				createStreams();
				getClientName();
				listenForMessage();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}finally {
				closeConnection();
			}
		}
		
		
		private void createStreams(){
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				clientWriters.add(out);
				clientStartTime = System.currentTimeMillis();
				System.out.println("One connection is successful.");
			}catch(IOException e) {
				System.out.println("Exception in creatStreams: " + e.getMessage());
			}
		}
		
		private void getClientName() {
			while(true) {
				out.print(welcome); 
				out.flush();
				try {
					name = in.readLine();
				}catch(IOException e) {
					System.out.println("Exception in getClientName: "+ e.getMessage());
				}
				//synchronized block to handle critical section so that no conflict will occur when two clients ask for a same username.
				synchronized(clientNames){
					if(name == null) return;
					if(!clientNames.contains(name)) {
						clientNames.add(name);
						break;
					}
					out.print("Sorry, this username is unavailable.");
					out.flush();
				}
			}
			out.print("Accepted! You can type your message now:");
			out.flush();
			System.out.println(name+ "has entered the chat room.");
		}
		
		private void listenForMessage() throws IOException{
			String line;
			while(in != null) { //when session stream has been created.
				line = in.readLine();
				if(line == null) break;
				if(line.startsWith("\\")) {
					if(!processClientRequest(line)) return;
				}else broadcast(name +": "+ line);
			}
		}
		
		private void closeConnection(){
			if(name != null) {
				clientNames.remove(name);
				broadcast(name+" has left the chat.");
			}
			if(out != null) {
				clientWriters.remove(out);
			}
			try {
				socket.close();
			}catch(IOException e) {
				System.out.println("Exception occurs when closing the socket.\n" + e.getMessage());
			}
		}
		
		private void broadcast(String message) {
			for(PrintWriter pw : clientWriters) {
				pw.print(message);
				pw.flush();
			}
			System.out.println(message);
		}
		
		private boolean processClientRequest(String cmd) {
			switch (cmd) {
				case "\\quit":
					return false;
				case "\\help":
					for(String c: commands) {
						if(c =="\\serverTime") out.println(c+ " >>>>>> Get the Server's running time.");
						else if(c =="\\clientTime") out.println(c + " >>> Get the current client's running time.");
						else if(c=="\\IPAddress") out.println(c + " >>> Get the Server's IP address.");
						else if(c=="\\clientNumber") out.println(c + " >>> Get the number of currently connected clients.");
						else if (c=="\\quit") out.println(c + " >>> Disconnect.");
						else out.println( "Command " + c ); 
					}
					out.flush();
					break;
				case "\\serverTime":
					out.println("The Server has run for "+ (System.currentTimeMillis()-startTime)/1000 + " seconds.");
					break;
				case "\\clientTime":
					out.println("This Client has run for "+ (System.currentTimeMillis()-clientStartTime)/1000 + " seconds.");
					break;
				case "IPAddress":
					out.println("The IP address of this Server is "+ socket.getLocalAddress().toString());
					break;
				case "clientNumber":
					out.println("The current number of clients in the Chat Room is "+ clientNames.size());
					break;
			}
			
			return true;
		}
		
	}//End of class HandleSession
	
}//End of class Server
