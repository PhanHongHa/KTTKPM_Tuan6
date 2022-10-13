package com.example;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.BasicConfigurator;

public class Client2 extends JFrame{
	private List<String> messages;
	public Client2() throws NamingException, JMSException {
		messages = new ArrayList();
		JFrame frame = new JFrame("Chat Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);
		JPanel panel = new JPanel(); 
		JLabel label = new JLabel("Enter Text");
		final JTextField tf = new JTextField(10); 
		JButton send = new JButton("Send");
		final JTextArea ta = new JTextArea();
		
		// thiết lập môi trường cho JMS logging
		BasicConfigurator.configure();
		// thiết lập môi trường cho JJNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		// tạo context
		Context ctx = new InitialContext(settings);
		// lookup JMS connection factory
		Object obj = ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
		// tạo connection
		final Connection con = factory.createConnection("admin", "admin");
		// nối đến MOM
		con.start();
		// tạo session
		final Session session = con.createSession(/* transaction */false, /* ACK */Session.AUTO_ACKNOWLEDGE);
		Destination destination = (Destination) ctx.lookup("dynamicTopics/phandinhnhat");
		MessageConsumer receiver = session.createConsumer(destination);
		// receiver.receive();//blocked method
		// Cho receiver lắng nghe trên queue, chừng có message thì notify
		receiver.setMessageListener(new MessageListener() {

			// có message đến queue, phương thức này được thực thi
			public void onMessage(Message msg) {// msg là message nhận được
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;
						String txt = tm.getText();
//						ta.setText(String.format("B: %s" + "\n",  txt));
						ta.append(String.format("B: %s" + "\n",  txt));
						msg.acknowledge();// gửi tín hiệu ack
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		// tạo producer
		final MessageProducer producer = session.createProducer(destination);
		
		
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Message msg;
				try {
					String message = tf.getText();
					messages.add(message);
					msg = session.createTextMessage(message);
					// gửi
					producer.send(msg);
					ta.append(String.format("A: %s" + "\n",  message));
					tf.setText("");
					
				} catch (JMSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		JButton reset = new JButton("Reset");
		
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tf.setText("");
				
			}
		});
		
		panel.add(label); // Components Added using Flow Layout
		panel.add(tf);
		panel.add(send);
		panel.add(reset);
		// Text Area at the Center
	
		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
//		frame.getContentPane().add(BorderLayout.NORTH, mb);
		frame.getContentPane().add(BorderLayout.CENTER, ta);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws NamingException, JMSException {
		new Client2().setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}


}
