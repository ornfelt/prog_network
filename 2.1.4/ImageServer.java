import java.net.*;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageServer extends Thread {
	
   private ServerSocket serverSocket;
   private Socket socket;
   private static int serverTimeout = 180000;

   public ImageServer(int port) throws IOException, SQLException, ClassNotFoundException, Exception {
          serverSocket = new ServerSocket(port);
          System.out.println("Opening server socket with port: " + port);
          serverSocket.setSoTimeout(serverTimeout);
   }

   public void run() {
       while(true) {
    	   //receive image from client
           try {
              socket = serverSocket.accept();

              BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(socket.getInputStream()));
              	
              System.out.println("Image received!");
              String receivedFileName = "index2.jpg";
              //full path might be needed 
              String myPath = "C:\\Users\\...\\";
              
              //rotate picture twice 180 degrees (if picture is upside down)
              //img = rotateCw(img);
              //img = rotateCw(img);
			  
              //create new image 
              ImageIO.write(img, "jpg", new File(receivedFileName));
              
              String message = "Do you want to open the received file? ";
              String title = "Open image?";
              
              //ask if user wants to open new image in JFrame
              int reply = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
              if (reply == JOptionPane.YES_OPTION) {
            	  //try to open image
            	  openPictureJFrame((receivedFileName));
              }
          }
         catch(SocketTimeoutException st){
        	 System.out.println("Socket timed out!");
             break;
         }
         catch(IOException e)
         {
              e.printStackTrace();
              break;
         }
         catch(Exception ex)
        {
              ex.printStackTrace();
        }
      }
   }
      
   //main method that starts server on new thread
   public static void main(String [] args) throws IOException, SQLException, ClassNotFoundException, Exception {
	   //get port and server from input if user has entered any values
	   int port = args.length > 0 ? Integer.parseInt(args[0]) : 6066;
	   String serverName = args.length > 1 ? args[1] : "localhost";
	   Thread t = new ImageServer(port);
	   t.start();
   }
   
   //rotate image 90 degrees
   public static BufferedImage rotateCw( BufferedImage img ) {
       int width  = img.getWidth();
       int height = img.getHeight();
       BufferedImage newImage = new BufferedImage( height, width, img.getType());

       for( int i=0 ; i < width ; i++ )
           for( int j=0 ; j < height ; j++ )
               newImage.setRGB( height-1-j, i, img.getRGB(i,j) );
       
       return newImage;
   }
   
   //open picture with JFrame
   public static void openPictureJFrame(String fileName) {
	   SwingUtilities.invokeLater(new Runnable() {
		  
		   @Override
		   public void run() {
			   JFrame frame = new JFrame(fileName); 
			   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			   BufferedImage img = null;
			   //attempt to open file
			   try {
				   img = ImageIO.read(new File(fileName));
			   }catch(Exception e ) { e.printStackTrace(); }
			   
			   //add image to img icon and jlabel
			   JLabel lbl = new JLabel();
			   lbl.setIcon(new ImageIcon(img));
			   frame.getContentPane().add(lbl, BorderLayout.CENTER);
			   //set size if you want
			   frame.setSize(1920, 1080);
			   frame.pack();
			   frame.setLocationRelativeTo(null);
			   frame.setVisible(true);
		   }
	   });
   }
}