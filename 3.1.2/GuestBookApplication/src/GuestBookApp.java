import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This class uses a SQL database to store Guestbook data with values such as: Name, email, hompage and comments
 * @author Jonas Örnfelt
 *
 */

public class GuestBookApp {
	
	//main method that connects to database and selects all guestbook entries
	public static void main(String[] args) {
		String myResString = "";
		try {
			// get connection to db
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection myConn = DriverManager.getConnection("jdbc:mysql://atlas.dsv.su.se:3306/db_20502149?characterEncoding=latin1&useConfigs=maxPerformance","usr_20502149", "502149");
			//create statement
			Statement st = myConn.createStatement();
			//execute query 
			ResultSet res = st.executeQuery("select * from guest_book_entries;");
			
			//query for updating database 
			/*
			Statement st2 = myConn.createStatement();
			String removeQuery = "delete from guest_book_entries where name = 'Mr html';";
			st2.executeUpdate(removeQuery);
			*/

			while(res.next()) {
				System.out.println(res.getString("name") + ", " + res.getString("email"));
				//can make a list instead here
				myResString += res.getString("name") + ", ";
			}
			if(myResString.length() > 5){
				myResString = myResString.substring(0, myResString.length() - 2);
			}
			
			myConn.close();

		}catch(Exception e) {e.printStackTrace();}
		
		new GUI(myResString);
	}
}

//GUI class that listens to button click
class GUI implements ActionListener {
    
	//graphic components
    private JFrame frame = new JFrame();
    private JButton button;
    private JLabel labelName, labelMail, labelPage, labelComment, labelAdd;
    private JTextField textName, textMail, textPage, textComment;
    private JTextArea resArea;
    private JScrollPane areaScrollPane;

    public GUI(String text) {

        // the clickable button
    	button = new JButton("Add");
        button.addActionListener(this);
        button.setSize(100, 5);
        //labels for user input
        labelName = new JLabel("Name:");
        labelMail = new JLabel("Email:");
        labelPage = new JLabel("Homepage:");
        labelComment = new JLabel("Comment:");
        labelAdd = new JLabel("Add");
        //textfields for user input
        textName = new JTextField(20);
        textMail = new JTextField(20);
        textPage = new JTextField(20);
        textComment = new JTextField(20);
        //textarea for database results
        resArea = new JTextArea();
        
        resArea.setLineWrap(true);
        resArea.setWrapStyleWord(true);
        //make textarea scrollable
        areaScrollPane = new JScrollPane(resArea);
        
        areaScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(700, 245));
        resArea.setText(text);

        // the panel with the button and text
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(0,2, 5, 5));
        
        //add elements to panel
        panel.add(labelName);
        panel.add(textName);
        panel.add(labelMail);
        panel.add(textMail);
        panel.add(labelPage);
        panel.add(textPage);
        panel.add(labelComment);
        panel.add(textComment);
        panel.add(labelAdd);
        panel.add(button);

        JPanel resPanel = new JPanel();
        resPanel.add(areaScrollPane);
        
        // set up the frame and display it
        frame.add(panel, BorderLayout.NORTH);
        frame.add(resPanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("GuestBookApp");
        //frame.pack();
        frame.setSize(720, 480);
        frame.setVisible(true);
    }
    

    // process the button clicks
    public void actionPerformed(ActionEvent e) {
    	String name;
    	String mail;
    	String page;
    	String comment;
    	
    	//check user input and set to "censur" if it contains html code
    	name = doesContainHtml(textName.getText().toLowerCase()) ? "censur" : textName.getText();
    	mail = doesContainHtml(textMail.getText().toLowerCase()) ? "censur" : textMail.getText();
    	page = doesContainHtml(textPage.getText().toLowerCase()) ? "censur" : textPage.getText();
    	comment = doesContainHtml(textComment.getText().toLowerCase()) ? "censur" : textComment.getText();
        
        String myResString = "";
		try {
			// get connection to db
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection myConn = DriverManager.getConnection("jdbc:mysql://atlas.dsv.su.se:3306/db_20502149?characterEncoding=latin1&useConfigs=maxPerformance","usr_20502149", "502149");
			//execute query 
			String insertQuery = "insert into guest_book_entries (name, email, page, comment) values (?, ?, ?, ?)";
			// create the mysql insert preparedstatement
		    PreparedStatement preparedStmt = myConn.prepareStatement(insertQuery);
		    preparedStmt.setString(1, name);
		    preparedStmt.setString(2, mail);
		    preparedStmt.setString(3, page);
		    preparedStmt.setString(4, comment);
		    
		    preparedStmt.execute();
		    
		    //reload textArea with updated db
		    //Thread.sleep(100);
		    //create statement
			Statement st = myConn.createStatement();
			//execute query 
			ResultSet res = st.executeQuery("select * from guest_book_entries;");
			//process the result set
			while(res.next()) {
				System.out.println(res.getString("name") + ", " + res.getString("email"));
				//might make a list instead here
				myResString += res.getString("name") + ", ";
			}
			if(myResString.length() > 5){
				myResString = myResString.substring(0, myResString.length() - 2);
			}
		    myConn.close();
		    
		}catch(Exception ex) {ex.printStackTrace();}
		
		//remove text from input fields
		textName.setText(null);
		textMail.setText(null);
		textPage.setText(null);
		textComment.setText(null);
        updateResArea(myResString);
        
    }
    
  //check incoming string for HTML elements
    private boolean doesContainHtml(String textToCheck) {
    	String tag = "</";
    	String endTag = ">";
    	String doctype = "<!DOCTYPE html>";
    	String htmlTag = "<html>";
    	String htmlString = "html";
    	
    	if(textToCheck.contains(tag) && textToCheck.contains(endTag)) {
    		return true;
    	}else if(textToCheck.contains(doctype) || textToCheck.contains(htmlTag) || textToCheck.contains(htmlString)) {
    		return true;
    	}else {
    		return false;
    	}
    }
    //this updates the textarea with database entries
    public void updateResArea(String text) {
    	resArea.setText(text);
    }
}
