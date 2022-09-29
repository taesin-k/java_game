import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Card extends JButton {
	private ImageIcon GAME_CARD;			//���� ī��
	private ImageIcon START_CARD;			//���� ī��
	private int width, height, check_num;
	private boolean isCorrect;

	public Card(int check_num, String GAME_CARD, String START_CARD, int width, int height) {
		this.check_num = check_num; // ī�帶�� ��ȣ�� ��������
		this.setSize(width, height); // ī���� ���̿� ���̸� ������
		this.GAME_CARD = this.resizeImg(GAME_CARD); //��ư ũ�⿡ �̹��� ũ�⸦ ����
		this.START_CARD = this.resizeImg(START_CARD);
		this.isCorrect = false; // false�� �޸� ī�尡 ��ġ��
		
		this.setBorderPainted(false); // ��ư�� �ܰ����� ������
		this.setFocusPainted(false); // false�� ��ư Ŭ�� �� ������ ��Ŀ�� ������
		
		this.setCard(); // ī�带 ������
	}
	
	public void setCard() {
		if(this.isCorrect) {
			this.setIcon(GAME_CARD);
		}
		else {
			this.setIcon(START_CARD);
		}
	}
	
	public int getcheck_num() {
		return check_num;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
		setCard();
	}

	private ImageIcon resizeImg(String URL) {
		ImageIcon img = new ImageIcon(URL);
		Image originImg = img.getImage();
		Image scaledImg = originImg.getScaledInstance(this.getWidth(), this.getHeight(),Image.SCALE_AREA_AVERAGING);			
		
		return new ImageIcon(scaledImg);
	}
	
	
	
	
}
