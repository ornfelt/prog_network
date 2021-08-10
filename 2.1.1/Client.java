import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This class tries to connect to incoming server and port parameters (or default values) Multiple clients of this can connect.
 * @author Jonas Örnfelt
 *
 */

public class Client {
	
	//static variables for default server and port
	private static String DEFAULT_SERVER = "127.0.0.1";
	private static int DEFAULT_PORT = 2000;

	public static void main(String args[]) throws IOException{
		String server = args.length > 0 ? args[0] : DEFAULT_SERVER;
		int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
		Socket socket = new Socket(server, port);
		
		ServerConnection serverConnection = new ServerConnection(socket);
		//for keyboard commands
		BufferedReader key = new BufferedReader(new InputStreamReader(System.in));
		//for output (with auto-flush)
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		
		//start serverconnection thread
		new Thread(serverConnection).start();
		System.out.println("Connected to server via address: " + socket.getInetAddress() + ", and port: " + socket.getPort());
		
		while(true) {
			System.out.println("");
			String command = key.readLine();
			
			//break out of while loop if user wants to quit
			if(command.equals("quit")) break;
			
			out.println(command);
			
		}
		socket.close();
		System.exit(0);
		}
}

//class for handling the server connection in separate thread
class ServerConnection implements Runnable {
	
	private Socket serverSocket;
	private BufferedReader in;
	
	public ServerConnection(Socket socket) throws IOException {
		serverSocket = socket;
		//for input 
		in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
	}

	@Override
	public void run() {
		try {
			while(true) {
				String serverResponse = in.readLine();
				//break if no response from server
				if(serverResponse == null) break;
				System.out.println("Server: " + serverResponse);
			}
		}catch (IOException e) { e.printStackTrace(); }
			finally {
				try {
					in.close();
				} catch (IOException e) { e.printStackTrace(); }
			}
	}
}
