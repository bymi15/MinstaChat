package com.minstachat.client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.border.LineBorder;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField txtName;
	private JLabel lblAddress;
	private JTextField txtAddress;
	private JTextField txtPort;
	private JLabel lblPort;
	private JLabel lblMinstachat;
	private JButton btnExit;

	private String name, address;
	private int port;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login(); //Instantiate object
					frame.setVisible(true); //Show the form
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Input validation method
	 */
	private boolean valid(String name, String address, String port){
		if(name == null || name == "" || name.length() > 20) return false;
		String symbols = "!@#$%^&*()`~-=+.,/?;:}{[]|/";
		for(int i = 0; i<symbols.length() - 1; i++){
			String s = String.valueOf(symbols.charAt(i));
			if(name.contains(s)) return false;
		}
		if(address.equals(0) || address == null || address == "") return false;
		if(port.equals(0) || port == null || port == "" || Integer.parseInt(port) < 1024) return false;
		
		return true;
	}
	
	/**
	 * Display messagebox method
	 */
	private void msgbox(String message, String title, int type){ 
		//type: ERROR_MESSAGE = 0, INFORMATION_MESSAGE = 1
		JOptionPane.showMessageDialog(null, message, title, type);
	}
	
	/**
	 * Login method
	 */
	private void login(String name, String address, int port){
		dispose(); //Closes current form
		new Client(name, address, port); //Instantiates client object
	}
	
	/**
	 * Constructor - initialise GUI
	 */
	public Login() {
		setBackground(new Color(224, 255, 255));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		setTitle("Minsta Chat - Login");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(328, 352);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(Color.GRAY));
		panel_1.setBackground(Color.GRAY);
		panel_1.setBounds(23, 11, 272, 38);
		contentPane.add(panel_1);
		
		lblMinstachat = new JLabel("Minsta-Chat");
		panel_1.add(lblMinstachat);
		lblMinstachat.setForeground(new Color(0, 0, 0));
		lblMinstachat.setFont(new Font("Gill Sans Nova", Font.PLAIN, 20));
		
		JPanel panel = new JPanel();
		panel.setBorder(UIManager.getBorder("ComboBox.border"));
		panel.setBackground(Color.DARK_GRAY);
		panel.setBounds(23, 72, 272, 228);
		contentPane.add(panel);
		panel.setLayout(null);
		
		lblAddress = new JLabel("IP Address:");
		lblAddress.setForeground(Color.WHITE);
		lblAddress.setBounds(34, 70, 89, 14);
		panel.add(lblAddress);
		
		lblPort = new JLabel("Port:");
		lblPort.setForeground(Color.WHITE);
		lblPort.setBounds(34, 114, 89, 14);
		panel.add(lblPort);
		
		JLabel lblNickname = new JLabel("Nickname:");
		lblNickname.setForeground(Color.WHITE);
		lblNickname.setBounds(34, 25, 89, 14);
		panel.add(lblNickname);
		
		txtName = new JTextField();
		txtName.setText("Jack");
		txtName.setToolTipText("eg. John");
		txtName.setBounds(34, 39, 203, 20);
		panel.add(txtName);
		txtName.setColumns(10);
		
		txtAddress = new JTextField();
		txtAddress.setText("localhost");
		txtAddress.setToolTipText("eg. 192.168.1.1");
		txtAddress.setBounds(34, 83, 203, 20);
		panel.add(txtAddress);
		txtAddress.setColumns(10);
		
		txtPort = new JTextField();
		txtPort.setText("5678");
		txtPort.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					if(valid(txtName.getText(), txtAddress.getText(), txtPort.getText())){
						name = txtName.getText();
						address = txtAddress.getText();
						port = Integer.parseInt(txtPort.getText());
						login(name, address, port);
					}
					else {
						msgbox("Invalid input. Please re-enter the information.", "An error has occured!", 0); //ERROR_MESSAGE = 0, INFORMATION_MESSAGE = 1
						txtName.setText("");
						txtAddress.setText("");
						txtPort.setText("");
					}
				}
			}
		});
		txtPort.setToolTipText("eg. 4995");
		txtPort.setBounds(34, 127, 203, 20);
		panel.add(txtPort);
		txtPort.setColumns(10);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(valid(txtName.getText(), txtAddress.getText(), txtPort.getText())){
					name = txtName.getText();
					address = txtAddress.getText();
					port = Integer.parseInt(txtPort.getText());
					login(name, address, port);
				}
				else {
					msgbox("Invalid input. Please re-enter the information.", "An error has occured!", 0); //ERROR_MESSAGE = 0, INFORMATION_MESSAGE = 1
					txtName.setText("");
					txtAddress.setText("");
					txtPort.setText("");
				}
			}
		});
		btnLogin.setBounds(34, 170, 89, 36);
		panel.add(btnLogin);
		
		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		btnExit.setBounds(148, 170, 89, 36);
		panel.add(btnExit);
	}
}
