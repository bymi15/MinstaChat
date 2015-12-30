package com.minstachat.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextPane txtHistory;
	private StyledDocument doc;
	private SimpleAttributeSet keyword_name;
	private SimpleAttributeSet keyword_msg;
	private SimpleAttributeSet keyword_whisperTo;
	private SimpleAttributeSet keyword_whisperFrom;
	private SimpleAttributeSet keyword_server;
	private SimpleAttributeSet keyword_admin;
	private JTextField txtMessage;
	private DefaultCaret caret;
	private JList<String> listUsers;
	private String lastWhisper = "";
	
	private String name;
	private List<String> users = new ArrayList<String>();
	
	private BufferedReader inputStream;
	private PrintStream outputStream;
	private InputStreamReader isr;
	
	private Socket socket;
	private InetAddress ip;
	private Thread listenThread, sendThread;
	private volatile boolean running = false; //flag
	private JScrollPane scrollPane;
	
	/**
	 * Constructor
	 */
	public Client(String name, String address, int port) {
		this.name = name;
		initialise();
		console("Attempting to connect to " + address + ":" + port + "...", 4);
		boolean connection = openConnection(address, port);
		if(connection){
			running = true;
			console("**********Successfully Connected!**********", 4);
			console("<SERVER> <" + name + "> has logged in to " + address + ":" + port, 1);
			try {
				isr = new InputStreamReader(socket.getInputStream());
				inputStream = new BufferedReader(isr);
				outputStream = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
			console("Could not connect to server...", 4);
			console("Restart the program to reconnect.", 4);
		}
		send(name); //Sends the username to the server	
		receive();
		txtMessage.requestFocusInWindow();
	}
	
	/**
	 * Initialise GUI
	 */
	private void initialise(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				try {
						running = false;
						listenThread.interrupt();
						sendThread.interrupt();
						if(outputStream != null && inputStream != null && socket != null){
						outputStream.println("/logout");
						inputStream.close();
						outputStream.close();
						socket.close();
					}
					dispose();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		setTitle("Minsta Chat Client");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 545);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(12, 22, 708, 446);
		contentPane.add(scroll);
		
		//Main chatbox GUI
		txtHistory = new JTextPane();
		txtHistory.setEditable(false);
		scroll.setViewportView(txtHistory);
		caret = (DefaultCaret) txtHistory.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		txtHistory.setCaret(caret);
		keyword_msg = new SimpleAttributeSet();
		Font f = new Font("Calibri Light", Font.PLAIN, 16);
		StyleConstants.setForeground(keyword_msg, Color.black);
		StyleConstants.setBackground(keyword_msg, Color.white);
		StyleConstants.setFontFamily(keyword_msg, f.getFamily());
		StyleConstants.setFontSize(keyword_msg, f.getSize());
		doc = txtHistory.getStyledDocument();
		doc.setCharacterAttributes(0, doc.getLength() + 1, keyword_msg, false);
		keyword_name = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword_name, Color.blue);
		StyleConstants.setBackground(keyword_name, Color.white);
		StyleConstants.setBold(keyword_name, true);
		StyleConstants.setFontFamily(keyword_name, f.getFamily());
		StyleConstants.setFontSize(keyword_name, f.getSize());
		keyword_server = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword_server, new Color(21, 179, 35));
		StyleConstants.setBackground(keyword_server, Color.white);
		StyleConstants.setBold(keyword_server, true);
		StyleConstants.setFontFamily(keyword_server, f.getFamily());
		StyleConstants.setFontSize(keyword_server, f.getSize());
		keyword_admin = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword_admin, Color.red);
		StyleConstants.setBackground(keyword_admin, Color.white);
		StyleConstants.setBold(keyword_admin, true);
		StyleConstants.setFontFamily(keyword_admin, f.getFamily());
		StyleConstants.setFontSize(keyword_admin, f.getSize());
		keyword_whisperTo = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword_whisperTo, new Color(252, 162, 35));
		StyleConstants.setBold(keyword_whisperTo, true);
		StyleConstants.setBackground(keyword_whisperTo, Color.white);
		StyleConstants.setFontFamily(keyword_whisperTo, f.getFamily());
		StyleConstants.setFontSize(keyword_whisperTo, f.getSize());
		keyword_whisperFrom = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword_whisperFrom, new Color(44, 183, 230));
		StyleConstants.setBold(keyword_whisperFrom, true);
		StyleConstants.setBackground(keyword_whisperFrom, Color.white);
		StyleConstants.setFontFamily(keyword_whisperFrom, f.getFamily());
		StyleConstants.setFontSize(keyword_whisperFrom, f.getSize());
		
		txtHistory.setBorder(BorderFactory.createLineBorder(Color.gray));
		
		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					sendMessage(txtMessage.getText());
				}
				if(e.getKeyCode() == KeyEvent.VK_SPACE){
					if(lastWhisper.equals("")) return;
					if(txtMessage.getText().startsWith("/r")){
						int index = -1;
						String username;
						index = lastWhisper.indexOf("whispered") - 1;
						if(index == -1) return;
						username = lastWhisper.substring(0, index);
						txtMessage.setText("@" + username + " ");
					}
				}
			}
		});
		txtMessage.setBounds(12, 473, 872, 33);
		txtMessage.setBorder(new LineBorder(Color.GRAY));
		txtMessage.setBorder(BorderFactory.createCompoundBorder(
		        txtMessage.getBorder(), 
		        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		txtMessage.setFont(new Font("Gill Sans Nova", Font.PLAIN, 16));
		contentPane.add(txtMessage);
		txtMessage.setColumns(10);
		scrollPane = new JScrollPane();
		scrollPane.setBounds(725, 22, 159, 446);
		contentPane.add(scrollPane);
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		listUsers = new JList<String>(dlm);
		listUsers.setVisibleRowCount(-1);
		listUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listUsers.setBackground(new Color(255, 255, 255));
		listUsers.setBorder(UIManager.getBorder("List.noFocusBorder"));
		listUsers.setSelectedIndex(-1);
		listUsers.setBounds(717, 460, 177, -434);
		scrollPane.setViewportView(listUsers);
		JLabel lblOnlineUsers = new JLabel("Online Users:");
		lblOnlineUsers.setBounds(730, 4, 154, 14);
		contentPane.add(lblOnlineUsers);
		
		
		listUsers.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        @SuppressWarnings("unchecked")
				JList<String> listUsers = (JList<String>) e.getSource();
		        if (e.getClickCount() == 2 && listUsers.getModel().getSize() != 0) {
		            int index = listUsers.locationToIndex(e.getPoint());
		            ListModel<String> lm = listUsers.getModel();
		            String usr = (String) lm.getElementAt(index);
		            txtMessage.setText("@" + usr + " ");
		            listUsers.ensureIndexIsVisible(index);
		            txtMessage.requestFocusInWindow();
		        }
		    }
		});
		setVisible(true);
	}

	/**
	 * Console - prints to chat history:
	 */
	
	public void console(String message, int type){
		if(type == 0){
			printUserMessage(message);
		}
		else if(type == 1){
			printServerMessage(message);
		}
		else if(type == 2){
			printAdminMessage(message);
		}
		else if(type == 3){
			printWhisper(message);
		}
		else if(type == 4){
			printCommand(message);
		}
	}
	
	private void printUserMessage(String message){
		if(message.equals(null)) return;
		int start = message.indexOf(">") + 1;
		int startName = message.indexOf("<", start);
		int endName = message.indexOf(">", startName);
		
		String name = message.substring(startName, endName + 1);
		String msg = message.substring(endName + 1);
		try {
			doc.insertString(doc.getLength(), name + " ", keyword_name);
			doc.insertString(doc.getLength(), msg + "\n", keyword_msg);
			txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void printCommand(String message){
		if(message.equals(null)) return;
		try{
			doc.insertString(doc.getLength(), message + "\n", keyword_server);
			txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void printServerMessage(String message){
		if(message.equals(null)) return;
		int start = message.indexOf("<");
		int end = message.indexOf(">");
		String name = message.substring(start, end + 1);
		String msg = message.substring(end + 1);
		try{
			doc.insertString(doc.getLength(), name + " ", keyword_server);
			doc.insertString(doc.getLength(), msg + "\n", keyword_msg);
			txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void printAdminMessage(String message){
		if(message.equals(null)) return;
		int start = message.indexOf(">") + 1;
		int startName = message.indexOf("<", start);
		int endName = message.indexOf(">", startName);
		
		String name = message.substring(startName, endName + 1);
		String msg = message.substring(endName + 1);
		try{
			doc.insertString(doc.getLength(), name + " ", keyword_admin);
			doc.insertString(doc.getLength(), msg + "\n", keyword_msg);
			txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void printWhisper(String message){
		if(message.equals(null)) return;
		int startIndex = message.indexOf(">") + 2;
		if(message.indexOf("You") == startIndex){
			try{
				doc.insertString(doc.getLength(), message.substring(startIndex) + "\n", keyword_whisperTo);
				txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}else{
			lastWhisper = message.substring(startIndex);
			try{
				doc.insertString(doc.getLength(), message.substring(startIndex) + "\n", keyword_whisperFrom);
				txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public String getTime(){
		String time = new SimpleDateFormat("[yyyy/MM/dd](HH:mm)").format(Calendar.getInstance().getTime());
		return time;
	}
	
	private boolean openConnection(String address, int port){
		try {
			ip = InetAddress.getByName(address);
			socket = new Socket(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void sendMessage(String message){
		if(message.trim().equals("")) return;
		
		if(getMessageType(message).equals("pm")){
			send(message);
			txtMessage.setText("");
			txtMessage.requestFocusInWindow();
		}else if(getMessageType(message).equals("command")){ //type: command
			send(message);
			txtMessage.setText("");
			txtMessage.requestFocusInWindow();
			int index = message.indexOf(" ");
			if(index > 0){
				String command = message.substring(1, index);
				console("<COMMAND> " + command + " requested to server.", 4);
				if(command.equals("logout")){
					Login frame = new Login();
					frame.setVisible(true);
					dispose();
				}
				if(command.equals("exit")){
					System.exit(0);
				}
			}
			else{
				console("<COMMAND> " + message.substring(1) + " requested to server.", 4);
			}
		}else{
			send(message);
			console("<USERMESSAGE> <" + name + "> " + message, 0);
			txtMessage.setText("");
			txtMessage.requestFocusInWindow();
		}
	}
	
	private void send(final String message){
		sendThread = new Thread("send"){
			public void run(){
				outputStream.println(message);
			}
		};
		sendThread.start();
	}
	private void receive(){
		listenThread = new Thread("listen"){
			public void run(){
				while(running){
					try {
						String s = inputStream.readLine();
						if(s != null && s != ""){
							if(getMessageType(s).equals("userlist")) getUserList(s);
							else if(getMessageType(s).equals("whisper")) console(s, 3);
							else if(getMessageType(s).equals("server")) console(s, 1);
							else if(getMessageType(s).equals("admin")) console(s, 2);
							else if(getMessageType(s).equals("command")) parseCommand(s);
							else console(s, 0);
						}
					} catch (IOException e) {
						console("Disconnected from server...", 4);
						console("Restart the client to reconnect.", 4);
						running = false;
					}
				}
			}
		};
		listenThread.start();
	}
	
	private String getMessageType(String msg){
		if(msg.startsWith("@")) return "pm";
		if(msg.startsWith("/")) return "command";
		if(msg.startsWith("|") && msg.endsWith("|")) return "userlist";
		if(msg.startsWith("<USERMESSAGE>")) return "message";
		if(msg.startsWith("<WHISPER>")) return "whisper";
		if(msg.startsWith("<SERVER>")) return "server";
		if(msg.startsWith("<ADMIN>")) return "admin";
		return "message";
	}
	
	private void getUserList(String str){ //decodes string containing users and adds it to users array list
		if(!str.startsWith("|") || !str.endsWith("|") || str.equals("|") || str.equals(null)) return;
		String s = str.substring(1, str.length() - 1); //eg: from "|user1|user2|user3|" to "user1|user2|user3"
		String[] arr = s.split("\\|"); //eg: from "user1|user2|user3" to "user1", "user2", "user3"
		users.clear();
		for(String usr : arr){
			users.add(usr); //adds each string in the array to the users array list
		}
		//Update GUI listbox
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		for(int i = 0; i < users.size(); i++){
			dlm.addElement(users.get(i));
		}
		updateListGUI(dlm);
	}
	
	private void updateListGUI(DefaultListModel<String> dlm){
		listUsers.setModel(dlm);
	}
	
	private void parseCommand(String s){
		if(s.startsWith("/disconnect")){
			disconnect();
		}
	}
	
	private void disconnect(){
		try {
			outputStream.close();
			inputStream.close();
			isr.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*for testing purposes
	private void msgbox(String message, String title, int type){ 
		//type: ERROR_MESSAGE = 0, INFORMATION_MESSAGE = 1
		JOptionPane.showMessageDialog(null, message, title, type);
	}
	*/
}
