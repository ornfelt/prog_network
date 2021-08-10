import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;   
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * This class sends and receives audio via sockets
 * @author Jonas Örnfelt
 *
 */

public class AudioClient {
	
	AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
	DatagramSocket socket;
	DatagramSocket otherSocket;
	ByteArrayOutputStream out;
	TargetDataLine microphone;
    SourceDataLine speakers;
    int numBytesRead;
    int CHUNK_SIZE;
    byte[] data;
    InetAddress address;
	
	public AudioClient(String server, int myPort, int otherPort) {
	    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
	    
	    try {
			/* variables for microphone and speaker */
		    microphone = AudioSystem.getTargetDataLine(format);
		    microphone = (TargetDataLine) AudioSystem.getLine(info);
		    microphone.open(format);
	
		    out = new ByteArrayOutputStream();
		    
		    CHUNK_SIZE = 1024;
		    data = new byte[microphone.getBufferSize() / 5];
		    microphone.start();
	
		    int bytesRead = 0;
		    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		    speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		    speakers.open(format);
		    speakers.start();
		    
		    address = InetAddress.getByName(server);
	        socket = new DatagramSocket(myPort);
	        otherSocket = new DatagramSocket();
	        
	        byte[] receiveData = new byte[1024];
	        byte[] sendData = new byte[1024];
	        
	    } catch (Exception e) { e.printStackTrace(); }
	    
	    //thread for sending audio
	    Thread send = new Thread (new Runnable () {
            String msg;
            @Override
             public void run() {
               while(true){
            	   try {
  	            	 byte[] buffer = new byte[1024];
  	            	 //while true
  	                 for(;;) {
  	                     numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
  	                     out.write(data, 0, numBytesRead); 
  	                     //use below for immediate playback
  	                     //speakers.write(data, 0, numBytesRead);            
  	                     DatagramPacket request = new DatagramPacket(data,numBytesRead, address, otherPort);
  	                     socket.send(request);
  	                 }
  	            	 }catch(Exception e) { e.printStackTrace(); }
               }
            }
        });
        send.start ();
	        
        	//thread for receiving audio
	        Thread receive = new Thread (new Runnable () {
	            String msg;
	            @Override
	            public void run () {
	            	try {
	                    while(true){
	                        byte[] buffer = new byte[1024];
	       	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
	       	            	socket.receive(response);

	       	            	out.write(response.getData(), 0, response.getData().length);
	       	            	speakers.write(response.getData(), 0, response.getData().length);
	       	            	String quote = new String(buffer, 0, response.getLength());
	       	            	//out.close();

	       	            	//you can print audio data with the prints below
	       	            	//System.out.println(quote);
	       	            	System.out.println();
	                    }
	                    //System.out.println("Server disconnected");
	                    //out.close();
	                    //socket.close();
	                    
	                  } catch (IOException e) {
	                      e.printStackTrace();
	                  }
	               }
	           });
	        receive.start ();
	}

	//main method that creates a new instance of client
	public static void main(String[] args) throws LineUnavailableException {
		int myPort = args.length > 0 ? Integer.parseInt(args[0]) : 5001;
		String ip = args.length > 1 ? args[1] : "127.0.0.1";
		int otherPort = args.length > 2 ? Integer.parseInt(args[2]) : 5000;
		
		System.out.print("Trying to establish connection with server: " + ip + ", my port: " + myPort);
		AudioClient client = new AudioClient(ip, myPort, otherPort);
		//play a sound when connected
		//playSound("click.wav");
	}
}