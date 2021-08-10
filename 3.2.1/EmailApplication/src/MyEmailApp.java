import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This class provides a GUI which you can use to send emails 
 * @author Jonas Örnfelt
 *
 */

public class MyEmailApp {
	
	public static void main(String[] args) {
		//start Email GUI
		new EmailGUI();
	}
}

//class for handling GUI with actionlistener for button clicks
class EmailGUI implements ActionListener {
    
	//graphic components
    private JFrame frame = new JFrame();
    private JButton button;
    private JLabel labelMailServer, labelEmailAccount, labelPassword, labelFrom, labelTo, labelSubject;
    private JTextField textMailServer, textEmailAccount, textFrom, textTo, textSubject;
    JPasswordField textPassword;
    private JTextArea textAreaMessage;
    private JScrollPane areaScrollPane;

    public EmailGUI() {

        // the clickable button
    	button = new JButton("Send");
        button.addActionListener(this);
        button.setSize(100, 5);
        //labels for user input
        labelMailServer = new JLabel("Mail server:");
        labelEmailAccount = new JLabel("Email account:");
        labelPassword = new JLabel("Password:");
        labelFrom = new JLabel("From:");
        labelTo = new JLabel("To:");
        labelSubject = new JLabel("Subject:");
        
        //textfields for user input
        textMailServer = new JTextField(20);
        //pre-set gmail server
        textMailServer.setText("gmail.com");
        textEmailAccount = new JTextField(20);
        textPassword = new JPasswordField(20);
        textFrom = new JTextField(20);
        textTo= new JTextField(20);
        textSubject= new JTextField(20);
        
        //text area for message
        textAreaMessage = new JTextArea();
        textAreaMessage.setText("Enter message here");
        textAreaMessage.setLineWrap(true);
        textAreaMessage.setWrapStyleWord(true);
        //make text area scrollable
        areaScrollPane = new JScrollPane(textAreaMessage);
        
        areaScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(700, 245));
       
        // the panel with the button and text
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagLayout gridbag = new GridBagLayout();
     	GridBagConstraints c = new GridBagConstraints();
     	panel.setLayout(gridbag);
     	
     	c.fill = GridBagConstraints.BOTH;
     	c.weightx = 1.0;
     	gridbag.setConstraints(labelMailServer, c);
     	panel.add(labelMailServer);
     	c.gridwidth = GridBagConstraints.REMAINDER; //end row
     	gridbag.setConstraints(textMailServer, c);
     	panel.add(textMailServer);
     	
     	c.gridwidth = GridBagConstraints.BOTH;
     	gridbag.setConstraints(labelEmailAccount, c);
     	panel.add(labelEmailAccount);
     	c.gridwidth = GridBagConstraints.REMAINDER; //end row
     	gridbag.setConstraints(textEmailAccount, c);
     	panel.add(textEmailAccount);
     	
     	c.gridwidth = GridBagConstraints.BOTH;
     	gridbag.setConstraints(labelPassword, c);
     	panel.add(labelPassword);
     	c.gridwidth = GridBagConstraints.REMAINDER; //end row
     	gridbag.setConstraints(textPassword, c);
     	panel.add(textPassword);
        
     	c.gridwidth = GridBagConstraints.BOTH;
     	gridbag.setConstraints(labelFrom, c);
     	panel.add(labelFrom);
     	c.gridwidth = GridBagConstraints.REMAINDER; //end row
     	gridbag.setConstraints(textFrom, c);
     	panel.add(textFrom);
     	
     	c.gridwidth = GridBagConstraints.BOTH;
     	gridbag.setConstraints(labelTo, c);
     	panel.add(labelTo);
     	c.gridwidth = GridBagConstraints.REMAINDER; //end row
     	gridbag.setConstraints(textTo, c);
     	panel.add(textTo);
     	
     	c.gridwidth = GridBagConstraints.BOTH;
     	gridbag.setConstraints(labelSubject, c);
     	panel.add(labelSubject);
     	c.gridwidth = GridBagConstraints.REMAINDER; //end row
     	gridbag.setConstraints(textSubject, c);
     	panel.add(textSubject);

        JPanel messagePanel = new JPanel();
        messagePanel.add(areaScrollPane);
        messagePanel.add(button);
        
        // set up the frame and display it
        frame.add(panel, BorderLayout.NORTH);
        frame.add(messagePanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("EmailApp");
        frame.pack();
        frame.setSize(720, 480);
        frame.setVisible(true);
        //set frame to center of screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
    }

    // process the button clicks
    public void actionPerformed(ActionEvent e) {
    	//init variables
    	String mailServer;
    	String emailAccount;
    	String password;
    	String from;
    	String to;
    	String subject;
    	String message;
        
    	//get user input from textfields
		try {
			mailServer = textMailServer.getText();
	    	emailAccount = textEmailAccount.getText();
	    	//textPassword.getText() seems to work as well, and can be used if password is a textfield instead of passwordfield
	    	//password = textPassword.getText();
	    	char[] pass = textPassword.getPassword();
	    	password = new String(pass);
	    	from = textFrom.getText();
	    	to = textTo.getText();
	    	subject = textSubject.getText();
	    	message = textAreaMessage.getText();
	    	
	    	//check all input fields except message, if any is empty, then show message dialog to user
	    	if(mailServer.equals("") || emailAccount.equals("") || password.equals("") || from.equals("") || to.equals("") || subject.equals("")) {
	    		JOptionPane.showMessageDialog(null, "Please enter info on all fields");
	    	}else {
	    	//my test email
	    	sendMail(mailServer, emailAccount, password, from, to, subject, message);
	    	}
		    
		}catch(Exception ex) {ex.printStackTrace();}
		
		//remove text from input fields
		//private JTextField textMailServer, textEMailAccount, textPassword, textFrom, textTo, textSubject;
		textEmailAccount.setText(null);
		textPassword.setText(null);
		textFrom.setText(null);
		textTo.setText(null);
		textSubject.setText(null);
    }
    
    // this method sends the mail
    public static void sendMail(String server, String myEmailAccount, String myPassword, String from, String recepient,
    		String subject, String messageToSend) {
    	
		Properties p = new Properties();
		p.put("mail.smtp.auth", "true");
		p.put("mail.smtp.starttls.enable", "true");
		p.put("mail.smtp.host", "smtp." + server);
		p.put("mail.smtp.port", "587");
		
		Session s = Session.getInstance(p, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(myEmailAccount, myPassword);
			}
		});
		
		Message message = prepareMessage(s, myEmailAccount, from, recepient, subject, messageToSend);
		try {
		Transport.send(message);
		JOptionPane.showMessageDialog(null, "Message sent!");
		}catch(Exception e) {e.printStackTrace();}
	}
	
    //prepare message and then return it
	private static Message prepareMessage(Session session, String myEmailAcc, String from, String recepient,
			String subject, String messageToSend) {
		try {
			Message message = new MimeMessage(session);
			//message.setFrom(new InternetAddress(myEmailAcc));
			message.setFrom(new InternetAddress(from));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			message.setSubject(subject);
			message.setText(messageToSend);
			//can also send html code as content, see below
			//String htmlCode = "<h1> We Love Java </h1> <br/> <h2> <b>My message </b></h2>";
			//message.setContent(htmlCode, "text/html");
			return message;
			
		}catch(Exception e ) {e.printStackTrace();}
		return null;
	}
}
