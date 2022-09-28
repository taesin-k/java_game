import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CardGame extends JPanel {
	final String GAME_CARD[] = { "src/res/그루트.jpg", "src/res/데드풀.jpg",
			"src/res/배트맨.jpg", "src/res/베놈.jpg", "src/res/스파이더맨.jpg",
			"src/res/아이언맨.jpg", "src/res/앤트맨.jpg", "src/res/블랙펜서.jpg" };
	final String START_CARD = "src/res/Marvel_Comics.jpg";
	final int[] STAGE_CARD_NUM = { 8, 12, 16 }; // 스테이지별 카드 갯수
	int total_card; //카드 개수
	int total_click; //클릭 횟수

	int level = 0; // 스테이지
	GamePanel gamePanel; //게임하는 패널 생성

	ArrayList<Card> cards; // 카드 버튼 리스트
	ArrayList<Integer> cardCountList; //카드 종류 별로 개수 세는 리스트 (2개짝을 맞춰주는 용도)
	boolean clicked = false;	//클릭 유무
	int click_card_number; //클릭한 카드의 위치
	
	private boolean turnCheck=false;
	private boolean startCheck=false;
	public boolean userCheck = false;
	private JButton start;	//게임 시작 버튼
	private JButton quit;	//게임 종료 버튼
	public JPanel buttonPanel = new JPanel();
	
	ObjectInputStream reader;	// 수신용 스트림
	ObjectOutputStream writer;	// 송신용 스트림
	
	public CardGame() {
		gamePanel = new GamePanel();
		cards = new ArrayList<>();
		cardCountList = new ArrayList<>();
		
		start = new JButton("Start");
		quit = new JButton("Quit");
		
		buttonPanel.setLayout(new FlowLayout());		
		buttonPanel.add(start);
		buttonPanel.add(quit);
		
		start.addActionListener(new StartButtonListener());
		quit.addActionListener(new QuitListener());
		
		this.setLayout(new BorderLayout());	
		add(BorderLayout.CENTER,gamePanel);
		add(BorderLayout.SOUTH,buttonPanel);
		
		gamePanel.initGame(level);	
	}
	
	class GamePanel extends JPanel implements ActionListener { //게임 패널 생성 클라
		public void initGame(int stage) {
			total_card = STAGE_CARD_NUM[stage];
			this.removeAll();
			this.setLayout(new GridLayout(2 + stage, 4, 5, 5));

			cards.clear();
			cardCountList.clear();

			initsuffle(total_card);
			suffleCard(total_card);
			addCardsToPanel();

			this.revalidate(); //새로고침되어서 다시 그려줌
		}
		public void initsuffle(int total_card) {
			for (int i = 0; i < total_card / 2; i++) {
				cardCountList.add(0);
			}
		}
		public void suffleCard(int total_card) {
			for (int i = 0; i < total_card; i++) {
				int check_num = (int) (Math.random() * (total_card / 2));
				if (cardCountList.get(check_num) != 2) { // 카드 2장이 꽉 차지 않았을때 카드 추가, cardCountList 카운트 증가
					Card card = new Card(check_num, GAME_CARD[check_num], START_CARD, 250, 700 /(total_card / 4));
					card.addActionListener(this);
					cards.add(card);
					// 900/(total_card / 4) => height를 레벨에 따라서 자동으로 크기 조절을 하기 위해
					cardCountList.set(check_num, cardCountList.get(check_num) + 1);
				} else { // 같은 카드가 2장 꽉 찼을 경우 전단계로 다시 돌아가서 카드를 추가 시킬 수 있게 해줌
					i--;
				}
			}
		}
		public void addCardsToPanel() { //카드를 패널에 그림
			for (Card card : cards) {
				this.add(card);
			}
		}
		public void Finish() {
			for (Card card : cards) {
				if (!card.isCorrect()) {
					return;
				}
			}
			JOptionPane.showMessageDialog(this,total_click +"번만에 해결하였습니다.");
			total_click = 0;
			if(level <= 1)
				initGame(++level);		
			else {
				int result = JOptionPane.showConfirmDialog(this, "계속하시겠습니까?", "축하힙니다.", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					level = 0;
					initGame(level);
				}
				else
					System.exit(0);
			}
		}	
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			for (int i = 0; i < cards.size(); i++) {
				if (e.getSource() == cards.get(i)) { //클릭한 곳이 i번째 카드를 뜻함
					if (!cards.get(i).isCorrect()) { //카드가 뒷면 일때
						if (!clicked) { // 아무것도 클릭이 되지 않았을때
							cards.get(i).setCorrect(true); //i번째 카드를 앞면으로 만듬
							click_card_number = i; //click_card_number는 i
							clicked = true; //밑에 단계로 넘어감
						} else {
							if (i != click_card_number) {  //다른 카드를 눌렸을 때
								if (cards.get(i).getcheck_num() == cards.get(click_card_number).getcheck_num()) { //첫번째 카드랑 두번째 카드가 같을 때
									cards.get(i).setCorrect(true); //i번째 카드를 앞면으로 만듬
								} else {
									cards.get(i).setCorrect(true); //i번째 카드를 앞면으로 보여줌
									JOptionPane.showMessageDialog(this,"틀렸습니다.");
									cards.get(i).setCorrect(false); //카드를 뒷면으로 바꿈
									cards.get(click_card_number).setCorrect(false); //카드를 뒷면으로 바꿈
								}
								clicked = false;
								total_click++;
							}
						}
						Finish();
						break;
					}
				}
			}
		}
	}
	public class StartButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(!startCheck){ 
				try {
					startCheck = true;
					turnCheck = true;
					userCheck = true;
			   		writer.writeObject(new GameMessage(GameMessage.MsgType.GAME_START, userCheck));
			   		writer.flush();		   					   		 
			   	  } catch(Exception ex) {
			   		  JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
			      	  ex.printStackTrace();
				}
			}
			else  JOptionPane.showMessageDialog(null, "게임이 진행중입니다.");
		}
	}
	private class QuitListener implements ActionListener 	// 종료 버튼(모든 프레임과 창이  강제로 닫힌다.)
	{                
		public void actionPerformed(ActionEvent event) 
		{
			System.exit(0);	// 프로그램 종료
		}
	}

}
