import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DbConnect {

	public static void main(String[] args) {
		String myResString = "";
		try {
			// get connection to db
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/javatesting_schema?characterEncoding=latin1&useConfigs=maxPerformance","javatester", "password");
			Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/GuestBook?characterEncoding=latin1&useConfigs=maxPerformance","javatester", "password");
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
		}catch(Exception e) {e.printStackTrace();}
		
		new OLDGUI(myResString);
		
	}
	
}
	class OLDGUI implements ActionListener {
	    
		//graphic components
	    private JFrame frame = new JFrame();
	    private JButton button;
	    private JLabel labelName, labelMail, labelPage, labelComment, labelAdd;
	    private JTextField textName, textMail, textPage, textComment;
	    private JTextArea resArea;
	    private JScrollPane areaScrollPane;

	    public OLDGUI(String text) {

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
	        
	        /*
	         * Alternative layout with Gridbaglayout
	        GridLayout experimentLayout = new GridLayout(0,2, 10, 10);
	        panel.setLayout(experimentLayout);

	        GridBagLayout gridbag = new GridBagLayout();
         	GridBagConstraints c = new GridBagConstraints();
         	panel.setLayout(gridbag);
         	
         	c.fill = GridBagConstraints.BOTH;
         	c.weightx = 1.0;
         	gridbag.setConstraints(labelName, c);
         	panel.add(labelName);
         	c.gridwidth = GridBagConstraints.REMAINDER; //end row
         	gridbag.setConstraints(textName, c);
         	panel.add(textName);
         	
         	c.gridwidth = GridBagConstraints.BOTH;
         	gridbag.setConstraints(labelMail, c);
         	panel.add(labelMail);
         	c.gridwidth = GridBagConstraints.REMAINDER; //end row
         	gridbag.setConstraints(textMail, c);
         	panel.add(textMail);
         	
         	c.gridwidth = GridBagConstraints.BOTH;
         	gridbag.setConstraints(labelPage, c);
         	panel.add(labelPage);
         	c.gridwidth = GridBagConstraints.REMAINDER; //end row
         	gridbag.setConstraints(textPage, c);
         	panel.add(textPage);
	        
         	c.gridwidth = GridBagConstraints.BOTH;
         	gridbag.setConstraints(labelComment, c);
         	panel.add(labelComment);
         	c.gridwidth = GridBagConstraints.REMAINDER; //end row
         	gridbag.setConstraints(textComment, c);
         	panel.add(textComment);

         	c.weightx = 0.0;
         	gridbag.setConstraints(button, c);
         	panel.add(button);
         	*/
	        
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
	        frame.setTitle("GUI");
	        //frame.pack();
	        frame.setSize(720, 480);
	        frame.setVisible(true);
	    }
	    

	    // process the button clicks
	    public void actionPerformed(ActionEvent e) {
	        String name = textName.getText();
	        String mail = textMail.getText();
	        String page = textPage.getText();
	        String comment = textComment.getText();
	        System.out.println("Trying to add post to DB");
	        
	        String myResString = "";
			try {
				// get connection to db
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/GuestBook?characterEncoding=latin1&useConfigs=maxPerformance","javatester", "password");
				//execute query 
				String insertQuery = "insert into guest_book_entries (name, email, page, comment) values (?, ?, ?, ?)";
				// create the mysql insert preparedstatement
			    PreparedStatement preparedStmt = myConn.prepareStatement(insertQuery);
			    preparedStmt.setString (1, name);
			    preparedStmt.setString (2, mail);
			    preparedStmt.setString   (3, page);
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
			
	        updateResArea(myResString);
	        
	    }
	    //this updates the textarea with database entries
	    public void updateResArea(String text) {
	    	resArea.setText(text);
	    }
	}
