import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class launches a server on incoming port (or 2000) and then accepts multiple clients and will print messages to all of them. 
 * @author Jonas Örnfelt
 *
 */

public class Server {
	
	//static variables for default port
	private static int DEFAULT_PORT = 2000;
	//save clients in list
	public static ArrayList<ClientHandler> clients = new ArrayList<>();
	private static int maxClients = 20;
	//create thread pool for max amount of threads
	private static ExecutorService threadPool = Executors.newFixedThreadPool(maxClients);
	
	//main method that starts serversocket with incoming port or default port
	public static void main(String args[]) throws IOException{
		//set port to incoming port or default port
		int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
		//start serversocket
		ServerSocket serverSocket = new ServerSocket(port);
		
		while(true) {
			System.out.println("Waiting for client connection");
			Socket client = serverSocket.accept();
			InetAddress clientAddress = client.getInetAddress();
			int clientPort = client.getPort();
			ClientHandler newClient = new ClientHandler(client, clients);
			clients.add(newClient);
			threadPool.execute(newClient);
			System.out.println("Client connected with address: " + clientAddress + ", and port: " + clientPort);
			System.out.println("Amount of clients connected: " + clients.size() + " out of maximum: " + maxClients);
		}
	}
}

//class for handling all clients in "clients" list (runs in separate thread)
class ClientHandler implements Runnable {
	//the specific client
	private Socket client;
	//for output
	private PrintWriter out;
	//for input
	private BufferedReader in;
	//client threads
	private ArrayList<ClientHandler> clients;
	
	public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) throws IOException {
		this.client = socket;
		this.clients = clients;
		//for output (with auto-flush)
		out = new PrintWriter(client.getOutputStream(), true);
		//init input
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}

	@Override
	public void run() {
		//handle client requests
		try {
			while(true) {
				String msg = in.readLine();
				if(!msg.equals("quit")) {
					//out.println(msg);
					broadcastMessage(msg);
				}else {
					System.exit(0);
				}
			}
		} catch(IOException e) { e.printStackTrace(); }
		catch(NullPointerException e) {}
		finally {
			out.close();
			try {
				in.close();
				Server.clients.remove(this);
				System.out.println("Client disconnected. Amount of clients connected: " + Server.clients.size());
				Thread.currentThread().interrupt();
			} catch (IOException e) {e.printStackTrace(); }
		}
	}
	
	private void broadcastMessage(String msg) {
		for(ClientHandler ch : clients) {
			ch.out.println(msg);
		}
	}
}
