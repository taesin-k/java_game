import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import java.applet.Applet;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;

public class GameClient {
	JFrame frame;
	String frameTitle = "ä�� Ŭ���̾�Ʈ";
	JTextArea incoming;			// ���ŵ� �޽����� ����ϴ� ��
    JTextArea outgoing;			// �۽��� �޽����� �ۼ��ϴ� ��
    JList counterParts;			// ���� �α����� ä�� ������� ��Ÿ���� ����Ʈ.
    ObjectInputStream reader;	// ���ſ� ��Ʈ��
    ObjectOutputStream writer;	// �۽ſ� ��Ʈ��
    Socket sock;				// ���� ����� ����
    String user;				// �� Ŭ���̾�Ʈ�� �α��� �� ������ �̸�
    JButton logButton;			// ����� �Ǵ� �α���/�α׾ƿ� ��ư
    JScrollPane qScroller;
    CardGame game;
    
    public static void main(String[] args) {
       GameClient client = new GameClient();
       client.go();
    }

    private void go() {
       // build GUI
	   	frame = new JFrame(frameTitle + " : �α����ϼ���");
	   	game = new CardGame();

	   	// �޽��� ���÷��� â
	   	incoming = new JTextArea(10,20);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // ��ȭ ��� ���. �ʱ⿡�� "��ü" - ChatMessage.ALL �� ����
        String[] list = {GameMessage.ALL};
        counterParts = new JList(list);
        JScrollPane cScroller = new JScrollPane(counterParts);
        cScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
     
        counterParts.setVisibleRowCount(5);
        counterParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        counterParts.setFixedCellWidth(100);
        
        // �޽��� ������ ���� ��ư
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        
        // �޽��� ���÷��� â  
	   	outgoing = new JTextArea(2,20);
	   	outgoing.addKeyListener(new EnterKeyListener());
	   	outgoing.setLineWrap(true);
	   	outgoing.setWrapStyleWord(true);
	   	outgoing.setEditable(true);
	   	
        JScrollPane oScroller = new JScrollPane(outgoing);
        oScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        oScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // �α��ΰ� �ƿ��� ����ϴ� ��ư. ó������ Login �̾��ٰ� �ϴ� �α��� �ǰ��� Logout���� �ٲ�
        logButton = new JButton("Login");
        logButton.addActionListener(new LogButtonListener());

	   	// GUI ��ġ
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
        upperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
        lowerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,2));
        
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());
        
        userPanel.add(new JLabel("��ȭ�����"));
        userPanel.add(Box.createRigidArea(new Dimension(0,5)));
        userPanel.setPreferredSize(new Dimension(100,200));	
        userPanel.add(cScroller);

        inputPanel.add(new JLabel("�޽����Է�"));
        inputPanel.add(Box.createRigidArea(new Dimension(0,5)));
        inputPanel.add(oScroller);
        
        buttonPanel.add(sendButton);
        buttonPanel.add(logButton);
        
        sendPanel.add(BorderLayout.CENTER, inputPanel);
        sendPanel.add(BorderLayout.SOUTH, buttonPanel);

        lowerPanel.add(userPanel);
        lowerPanel.add(Box.createRigidArea(new Dimension(5,0)));
        lowerPanel.add(sendPanel);
        lowerPanel.add(Box.createRigidArea(new Dimension(5,0)));
        

        upperPanel.add(qScroller);
        
        mainPanel.add(upperPanel);
        mainPanel.add(lowerPanel);
        
        // ��Ʈ��ŷ�� �õ��ϰ�, �������� �޽����� ���� ������ ����
        setUpNetworking();
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
          
        // Ŭ���̾�� ������ â ����
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.EAST, mainPanel);
        frame.getContentPane().add(BorderLayout.CENTER, game);
        frame.setSize(1400,700);

        frame.setVisible(true);

        // �������� ��� �����Ƿ� ���⼭ ������ ������� ��� ���� ��
        // �� ������ �����带 �����ϸ�, �� �����ӿ��� ���� ��������� ���ܸ� �߻��ϰԵǰ�
        // �̸� �̿��� ��� �����带 �����ϰ� ���� ��Ű���� ��
     } // close go

   private void setUpNetworking() {  
	   try {
		   sock = new Socket("127.0.0.1", 9270);			// ���� ����� ���� ��Ʈ�� 9270�� ���Ű�� �� 
		   reader = new ObjectInputStream(sock.getInputStream());
		   writer = new ObjectOutputStream(sock.getOutputStream());
		   game.writer=writer;
		   game.reader=reader;
	   } catch(Exception ex) {
		   JOptionPane.showMessageDialog(null, "�������ӿ� �����Ͽ����ϴ�. ������ �����մϴ�.");
           ex.printStackTrace();
           frame.dispose();		// ��Ʈ��ũ�� �ʱ� ���� �ȵǸ� Ŭ���̾�Ʈ ���� ����
	   }
   } // close setUpNetworking   

   // �α��ΰ� �ƿ��� ����ϴ� ��ư�� ��û��. ó������ Login �̾��ٰ� �ϴ� �α��� �ǰ��� Logout�� ó��
   private class LogButtonListener implements ActionListener {
      public void actionPerformed(ActionEvent ev) {
    	  if (logButton.getText().equals("Login")) {
    		  processLogin();
    		  logButton.setText("Logout");
    	  }
    	  else
    		  processLogout();
      }
      // �α��� ó��
      private void processLogin() {
    	  user = JOptionPane.showInputDialog("����� �̸��� �Է��ϼ���");
    	  try {
       		  writer.writeObject(new GameMessage(GameMessage.MsgType.LOGIN, user, "", ""));
              writer.flush();
              frame.setTitle(frameTitle + " (�α��� : " + user + ")");
       	  } catch(Exception ex) {
       		  JOptionPane.showMessageDialog(null, "�α��� �� �������ӿ� ������ �߻��Ͽ����ϴ�.");
       		  ex.printStackTrace();
       	  }
       }
      // �α׾ƿ� ó��
      private void processLogout() {
   	   	  int choice = JOptionPane.showConfirmDialog(null, "Logout�մϴ�");
   	   	  if (choice == JOptionPane.YES_OPTION) {
   	   		  try {
   	             writer.writeObject(new GameMessage(GameMessage.MsgType.LOGOUT, user, "", ""));
   	             writer.flush();
   	             // ����� ��� ��Ʈ���� ������ �ݰ� ���α׷��� ���� ��
  	             writer.close(); reader.close(); sock.close();
   	   		  } catch(Exception ex) {
   	       		  JOptionPane.showMessageDialog(null, "�α׾ƿ� �� �������ӿ� ������ �߻��Ͽ����ϴ�. ���������մϴ�");
   	        	  ex.printStackTrace();
   	          } finally {
   	             System.exit(100);			// Ŭ���̾�Ʈ ���� ���� 
   	          }
   	   	  }
   	   }
   }  // close LoginButtonListener inner class

   public class SendButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent ev) {
	   	  String to = (String) counterParts.getSelectedValue();
	   	  if (to == null) {
	   		  JOptionPane.showMessageDialog(null, "�۽��� ����� ������ �� �޽����� ��������");
	   		  return;
	   	  }
	   	  try {
       		  incoming.append(user + " : " + outgoing.getText() + "\n"); // ���� �޽��� â�� ���̱�
	   		  writer.writeObject(new GameMessage(GameMessage.MsgType.CLIENT_MSG, user, to, outgoing.getText()));
	   		  writer.flush();
	   		  outgoing.setText("");
	   		  outgoing.requestFocus();
	   	  } catch(Exception ex) {
	   		  JOptionPane.showMessageDialog(null, "�޽��� ������ ������ �߻��Ͽ����ϴ�.");
	      	  ex.printStackTrace();
	      }
	   }
   }  // close SendButtonListener inner class
   
   public class EnterKeyListener implements KeyListener{
	   boolean presscheck=false;
	@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			if(e.getKeyCode()==KeyEvent.VK_SHIFT){
				presscheck = true;
			}
			else if(e.getKeyCode()==KeyEvent.VK_ENTER){
				 if(presscheck == true){
					String str = outgoing.getText() +"\r\n";
					outgoing.setText(str);
					presscheck = false;
				 }
				 else{
					e.consume();
					presscheck = false;
					String to = (String) counterParts.getSelectedValue();
				   	  if (to == null) {
				   		  JOptionPane.showMessageDialog(null, "�۽��� ����� ������ �� �޽����� ��������");
				   		  return;
				   	  }
				   	  try {
			       		  incoming.append(user + " : " + outgoing.getText() + "\n"); // ���� �޽��� â�� ���̱�
			       		  incoming.setSelectionStart(incoming.getText().length());
			       		  qScroller.getVerticalScrollBar().setValue(qScroller.getVerticalScrollBar().getMaximum());
				   		  writer.writeObject(new GameMessage(GameMessage.MsgType.CLIENT_MSG, user, to, outgoing.getText()));
				   		  writer.flush();
				   		  outgoing.setText("");
				   		  outgoing.requestFocus();
				   	  } catch(Exception ex) {
				   		  JOptionPane.showMessageDialog(null, "�޽��� ������ ������ �߻��Ͽ����ϴ�.");
				      	  ex.printStackTrace();
				      }
				 }
			 }
		}
	
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_SHIFT){
				presscheck = false;
			}
		}
	
		@Override
		public void keyTyped(KeyEvent e) {
	   }
   }

   // �������� ������ �޽����� �޴� ������ �۾��� �����ϴ� Ŭ����
   public class IncomingReader implements Runnable {
     public void run() {
    	 GameMessage message;             
         GameMessage.MsgType type;
         String[] users={};
    	 try {
    		 while (true) {
        		 message = (GameMessage) reader.readObject();     	 // ���� ������ �޽��� ���                   
               	 type = message.getType();
               	 if (type == GameMessage.MsgType.LOGIN_FAILURE) {	 // �α����� ������ �����
               		 JOptionPane.showMessageDialog(null, "Login�� �����Ͽ����ϴ�. �ٽ� �α����ϼ���");
    	             frame.setTitle(frameTitle + " : �α��� �ϼ���");
    	             logButton.setText("Login");
               	 } else if (type == GameMessage.MsgType.SERVER_MSG) { // �޽����� �޾Ҵٸ� ������
               		 if (message.getSender().equals(user)) continue;  // ���� ���� ������ ���� �ʿ� ����
               		 incoming.append(message.getSender() + " : " + message.getContents() + "\n");
               		 qScroller.getVerticalScrollBar().setValue(qScroller.getVerticalScrollBar().getMaximum());
               	 } else if (type == GameMessage.MsgType.LOGIN_LIST) {
               		 // ���� ����Ʈ�� ���� �ؼ� counterParts ����Ʈ�� �־� ��.
               		 // ����  ���� (""�� ����� ���� �� ����Ʈ �� �տ� ���� ��)
               		 users = message.getContents().split("/");
               		 for (int i=0; i<users.length; i++) {
               			 if (user.equals(users[i]))users[i] = "";
               		 }
               		 users = sortUsers(users);		// ���� ����� ���� �� �� �ֵ��� �����ؼ� ����
               		 users[0] =  GameMessage.ALL;	// ����Ʈ �� �տ� "��ü"�� ������ ��
               		 counterParts.setListData(users);
               		 counterParts.setSelectedIndex(0);
               		 frame.repaint();
               	 } else if (type == GameMessage.MsgType.NO_ACT){
               		 // �ƹ� �׼��� �ʿ���� �޽���. �׳� ��ŵ
               	 }
               	 else {
               		 // ��ü�� Ȯ�ε��� �ʴ� �̻��� �޽���
               		 throw new Exception("�������� �� �� ���� �޽��� ��������");
               	 }
    		 } // close while
    	 } catch(Exception ex) {
    		 System.out.println("Ŭ���̾�Ʈ ������ ����");		// �������� ����� ��� �̸� ���� ������ ����
    	 }
     } // close run
     
     // �־��� String �迭�� ������ ���ο� �迭 ����
     private String [] sortUsers(String [] users) {
    	 String [] outList = new String[users.length];
    	 ArrayList<String> list = new ArrayList<String>();
    	 for (String s : users) {
    		 list.add(s);
    	 }
    	 Collections.sort(list);				// Collections.sort�� ����� �ѹ濡 ����
    	 for (int i=0; i<users.length; i++) {
    		 outList[i] = list.get(i);
    	 }
    	 return outList;
     }
  } // close inner class     
}
