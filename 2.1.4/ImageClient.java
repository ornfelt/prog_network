import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class ImageClient
{

	//main method that starts socket and tries to connect
   public static void main(String [] args) {
	   //get port and server from input if user has entered any values
	   int port = args.length > 0 ? Integer.parseInt(args[0]) : 6066;
	   String serverName = args.length > 1 ? args[1] : "localhost";
	   
	   //init variables used in try
	   Image newimg;
	   BufferedImage bimg;
	   byte[] bytes;
	   
	   //send image to server
	   try{
	         System.out.println("Connecting to " + serverName + " on port " + port);
	         Socket client = new Socket(serverName, port);
	         System.out.println("Just connected to " + client.getRemoteSocketAddress());
	
	         bimg = ImageIO.read(new File("index.jpg"));
	         ImageIO.write(bimg,"jpg",client.getOutputStream());
	         System.out.println("Image sent!");
	         client.close();
         
      }catch(IOException e){ e.printStackTrace(); }
   }
}