import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;

/**
 * This class implements a GUI where the user can draw freely, which then is distributed to other users of the same program. 
 * Try it out by running this class twice with, for example: "java Draw 2000 localhost 2001" and "java Draw 2001 localhost 2000"
 * @author Jonas Örnfelt
 *
 */

public class Draw extends JFrame {

  //main method that starts connection to port and server
  public static void main(String[] args) throws IOException {
	  //get port, server and remoteport from user input or set to default 
	  int myPort = Integer.parseInt(args[0]);
	  String remoteHost = args.length > 0 ? args[1] : "localhost";
	  int remotePort = Integer.parseInt(args[2]);
	  new Draw(myPort, remoteHost, remotePort);
	  
  }

  //constructor that displays Paper (JPanel)
  public Draw(int myPort, String remoteHost, int remotePort) {
	Paper p = new Paper(myPort, remoteHost, remotePort);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    getContentPane().add(p, BorderLayout.CENTER);

    setSize(640, 480);
    setVisible(true);
  }
}

//this class handles graphics
class Paper extends JPanel {
  private HashSet hs = new HashSet();
  UdpUnicastServer server;
  UdpUnicastClient client;
  boolean isFromServer;

  public Paper(int myPort, String remoteHost, int remotePort) {
    setBackground(Color.white);
    addMouseListener(new L1());
    addMouseMotionListener(new L2());
    
    server = new UdpUnicastServer(remotePort, remoteHost);
	client = new UdpUnicastClient(myPort);
	ExecutorService executorService = Executors.newFixedThreadPool(2);
    executorService.submit(client);
    executorService.submit(server);
    isFromServer = false;
  }

  //paint 
  public void paintComponent(Graphics g) {
	  try {
    super.paintComponent(g);
    g.setColor(Color.black);
    Iterator i = hs.iterator();
    while(i.hasNext()) {
      Point p = (Point)i.next();
      g.fillOval(p.x, p.y, 2, 2);
    }
	  }catch(ConcurrentModificationException e ) { }
  }

  //add point method with Point parameter
  public void addPoint(Point p) {	  
	  String message = Integer.toString(p.x) + ":" + Integer.toString(p.y);
      server.setMessage(message);
      hs.add(p);
      repaint();
  }
  
  //add point method with string array parameter
  public void addPoint(String[] xy) {
	  String xyFirst = xy[0].replaceAll("\\s","");
      String xySecond = xy[1].replaceAll("\\s","");
      int firstPoint = Integer.parseInt(xyFirst);
      int secondPoint = Integer.parseInt(xySecond);
      Point p = new Point(firstPoint, secondPoint); 
	  hs.add(p);
	  repaint();
  }

  //Mouseadapter class
  class L1 extends MouseAdapter {
    public void mousePressed(MouseEvent me) {
      addPoint(me.getPoint());
    }
  }

  //MouseMotionAdapter class
  class L2 extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent me) {
      addPoint(me.getPoint());
    }
  }

  //This class runs a thread and sends data about the graphics being painted
  class UdpUnicastServer implements Runnable {
	    //The port where the client is listening.
	    private final int remotePort;
	    private final String remoteHost;
	    private String message = "";
	    DatagramSocket serverSocket;

	    public UdpUnicastServer(int remotePort, String remoteHost) {
	        this.remotePort = remotePort;
	        this.remoteHost = remoteHost;
	    }

	    @Override
	    public void run() {
	    	//sever sends message to remotePort
	    	int randPort = 5000+randNumb();
	        try {
	        	if(serverSocket == null) {
	        	serverSocket = new DatagramSocket(randPort);
	        	}
	            if(message.length() > 1) {
	            	InetAddress host = InetAddress.getByName(remoteHost);
	            	
	                DatagramPacket datagramPacket = new DatagramPacket(
	                        message.getBytes(),
	                        message.length(),
	                        host,
	                        remotePort
	                );
	                serverSocket.send(datagramPacket);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }finally {
	        	//serverSocket.close();
	        }
	    }
	    
	    public void setMessage(String msg) {
	    	message = msg;
	    	run();
	    }
	    
	    private int randNumb() {
	    	Random r = new Random();
	    	int low = 10;
	    	int high = 100;
	    	int result = r.nextInt(high-low) + low;
	    	return result;
	    }
	}
  
  //This class runs a thread and receives data about the graphics being painted
  class UdpUnicastClient implements Runnable {
	    private int port;
	    DatagramSocket clientSocket;

	    public UdpUnicastClient(int port) {
	        this.port = port;
	    }

	    @Override
	    public void run() {
	        try {
	        	if(clientSocket == null) {
	        	clientSocket = new DatagramSocket(port);
	        	}

	            byte[] buffer = new byte[1000];

	            //receive incoming messages
	            while (true) {
	                DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
	                clientSocket.receive(datagramPacket);

	                String receivedMessage = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
	                System.out.println("Paint data: " + receivedMessage);
	                
	                String[] xy = receivedMessage.split(":");
		            addPoint(xy);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }finally {
	        	//clientSocket.close();
	        }
	    }
	}
}

