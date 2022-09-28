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
	String frameTitle = "채팅 클라이언트";
	JTextArea incoming;			// 수신된 메시지를 출력하는 곳
    JTextArea outgoing;			// 송신할 메시지를 작성하는 곳
    JList counterParts;			// 현재 로그인한 채팅 상대목록을 나타내는 리스트.
    ObjectInputStream reader;	// 수신용 스트림
    ObjectOutputStream writer;	// 송신용 스트림
    Socket sock;				// 서버 연결용 소켓
    String user;				// 이 클라이언트로 로그인 한 유저의 이름
    JButton logButton;			// 토글이 되는 로그인/로그아웃 버튼
    JScrollPane qScroller;
    CardGame game;
    
    public static void main(String[] args) {
       GameClient client = new GameClient();
       client.go();
    }

    private void go() {
       // build GUI
	   	frame = new JFrame(frameTitle + " : 로그인하세요");
	   	game = new CardGame();

	   	// 메시지 디스플레이 창
	   	incoming = new JTextArea(10,20);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 대화 상대 목록. 초기에는 "전체" - ChatMessage.ALL 만 있음
        String[] list = {GameMessage.ALL};
        counterParts = new JList(list);
        JScrollPane cScroller = new JScrollPane(counterParts);
        cScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
     
        counterParts.setVisibleRowCount(5);
        counterParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        counterParts.setFixedCellWidth(100);
        
        // 메시지 전송을 위한 버튼
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        
        // 메시지 디스플레이 창  
	   	outgoing = new JTextArea(2,20);
	   	outgoing.addKeyListener(new EnterKeyListener());
	   	outgoing.setLineWrap(true);
	   	outgoing.setWrapStyleWord(true);
	   	outgoing.setEditable(true);
	   	
        JScrollPane oScroller = new JScrollPane(outgoing);
        oScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        oScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 로그인과 아웃을 담당하는 버튼. 처음에는 Login 이었다가 일단 로그인 되고나면 Logout으로 바뀜
        logButton = new JButton("Login");
        logButton.addActionListener(new LogButtonListener());

	   	// GUI 배치
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
        
        userPanel.add(new JLabel("대화상대목록"));
        userPanel.add(Box.createRigidArea(new Dimension(0,5)));
        userPanel.setPreferredSize(new Dimension(100,200));	
        userPanel.add(cScroller);

        inputPanel.add(new JLabel("메시지입력"));
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
        
        // 네트워킹을 시동하고, 서버에서 메시지를 읽을 스레드 구동
        setUpNetworking();
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
          
        // 클라이언드 프레임 창 조정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.EAST, mainPanel);
        frame.getContentPane().add(BorderLayout.CENTER, game);
        frame.setSize(1400,700);

        frame.setVisible(true);

        // 프레임이 살아 있으므로 여기서 만들은 스레드는 계속 진행 됨
        // 이 프레임 스레드를 종료하면, 이 프레임에서 만든 스레드들은 예외를 발생하게되고
        // 이를 이용해 모든 스레드를 안전하게 종료 시키도록 함
     } // close go

   private void setUpNetworking() {  
	   try {
		   sock = new Socket("127.0.0.1", 9270);			// 소켓 통신을 위한 포트는 9270번 사용키로 함 
		   reader = new ObjectInputStream(sock.getInputStream());
		   writer = new ObjectOutputStream(sock.getOutputStream());
		   game.writer=writer;
		   game.reader=reader;
	   } catch(Exception ex) {
		   JOptionPane.showMessageDialog(null, "서버접속에 실패하였습니다. 접속을 종료합니다.");
           ex.printStackTrace();
           frame.dispose();		// 네트워크가 초기 연결 안되면 클라이언트 강제 종료
	   }
   } // close setUpNetworking   

   // 로그인과 아웃을 담당하는 버튼의 감청자. 처음에는 Login 이었다가 일단 로그인 되고나면 Logout을 처리
   private class LogButtonListener implements ActionListener {
      public void actionPerformed(ActionEvent ev) {
    	  if (logButton.getText().equals("Login")) {
    		  processLogin();
    		  logButton.setText("Logout");
    	  }
    	  else
    		  processLogout();
      }
      // 로그인 처리
      private void processLogin() {
    	  user = JOptionPane.showInputDialog("사용자 이름을 입력하세요");
    	  try {
       		  writer.writeObject(new GameMessage(GameMessage.MsgType.LOGIN, user, "", ""));
              writer.flush();
              frame.setTitle(frameTitle + " (로그인 : " + user + ")");
       	  } catch(Exception ex) {
       		  JOptionPane.showMessageDialog(null, "로그인 중 서버접속에 문제가 발생하였습니다.");
       		  ex.printStackTrace();
       	  }
       }
      // 로그아웃 처리
      private void processLogout() {
   	   	  int choice = JOptionPane.showConfirmDialog(null, "Logout합니다");
   	   	  if (choice == JOptionPane.YES_OPTION) {
   	   		  try {
   	             writer.writeObject(new GameMessage(GameMessage.MsgType.LOGOUT, user, "", ""));
   	             writer.flush();
   	             // 연결된 모든 스트림과 소켓을 닫고 프로그램을 종료 함
  	             writer.close(); reader.close(); sock.close();
   	   		  } catch(Exception ex) {
   	       		  JOptionPane.showMessageDialog(null, "로그아웃 중 서버접속에 문제가 발생하였습니다. 강제종료합니다");
   	        	  ex.printStackTrace();
   	          } finally {
   	             System.exit(100);			// 클라이언트 완전 종료 
   	          }
   	   	  }
   	   }
   }  // close LoginButtonListener inner class

   public class SendButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent ev) {
	   	  String to = (String) counterParts.getSelectedValue();
	   	  if (to == null) {
	   		  JOptionPane.showMessageDialog(null, "송신할 대상을 선택한 후 메시지를 보내세요");
	   		  return;
	   	  }
	   	  try {
       		  incoming.append(user + " : " + outgoing.getText() + "\n"); // 나의 메시지 창에 보이기
	   		  writer.writeObject(new GameMessage(GameMessage.MsgType.CLIENT_MSG, user, to, outgoing.getText()));
	   		  writer.flush();
	   		  outgoing.setText("");
	   		  outgoing.requestFocus();
	   	  } catch(Exception ex) {
	   		  JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
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
				   		  JOptionPane.showMessageDialog(null, "송신할 대상을 선택한 후 메시지를 보내세요");
				   		  return;
				   	  }
				   	  try {
			       		  incoming.append(user + " : " + outgoing.getText() + "\n"); // 나의 메시지 창에 보이기
			       		  incoming.setSelectionStart(incoming.getText().length());
			       		  qScroller.getVerticalScrollBar().setValue(qScroller.getVerticalScrollBar().getMaximum());
				   		  writer.writeObject(new GameMessage(GameMessage.MsgType.CLIENT_MSG, user, to, outgoing.getText()));
				   		  writer.flush();
				   		  outgoing.setText("");
				   		  outgoing.requestFocus();
				   	  } catch(Exception ex) {
				   		  JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
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

   // 서버에서 보내는 메시지를 받는 스레드 작업을 정의하는 클래스
   public class IncomingReader implements Runnable {
     public void run() {
    	 GameMessage message;             
         GameMessage.MsgType type;
         String[] users={};
    	 try {
    		 while (true) {
        		 message = (GameMessage) reader.readObject();     	 // 서버 부터의 메시지 대기                   
               	 type = message.getType();
               	 if (type == GameMessage.MsgType.LOGIN_FAILURE) {	 // 로그인이 실패한 경우라면
               		 JOptionPane.showMessageDialog(null, "Login이 실패하였습니다. 다시 로그인하세요");
    	             frame.setTitle(frameTitle + " : 로그인 하세요");
    	             logButton.setText("Login");
               	 } else if (type == GameMessage.MsgType.SERVER_MSG) { // 메시지를 받았다면 보여줌
               		 if (message.getSender().equals(user)) continue;  // 내가 보낸 편지면 보일 필요 없음
               		 incoming.append(message.getSender() + " : " + message.getContents() + "\n");
               		 qScroller.getVerticalScrollBar().setValue(qScroller.getVerticalScrollBar().getMaximum());
               	 } else if (type == GameMessage.MsgType.LOGIN_LIST) {
               		 // 유저 리스트를 추출 해서 counterParts 리스트에 넣어 줌.
               		 // 나는  빼고 (""로 만들어 정렬 후 리스트 맨 앞에 오게 함)
               		 users = message.getContents().split("/");
               		 for (int i=0; i<users.length; i++) {
               			 if (user.equals(users[i]))users[i] = "";
               		 }
               		 users = sortUsers(users);		// 유저 목록을 쉽게 볼 수 있도록 정렬해서 제공
               		 users[0] =  GameMessage.ALL;	// 리스트 맨 앞에 "전체"가 들어가도록 함
               		 counterParts.setListData(users);
               		 counterParts.setSelectedIndex(0);
               		 frame.repaint();
               	 } else if (type == GameMessage.MsgType.NO_ACT){
               		 // 아무 액션이 필요없는 메시지. 그냥 스킵
               	 }
               	 else {
               		 // 정체가 확인되지 않는 이상한 메시지
               		 throw new Exception("서버에서 알 수 없는 메시지 도착했음");
               	 }
    		 } // close while
    	 } catch(Exception ex) {
    		 System.out.println("클라이언트 스레드 종료");		// 프레임이 종료될 경우 이를 통해 스레드 종료
    	 }
     } // close run
     
     // 주어진 String 배열을 정렬한 새로운 배열 리턴
     private String [] sortUsers(String [] users) {
    	 String [] outList = new String[users.length];
    	 ArrayList<String> list = new ArrayList<String>();
    	 for (String s : users) {
    		 list.add(s);
    	 }
    	 Collections.sort(list);				// Collections.sort를 사용해 한방에 정렬
    	 for (int i=0; i<users.length; i++) {
    		 outList[i] = list.get(i);
    	 }
    	 return outList;
     }
  } // close inner class     
}
