import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Card extends JButton {
	private ImageIcon GAME_CARD;			//게임 카드
	private ImageIcon START_CARD;			//시작 카드
	private int width, height, check_num;
	private boolean isCorrect;

	public Card(int check_num, String GAME_CARD, String START_CARD, int width, int height) {
		this.check_num = check_num; // 카드마다 번호를 지정해줌
		this.setSize(width, height); // 카드의 넓이와 높이를 정해줌
		this.GAME_CARD = this.resizeImg(GAME_CARD); //버튼 크기에 이미지 크기를 맞춤
		this.START_CARD = this.resizeImg(START_CARD);
		this.isCorrect = false; // false시 뒷면 카드가 배치됨
		
		this.setBorderPainted(false); // 버튼의 외곽선을 없애줌
		this.setFocusPainted(false); // false시 버튼 클릭 시 나오는 포커스 없애줌
		
		this.setCard(); // 카드를 보여줌
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
