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
	final String GAME_CARD[] = { "src/res/�׷�Ʈ.jpg", "src/res/����Ǯ.jpg",
			"src/res/��Ʈ��.jpg", "src/res/����.jpg", "src/res/�����̴���.jpg",
			"src/res/���̾��.jpg", "src/res/��Ʈ��.jpg", "src/res/���漭.jpg" };
	final String START_CARD = "src/res/Marvel_Comics.jpg";
	final int[] STAGE_CARD_NUM = { 8, 12, 16 }; // ���������� ī�� ����
	int total_card; //ī�� ����
	int total_click; //Ŭ�� Ƚ��

	int level = 0; // ��������
	GamePanel gamePanel; //�����ϴ� �г� ����

	ArrayList<Card> cards; // ī�� ��ư ����Ʈ
	ArrayList<Integer> cardCountList; //ī�� ���� ���� ���� ���� ����Ʈ (2��¦�� �����ִ� �뵵)
	boolean clicked = false;	//Ŭ�� ����
	int click_card_number; //Ŭ���� ī���� ��ġ
	
	private boolean turnCheck=false;
	private boolean startCheck=false;
	public boolean userCheck = false;
	private JButton start;	//���� ���� ��ư
	private JButton quit;	//���� ���� ��ư
	public JPanel buttonPanel = new JPanel();
	
	ObjectInputStream reader;	// ���ſ� ��Ʈ��
	ObjectOutputStream writer;	// �۽ſ� ��Ʈ��
	
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
	
	class GamePanel extends JPanel implements ActionListener { //���� �г� ���� Ŭ��
		public void initGame(int stage) {
			total_card = STAGE_CARD_NUM[stage];
			this.removeAll();
			this.setLayout(new GridLayout(2 + stage, 4, 5, 5));

			cards.clear();
			cardCountList.clear();

			initsuffle(total_card);
			suffleCard(total_card);
			addCardsToPanel();

			this.revalidate(); //���ΰ�ħ�Ǿ �ٽ� �׷���
		}
		public void initsuffle(int total_card) {
			for (int i = 0; i < total_card / 2; i++) {
				cardCountList.add(0);
			}
		}
		public void suffleCard(int total_card) {
			for (int i = 0; i < total_card; i++) {
				int check_num = (int) (Math.random() * (total_card / 2));
				if (cardCountList.get(check_num) != 2) { // ī�� 2���� �� ���� �ʾ����� ī�� �߰�, cardCountList ī��Ʈ ����
					Card card = new Card(check_num, GAME_CARD[check_num], START_CARD, 250, 700 /(total_card / 4));
					card.addActionListener(this);
					cards.add(card);
					// 900/(total_card / 4) => height�� ������ ���� �ڵ����� ũ�� ������ �ϱ� ����
					cardCountList.set(check_num, cardCountList.get(check_num) + 1);
				} else { // ���� ī�尡 2�� �� á�� ��� ���ܰ�� �ٽ� ���ư��� ī�带 �߰� ��ų �� �ְ� ����
					i--;
				}
			}
		}
		public void addCardsToPanel() { //ī�带 �гο� �׸�
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
			JOptionPane.showMessageDialog(this,total_click +"������ �ذ��Ͽ����ϴ�.");
			total_click = 0;
			if(level <= 1)
				initGame(++level);		
			else {
				int result = JOptionPane.showConfirmDialog(this, "����Ͻðڽ��ϱ�?", "�������ϴ�.", JOptionPane.YES_NO_OPTION);
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
				if (e.getSource() == cards.get(i)) { //Ŭ���� ���� i��° ī�带 ����
					if (!cards.get(i).isCorrect()) { //ī�尡 �޸� �϶�
						if (!clicked) { // �ƹ��͵� Ŭ���� ���� �ʾ�����
							cards.get(i).setCorrect(true); //i��° ī�带 �ո����� ����
							click_card_number = i; //click_card_number�� i
							clicked = true; //�ؿ� �ܰ�� �Ѿ
						} else {
							if (i != click_card_number) {  //�ٸ� ī�带 ������ ��
								if (cards.get(i).getcheck_num() == cards.get(click_card_number).getcheck_num()) { //ù��° ī��� �ι�° ī�尡 ���� ��
									cards.get(i).setCorrect(true); //i��° ī�带 �ո����� ����
								} else {
									cards.get(i).setCorrect(true); //i��° ī�带 �ո����� ������
									JOptionPane.showMessageDialog(this,"Ʋ�Ƚ��ϴ�.");
									cards.get(i).setCorrect(false); //ī�带 �޸����� �ٲ�
									cards.get(click_card_number).setCorrect(false); //ī�带 �޸����� �ٲ�
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
			   		  JOptionPane.showMessageDialog(null, "�޽��� ������ ������ �߻��Ͽ����ϴ�.");
			      	  ex.printStackTrace();
				}
			}
			else  JOptionPane.showMessageDialog(null, "������ �������Դϴ�.");
		}
	}
	private class QuitListener implements ActionListener 	// ���� ��ư(��� �����Ӱ� â��  ������ ������.)
	{                
		public void actionPerformed(ActionEvent event) 
		{
			System.exit(0);	// ���α׷� ����
		}
	}

}
