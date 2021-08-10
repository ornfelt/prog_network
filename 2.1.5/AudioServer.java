import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

public class AudioServer {
	//variables
	AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
	TargetDataLine microphone;
    SourceDataLine speakers;
    DatagramSocket socket;
    DatagramSocket otherSocket;
    ByteArrayOutputStream out;
    int numBytesRead;
    int CHUNK_SIZE;
    byte[] data;
    InetAddress address;
    
    public AudioServer(String server, int myPort, int otherPort) {
        
        try {
			/*variables for microphone and speaker */
            microphone = AudioSystem.getTargetDataLine(format);
            
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

            out = new ByteArrayOutputStream();
            
            CHUNK_SIZE = 1024;
            data = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            speakers.open(format);
            speakers.start();

            address = InetAddress.getByName(server);
            socket = new DatagramSocket(myPort);
            otherSocket = new DatagramSocket();

        } catch (Exception e) { e.printStackTrace(); } 
        
		//send thread that handles sending audio bytes
        Thread send = new Thread(new Runnable() {
	          String msg;
	          @Override
	          public void run() {
	             while(true){
	            	 try {
	            	 byte[] buffer = new byte[1024];
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
        send.start();
	       
		   //receive thread for receiving audio bytes
	       Thread receive= new Thread(new Runnable() {
	           String msg ;
	           @Override
	           public void run () {
	            	try {
	                    //msg = in.readLine();
	                    while(true){
	                       //msg = in.readLine();
	                       byte[] buffer = new byte[1024];
	       	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
	       	            	socket.receive(response);

	       	            	out.write(response.getData(), 0, response.getData().length);
	       	            	speakers.write(response.getData(), 0, response.getData().length);
	       	            	String quote = new String(buffer, 0, response.getLength());

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
	       receive.start();
    }

    //main method that takes incoming parameter as port
    public static void main(String[] args) throws IOException {
		int myPort = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
		String ip = args.length > 1 ? args[1] : "127.0.0.1";
		int otherPort = args.length > 2 ? Integer.parseInt(args[2]) : 5001;
		
		System.out.println("Trying to start server with port: " + myPort);
		AudioServer server = new AudioServer(ip, myPort, otherPort);
    }
}