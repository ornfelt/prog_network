import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ChatApp {
	
    public static void main(String[] args) {
    	//get port and server from input if user has entered any values
 	   	int firstPort = args.length > 0 ? Integer.parseInt(args[0]) : 4000;
 	   	String serverName = args.length > 1 ? args[1] : "localhost";
 	   	int secondPort = args.length > 2 ? Integer.parseInt(args[2]) : 4001;
    	MyClient client = new MyClient(firstPort, serverName, secondPort);
    }
    
    static class MyClient implements ActionListener {
    	
    	/* Variables for Audio */
    	boolean controlStatus = false;
    	boolean sendVoiceMessage = false;
    	private AudioHandler ah;
    	/* Variables for Connection */
    	DatagramSocket socket;
    	DatagramSocket otherSocket;
    	ByteArrayOutputStream out;
        InetAddress address;
        String server = "localhost";
        int remotePort = 0;
        int port = 0;
        /* Variables for GUI */
        private JFrame frame = new JFrame();
        private JPanel messagePanel;
        private JTextArea textAreaMessage;
        private JTextField messageTextField;
        private JScrollPane areaScrollPane;
        private JCheckBox sendVoiceBox, darkModeBox;
        private JButton sendMessageButton, sendImageButton;
        boolean darkMode = false;
    	boolean darkModeEnabled = false;
        /* Variables for Images */
        boolean imageBeingSent= false;
        /* Variables for En/Decryption */
        private CryptHandler ch;
        private static final String CHAT_LOG_FILE = "ENCRYPTED_CHAT_LOG_1.txt";
        private static final String SECRET_KEY_FILE = "SECRET_KEY_1";
        private MyKeyHandler myKeyHandler;
        private MyEncryptHandler myEncryptHandler;
        private MyDecryptHandler myDecryptHandler;
        /* Variables for onlinecheck */
        private boolean isOtherUserOnline = false;
        private boolean hasBeenSetToOffline = false;
        private boolean offlineMessagePrinted = false;
        DatagramSocket userSocket;
        DatagramSocket otherUserSocket;
        private boolean twoSecondsHasPassed = false;
        private int secondsCount = 0;
        
        public MyClient(final int myPort, final String ip, final int otherPort) {
        	port = myPort;
        	server = ip;
        	remotePort = otherPort;
        	//init Crypthandler for encryption and decryption of messages
        	ch = new CryptHandler();
        	ch.initObjects();
        	//create key for encrypting chat log file
    		myKeyHandler = new MyKeyHandler();
    		//create secret key for this user
    		myKeyHandler.createKey(SECRET_KEY_FILE);
    		myEncryptHandler = new MyEncryptHandler();
    		myDecryptHandler = new MyDecryptHandler();
        	//init AudioHandler for handling voice input and output via other ports
        	ah = new AudioHandler(ip, (myPort+10), (otherPort+11));
        	setupGraphics();
        	//outputstream
    		out = new ByteArrayOutputStream();
    		
    		System.out.print("Trying to establish connection with server: " + ip + ", my port: " + myPort);
    		try {
				address = InetAddress.getByName(ip);
				socket = new DatagramSocket(myPort);
		        otherSocket = new DatagramSocket();
		        
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			}
    		
    	    Thread send = new Thread (new Runnable () {
                @Override
                 public void run() {
                   while(true){
                	  //send message to other user so (s)he knows you're online
              		  String pingMessage = "ping";
              		  try {
	              		    DatagramPacket datagramPacket = new DatagramPacket(
	              		    		  pingMessage.getBytes(),
	              		    		  pingMessage.length(),
	                                  address,
	                                  remotePort
	                          );
	              		    otherSocket.send(datagramPacket);
	              		      //send ping with 1s intervals
	                          Thread.sleep(1000);
	                          if(secondsCount < 2000) {
	                        	  secondsCount += 1000;
	                        	  if(secondsCount > 1500) {
	                        		  twoSecondsHasPassed = true;
	                        	  }
	                          }
              		    }catch(Exception ex) { ex.printStackTrace(); }
              		System.out.println();
                   }
                }
            });
            send.start ();
    	        Thread receive = new Thread (new Runnable () {
    	            @Override
    	            public void run () {
    	            	try {
    	                    while(true){
    	                    	if(imageBeingSent) {
    	                    		Thread receiveImage = new Thread (new Runnable () {
    	                    			@Override
    	                    			public void run () {
    	                    				try {
    	                    				ServerSocket serverSocket = new ServerSocket(3000);
		    	                    		serverSocket.setSoTimeout(180000);
		    	                    		Socket socket = serverSocket.accept();
    	                    				while(true) {
			    	                    		BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(socket.getInputStream()));
			    	                    		String receivedFileName = "MyReceivedFile.jpg";
			    	                    		//rotate picture twice (some pictures turned upside down when I tested the code)
			    	                    		//img = rotateCw(img);
			    	                    		//img = rotateCw(img);
			    	                    		
			    	                    		//create new image 
			    	                    		ImageIO.write(img, "jpg", new File(receivedFileName));
			    	                    		String paneMessage = "Do you want to open the received file? ";
			    	                            String paneTitle = "Open image?";
			    	                            
			    	                            //ask if user wants to open new image in JFrame
			    	                            int reply = JOptionPane.showConfirmDialog(null, paneMessage, paneTitle, JOptionPane.YES_NO_OPTION);
			    	                            if (reply == JOptionPane.YES_OPTION) {
			    	                          	  //try to open image
			    	                          	  openPictureJFrame((receivedFileName));
			    	                            }
			    	                            serverSocket.close();
			    	                            socket.close();
			    	                            break;
    	                    				}
    	                    				}catch(Exception e ) { e.printStackTrace(); }
    	                    			}
    	                    			});
    	                    		imageBeingSent = false;
    	                    		receiveImage.start();
	    	                        
    	                    	}else {
	    	                        byte[] buffer = new byte[1024];
	    	       	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
	    	       	            	socket.receive(response);
	    	       	            	out.write(response.getData(), 0, response.getData().length);
	    	       	            	
	    	       	            	//handle incoming message
	    	       	            	String quote = new String(buffer, 0, response.getLength());
	    	       	            	//decrypt message
	    	       	            	SealedObject sealed = ch.getSealedObject(quote);
	    	       	            	
	    	       	            	if(quote.equals("ping")) {
	    	       	            		//change online status to online if this user receives ping and other user boolean is currently set to offline
	    	       	            		if(!isOtherUserOnline) {
	    	       	            			isOtherUserOnline = true;
	    	       	            			hasBeenSetToOffline = false;
	    	       	            			if(!offlineMessagePrinted) {
	    	       	            				if(twoSecondsHasPassed) {
	    	       	            					replaceChat(removeOfflineMessageFromChat());
	    	       	            				}
	    	       	            				offlineMessagePrinted = true;
	    	       	            			}
	    	       	            		}
	    	       	            	}else if(quote.equals("IMAGE_BEING_SENT")) {
	    	       	            		imageBeingSent = true;
	    	       	            	}else if(quote.equals("VOICE_BEING_SENT")) {
	    	       	            		ah.startVoiceReceive();
	    	       	            	}else if(quote.equals("VOICE_STOPPED_BEING_SENT")) {
	    	       	            		ah.stopVoiceReceive();
	    	       	            	}else {
	    	       	            		updateChat("Friend", (SecretObject)ch.getSecretObject(sealed));
	    	       	            	}
    	                    	}
    	       	            	System.out.println();
    	                    }
    	                    
    	                  } catch (IOException e) {
    	                      e.printStackTrace();
    	                  }
    	               }
    	           });
    	        receive.start ();
    	        
    	        Thread checkOnlineStatus = new Thread (new Runnable () {
        			@Override
        			public void run () {
        				while(true) {
        					try {
        						//set other user online to false, then wait 3s and check if the value has been updated
        						isOtherUserOnline = false;
								Thread.sleep(3000);
								//if online boolean hasn't been updated by ping message, then the other user is offline
								if(!isOtherUserOnline && !hasBeenSetToOffline) {
									updateChat("Your friend is currently offline :(");
									hasBeenSetToOffline = true;
									offlineMessagePrinted = false;
								}
							} catch (InterruptedException e) { e.printStackTrace(); }
        					//keep thread alive with empty print
        					System.out.println();
        				}
        			}
    	        });
    	        checkOnlineStatus.start();
        }
        
        
        private void setupGraphics() {
        	
        	setupGraphicsComponents();
        	
        	//Crate graphics thread that listens to changes in settings
        	Thread graphicsThread = new Thread (new Runnable () {
	            @Override
	            public void run () {
	            	//finish setting up graphics
	            	messagePanel = new JPanel();
                    //add graphics components to panel
                    messagePanel.add(areaScrollPane);
                    messagePanel.add(messageTextField);
                    messagePanel.add(sendMessageButton);
                    messagePanel.add(sendImageButton);
                    messagePanel.add(sendVoiceBox);
                    messagePanel.add(darkModeBox);
                    //set up the frame and display it
                    frame.add(messagePanel, BorderLayout.CENTER);
                    frame.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                            if (JOptionPane.showConfirmDialog(frame, 
                                "Do you want to save your messages? (They will be encrypted in a file)", "Save chat?", 
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                            	//mySignHandler.signData(textAreaMessage.getText(), PRIVATE_KEY_FILE, CHAT_LOG_FILE);
                            	myEncryptHandler.encryptDataToFile(textAreaMessage.getText(), SECRET_KEY_FILE, CHAT_LOG_FILE);
                                System.exit(0);
                                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            }else {
                            	System.exit(0);
                            }
                        }
                    });
                    frame.setTitle("Chat App");
                    frame.pack();
                    frame.setSize(720, 480);
                    frame.setVisible(true);
                    frame.getRootPane().setDefaultButton(sendMessageButton);
                    //set frame to center of screen
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
                    
	            	try {
	                    while(true){
	                    	System.out.println();
	                    	//check if dark mode is checked and graphics needs to be updated
	                 	   if(!darkModeEnabled && darkModeBox.isSelected()) {
	                 		   changeToDarkMode();
	                 		   System.out.println("Trying to switch to dark mode");
	                 	   }else if(darkModeEnabled && !darkModeBox.isSelected()) {
	                 		   changeToNormalMode();
	                 		   System.out.println("Trying to switch to normal mode");
	                 	   }
	                 	   
	                 	   //check if send voice message box is selected
	                 	   if(ah.stopSend && sendVoiceBox.isSelected()) {
	                 		   ah.startVoiceSend();
	                 		   //send message to prepare other user for listening to voice message
	                 		  String voiceMessage = "VOICE_BEING_SENT";
	                 		 try {
	                 		    DatagramPacket datagramPacket = new DatagramPacket(
	                 		    		 voiceMessage.getBytes(),
	                 		    		 voiceMessage.length(),
	                                     address,
	                                     remotePort
	                             );
	                 		    otherSocket.send(datagramPacket);
	                 		    //wait a bit to make sure other user is prepared...
	                             Thread.sleep(200);
	                 		    }catch(Exception ex) { ex.printStackTrace(); }
	                 		   System.out.println("Microphone enabled");
	                 	   }else if(!ah.stopSend && !sendVoiceBox.isSelected()) {
	                 		  ah.stopVoiceSend();
	                 		  
	                 		   //send message to prepare other user for listening to voice message
	                 		  String voiceMessage = "VOICE_STOPPED_BEING_SENT";
	                 		 try {
	                 		    DatagramPacket datagramPacket = new DatagramPacket(
	                 		    		 voiceMessage.getBytes(),
	                 		    		 voiceMessage.length(),
	                                     address,
	                                     remotePort
	                             );
	                 		    otherSocket.send(datagramPacket);
	                 		    //wait a bit to make sure other user is prepared...
	                             Thread.sleep(200);
	                 		    }catch(Exception ex) { ex.printStackTrace(); }
	                 		   System.out.println("Microphone disabled");
	                 	   }
	                    }
	            	}catch(Exception e) { e.printStackTrace(); }
	            }
        	});
        	graphicsThread.start();
        	
        }
        
        static String readFile(String path, Charset encoding)
        		  throws IOException
        		{
        		  byte[] encoded = Files.readAllBytes(Paths.get(path));
        		  return new String(encoded, encoding);
        		}
        
        //update chat method with incoming string message
        private void updateChat(String author, String message) {
        	if(textAreaMessage.getText().equals("No messages...")) {
        		textAreaMessage.setText(author + ": " + message);
        	}else {
        		textAreaMessage.setText(textAreaMessage.getText() + "\n" + author + ": " + message);
        	}
        }
        
        //update chat method with incoming Secretobject message
        private void updateChat(String author, SecretObject message) {
        	if(textAreaMessage.getText().equals("No messages...")) {
        		textAreaMessage.setText(author + ": " + message);
        	}else {
        		textAreaMessage.setText(textAreaMessage.getText() + "\n" + author + ": " + message);
        	}
        }
        
        //update chat method with incoming Secretobject message
        private void updateChat(String message) {
        	if(textAreaMessage.getText().equals("No messages...")) {
        		textAreaMessage.setText(message);
        	}else {
        		textAreaMessage.setText(textAreaMessage.getText() + "\n" + message);
        	}
        }
        
        private void replaceChat(String message) {
        	textAreaMessage.setText(message);
        }
        
        private String removeOfflineMessageFromChat() {
        	if(textAreaMessage.getText().contains("Your friend is currently offline :(")) {
        		String[] messages = textAreaMessage.getText().split("\n");
        		if(messages.length <= 1) {
        			return "No messages...";
        		}
        		String newMessages = messages[0];
	        		for(int i = 1; i < messages.length-1; i++) {
	        			if(messages[i].equals("Your friend is currently offline :()")) return newMessages;
	        			newMessages += "\n" + messages[i];
	        		}
        		return newMessages;
        	}else {
        		return textAreaMessage.getText();
        	}
        }
        
        private void setupGraphicsComponents() {
        	//the send button
        	sendMessageButton = new JButton("Send");
            sendMessageButton.addActionListener(this);
            sendMessageButton.setSize(100, 5);
            
            sendImageButton = new JButton("Send image");
            sendImageButton.setSize(100, 5);
            sendImageButton.addActionListener(new ActionListener() {
            	  public void actionPerformed(ActionEvent e) {
            		    String chosenFile = chooseFile();
            		    //send a message to prepare other user for receiving file (won't be seen by user)
            		    String message = "IMAGE_BEING_SENT";
            		    try {
            		    DatagramPacket datagramPacket = new DatagramPacket(
                                message.getBytes(),
                                message.length(),
                                address,
                                remotePort
                        );
            		    otherSocket.send(datagramPacket);
            		    //wait half a second to make sure other user is prepared...
                        Thread.sleep(200);
            		    }catch(Exception ex) { ex.printStackTrace(); }
            		    sendImageFile2(chosenFile);
            		  }
        		  });
            
            //text area for message
            textAreaMessage = new JTextArea();
            
            String logMessages = "";
            //get logged messages (if any)
            logMessages = myDecryptHandler.getDecryptFileContent(CHAT_LOG_FILE, SECRET_KEY_FILE) == null ? "No messages..." : myDecryptHandler.getDecryptFileContent(CHAT_LOG_FILE, SECRET_KEY_FILE);
            logMessages = cleanStringFromOfflineMessages(logMessages);
			textAreaMessage.setText(logMessages);
            textAreaMessage.setLineWrap(true);
            textAreaMessage.setWrapStyleWord(true);
            textAreaMessage.setEditable(false);
            
            //make text area scrollable
            areaScrollPane = new JScrollPane(textAreaMessage);
            areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            areaScrollPane.setPreferredSize(new Dimension(700, 400));
            
            //text field where user can send a message
            messageTextField = new JTextField(25);
            
            sendVoiceBox = new JCheckBox("Send voice");
            sendVoiceBox.setBounds(100, 100, 50, 50);
            darkModeBox = new JCheckBox("Dark mode");
            darkModeBox.setBounds(100, 100, 50, 50);
        }
        
        //"clean" the incoming message from user offline message
        private String cleanStringFromOfflineMessages(String message) {
        	//we're trying to get rid of the offline message below
        	String offlineMessage = "Your friend is currently offline :(";
        	//split message by lines
        	String[] messageSplit = message.split("\n");
        	if(messageSplit.length <= 1 && !message.equals(offlineMessage)) return message;
        	String line = messageSplit[0];
        	String newMessage = line;
        	
        	if(messageSplit.length > 1) {
        		for(int i = 1; i < messageSplit.length; i++) {
        			line = messageSplit[i];
        			if(newMessage.equals(offlineMessage)) {
        				newMessage = line;
        			}else if(!line.equals(offlineMessage)) {
        				newMessage += "\n" + line;
        			}
        		}
        	}
        	return newMessage;
        }
        
        private void changeToDarkMode() {
        	textAreaMessage.setBackground(Color.black);
        	textAreaMessage.setForeground(Color.white);
        	sendMessageButton.setBackground(Color.black);
        	sendMessageButton.setForeground(Color.white);
        	sendImageButton.setBackground(Color.black);
        	sendImageButton.setForeground(Color.white);
        	messageTextField.setBackground(Color.black);
        	messageTextField.setForeground(Color.white);
        	sendVoiceBox.setBackground(Color.black);
        	sendVoiceBox.setForeground(Color.white);
        	darkModeBox.setBackground(Color.black);
        	darkModeBox.setForeground(Color.white);
        	messagePanel.setBackground(Color.black);
        	darkModeEnabled = true;
        }
        
        private void changeToNormalMode() {
        	textAreaMessage.setBackground(Color.white);
        	textAreaMessage.setForeground(Color.black);
        	sendMessageButton.setBackground(Color.white);
        	sendMessageButton.setForeground(Color.black);
        	sendImageButton.setBackground(Color.white);
        	sendImageButton.setForeground(Color.black);
        	messageTextField.setBackground(Color.white);
        	messageTextField.setForeground(Color.black);
        	sendVoiceBox.setBackground(Color.white);
        	sendVoiceBox.setForeground(Color.black);
        	darkModeBox.setBackground(Color.white);
        	darkModeBox.setForeground(Color.black);
        	messagePanel.setBackground(Color.white);
        	darkModeEnabled = false;
        }
        
        private void sendImageFile2(String file) {
        	Socket client;
			try {
				int tempImagePort = 3000;
				client = new Socket(server, tempImagePort);
				BufferedImage bimg = null;
				bimg = ImageIO.read(new File(file));
	        	ImageIO.write(bimg,"jpg",client.getOutputStream());
	        	client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        public void printString(String filePath, String text) throws IOException {
            PrintWriter textToWrite = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
            textToWrite.print(text);
            textToWrite.close();
        }
        
        private void sendImageFile(String file) {
        	/*
      	   BufferedImage bimg;
      	   try {
				bimg = ImageIO.read(new File(file));
				//ImageIO.write(bimg,"jpg",client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
      	   */
        	try {
    		 BufferedImage img = ImageIO.read(new File(file));
        	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	 ImageIO.write(img, "jpg", baos);
        	 baos.flush();
        	 byte[] buffer = baos.toByteArray();

        	 System.out.println(buffer.length);

        	  DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9876);
        	  otherSocket.send(packet);
        	}catch(Exception e) { e.printStackTrace(); }
        }
      	   
      	   private void receiveImageFile(String file) {
      		//receive image from client
               try {
                 BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(file));
                  	
                  System.out.println("Image received!");
                  String receivedFileName = "katter2.jpg";
                  //full path might be needed 
                  String myPath = "C:\\Users\\...\\";
                  
                  //rotate picture twice (the pictures turned upside down when I tested the code)
                  img = rotateCw(img);
                  img = rotateCw(img);
                  //create new image 
                  ImageIO.write(img, "jpg", new File(receivedFileName));
                  
                  String message = "Do you want to open the received file? ";
                  String title = "Open image?";
                  
                  //ask if user wants to open new image in JFrame
                  int reply = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
                  if (reply == JOptionPane.YES_OPTION) {
                	  //try to open image
                	  openPictureJFrame(receivedFileName);
                  }
              }
             catch(SocketTimeoutException st){ System.out.println("Socket timed out!"); }
             catch(IOException e){ e.printStackTrace(); }
             catch(Exception ex) { ex.printStackTrace(); }
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
  				   frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
        
        //process the button clicks
        @Override
        public void actionPerformed(ActionEvent e) {
        	System.out.println("button clicked");
        	//only do something if text is written into text field
        	if(!messageTextField.getText().equals("")) {
	        	try {
					out.write(messageTextField.getText().getBytes());
					String message = messageTextField.getText();
					SealedObject sealed = ch.getSealedObject(message);
					//save encrypted text to chat log file
					message = String.valueOf((SecretObject)ch.getSecretObject(sealed));
					System.out.println(messageTextField.getText());
					//send message to other user (encrypted)
					DatagramPacket datagramPacket = new DatagramPacket(
	                        message.getBytes(),
	                        message.length(),
	                        address,
	                        remotePort
	                );
	                otherSocket.send(datagramPacket);
	                //update chat with new message from this user
	                updateChat("You", message);
	                //remove written message from this users text input field
	                messageTextField.setText("");
				} catch (Exception ex) { ex.printStackTrace(); }
        	}
        }
    	
        private String chooseFile() {
        	final JFileChooser fc = new JFileChooser();
        	int returnVal = fc.showOpenDialog(fc);
        	String filePath = null;
        	if(returnVal == JFileChooser.APPROVE_OPTION) {
        		filePath = fc.getSelectedFile().getAbsolutePath();
        	}else {
        		System.out.println("User canceled file choosing");
        	}
        	return filePath;
        }
    
    
   static class AudioHandler {
	   
	   	public boolean stopSend = false;
	   	public boolean stopReceive = false;
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
	    Thread send, receive;
		
		public AudioHandler(String server, int myPort, int otherPort) {
		    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		    
		    try {
			    microphone = AudioSystem.getTargetDataLine(format);
			    microphone = (TargetDataLine) AudioSystem.getLine(info);
			    microphone.open(format);
		
			    out = new ByteArrayOutputStream();
			    
			    //CHUNK_SIZE = 1024;
			    CHUNK_SIZE = 512;
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
		    
		     send = new Thread (new Runnable () {
	           String msg;
	            @Override
	             public void run() {
	               while(true){
	            	   try {
	  	            	 byte[] buffer = new byte[1024];
	  	            	 int bytesRead = 0;
	  	            	 
	  	                 for(;;) {
	  	            	 //while(bytesRead < 10000) {
	  	                     numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
	  	                     bytesRead += numBytesRead;
	  	                     // write the mic data to a stream for use later
	  	                     out.write(data, 0, numBytesRead); 
	  	                     // write mic data to stream for immediate playback
	  	                     //speakers.write(data, 0, numBytesRead);            
	  	                     if(!stopSend) {
	  	                    	DatagramPacket request = new DatagramPacket(data,numBytesRead, address, otherPort);
	  	                     	socket.send(request);
	  	                     }
	  	                 }
	  	            	 }catch(IOException e) { e.printStackTrace(); }
	               }
	            }
	        });
	        send.start ();
		        
		        receive = new Thread (new Runnable () {
		            @Override
		            public void run () {
		            	try {
		                    while(true){
		                        byte[] buffer = new byte[512];
		       	            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		       	            	socket.receive(response);

		       	            	out.write(response.getData(), 0, response.getData().length);
		       	            	if(!stopReceive) {
		       	            		speakers.write(response.getData(), 0, response.getData().length);
		       	            	}
		       	            	//out.close();

		       	            	//you can print audio data with the prints below
		       	            	//String quote = new String(buffer, 0, response.getLength());
		       	            	//System.out.println(quote);
		       	            	//System.out.println();
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
		
	   	public void stopVoiceSend() {
	   		stopSend = true;
	   	}
	   	
	   	public void stopVoiceReceive() {
	   		stopReceive = true;
	   		speakers.flush();
	   	}
	   	
	   	public void startVoiceSend() {
	   		stopSend = false;
	   	}
	   	
	   	public void startVoiceReceive() {
	   		stopReceive = false;
	   	}
   }
   
   static class CryptHandler {
    	
    	private Cipher ecipher;
        private Cipher dcipher;
        private SecretKey key;
        
        public CryptHandler() {
        	
        }
    
	    private void initObjects() {
	    	
	    	// generate secret key using DES algorithm
	        try {
				key = KeyGenerator.getInstance("DES").generateKey();
	            ecipher = Cipher.getInstance("DES");
	            dcipher = Cipher.getInstance("DES");
	            // initialize the ciphers with the given key
				ecipher.init(Cipher.ENCRYPT_MODE, key);
				dcipher.init(Cipher.DECRYPT_MODE, key);
				
	        } catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
	        catch (InvalidKeyException e) {
	            System.out.println("Invalid key: " + e.getMessage());
	        } catch (NoSuchPaddingException e) {
	        	System.out.println("No such padding: " + e.getMessage());
			}
	    }
	    
	    private SealedObject getSealedObject(String message) {
	    	
	    	SealedObject sealedObject = null;
	    	try {
	    	// create a sealed object
			sealedObject = new SealedObject(new SecretObject(message), ecipher);
	    	}
	        catch (IllegalBlockSizeException e) {
	            System.out.println("Illegal Block:" + e.getMessage());
	        }
	        catch (IOException e) {
	            System.out.println("I/O Error:" + e.getMessage());
	        }
	    	// get the algorithm with the object has been sealed
			String algorithm = sealedObject.getAlgorithm();
			System.out.println("Algorithm " + algorithm);
	    	return sealedObject;
	    }
	    
	    private SecretObject getSecretObject(SealedObject sealedObject) {
	    	//unseal (decrypt) the object
	    	SecretObject o = null;
	    	try {
	    	
			o = (SecretObject) sealedObject.getObject(dcipher);
			System.out.println("Original Object: " + o);
	    	}catch (ClassNotFoundException e) {
	            System.out.println("Class Not Found: " + e.getMessage());
	        }
	    	catch (BadPaddingException e) {
	            System.out.println("Bad Padding: " + e.getMessage());
	        }
	    	catch (IllegalBlockSizeException e) {
	            System.out.println("Illegal Block: " + e.getMessage());
	        }
	    	catch (IOException e) {
	            System.out.println("I/O Error: " + e.getMessage());
	        }
			return o;
	    }
}
 
    public static class SecretObject implements Serializable {
 
        private static final long serialVersionUID = -1335351770906357695L;
        private final String message;
 
        public SecretObject(String message) {
            this.message = message;
        }
 
        @Override
        public String toString() {
            return message;
        }
    }
    
    static class MyKeyHandler{
    	
    	private static final String ALGO = "AES";
    	private static final String PASS_HASH_ALGO = "SHA-256";
    	
    	public MyKeyHandler() { }
    	
    	public void createKey(String key) {
    		try {
    			char[] keyChars = key.toCharArray();
    			Key secretKey = buildKey(keyChars);
    			printString(key, key);
    		}catch(Exception e) { e.printStackTrace(); }
    	}
    	
    	//build key via char array of password
    	private static Key buildKey(char[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	    MessageDigest digester = MessageDigest.getInstance(PASS_HASH_ALGO);
    	    digester.update(String.valueOf(password).getBytes("UTF-8"));
    	    byte[] key = digester.digest();
    	    SecretKeySpec spec = new SecretKeySpec(key, ALGO);
    	    return spec;
    	  }
    	
    	//print text to file (will overwrite already existing file)
    	public static void printString(String filePath, String text) throws IOException {
            PrintWriter textToWrite = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));
            textToWrite.print(text);
            textToWrite.close();
        }
    }
    
    static class MyEncryptHandler{
    	private static final String ALGO = "AES";
    	private static final String PASS_HASH_ALGO = "SHA-256";
    	
    	public void encryptDataToFile(String data, String key, String encryptedFileName) {
    		try {
    			String keyFromFile = readFile(key);
    			Key secretKey = buildKey(keyFromFile.toCharArray());
    			printString(encryptedFileName, encrypt(secretKey, data));
    			
    		}catch(Exception e) { e.printStackTrace(); }
    	}
    	
    	private static Key buildKey(char[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	    MessageDigest digester = MessageDigest.getInstance(PASS_HASH_ALGO);
    	    digester.update(String.valueOf(password).getBytes("UTF-8"));
    	    byte[] key = digester.digest();
    	    SecretKeySpec spec = new SecretKeySpec(key, ALGO);
    	    return spec;
    	  }
    	
    	//read file and get string of the content
    	static String readFile(String path, Charset encoding) throws IOException
    	{
    	  byte[] encoded = Files.readAllBytes(Paths.get(path));
    	  return new String(encoded, encoding);
    	}
    	
    	//read file and get string of the content
    	static String readFile(String path) throws IOException
    	{
    	  byte[] encoded = Files.readAllBytes(Paths.get(path));
    	  return new String(encoded);
    	}
    	
    	//print text to file (will overwrite already existing file)
    	public static void printString(String filePath, String text) throws IOException {
            PrintWriter textToWrite = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));
            textToWrite.print(text);
            textToWrite.close();
        }
    	
    	//this method encrypts the incoming string 
    	public static String encrypt(Key key, String data) throws Exception {
    		Cipher c = Cipher.getInstance(ALGO);
    		c.init(Cipher.ENCRYPT_MODE, key);
    		byte[] encVal = c.doFinal(data.getBytes());
    		byte[] encryptedValueByte = Base64.getEncoder().encode(encVal);
    		return new String(encryptedValueByte);
    	}
    }
    
    static class MyDecryptHandler{
    	
    	private static final String ALGO = "AES";
    	private static final String PASS_HASH_ALGO = "SHA-256";
    	
    	public String getDecryptFileContent(String fileName, String key) {
    		try {
    			String keyFromFile = readFile(key);
    			String fileContent = readFile(fileName);
    			Key secretKey = buildKey(keyFromFile.toCharArray());
    			
    			String decryptedContent = decrypt(secretKey, fileContent);
    			if(decryptedContent == null || decryptedContent.equals("")) {
    				return null;
    			}else {
    				return decryptedContent;
    			}
    			
    		}catch(Exception e) { e.printStackTrace(); }
    		return null;
    	}
    	
    	//this method decrypts the incoming string 
    	public static String decrypt(Key key, String encryptedData) throws Exception {
    		Cipher c = Cipher.getInstance(ALGO);
    		c.init(Cipher.DECRYPT_MODE, key);
    		byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
    		byte[] decValue = c.doFinal(decordedValue);
    		String decryptedString = new String(decValue);
    		return decryptedString;
    	}
    	
    	private static Key buildKey(char[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	    MessageDigest digester = MessageDigest.getInstance(PASS_HASH_ALGO);
    	    digester.update(String.valueOf(password).getBytes("UTF-8"));
    	    byte[] key = digester.digest();
    	    SecretKeySpec spec = new SecretKeySpec(key, ALGO);
    	    return spec;
    	  }
    	
    	//read file and get string of the content
    	static String readFile(String path, Charset encoding) throws IOException
    	{
    	  byte[] encoded = Files.readAllBytes(Paths.get(path));
    	  return new String(encoded, encoding);
    	}
    	
    	//read file and get string of the content
    	static String readFile(String path) throws IOException
    	{
    	  byte[] encoded = Files.readAllBytes(Paths.get(path));
    	  return new String(encoded);
    	}
    }
}
}