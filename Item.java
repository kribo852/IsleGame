import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;

enum Item{
		stick(0),
		plantfiber(1),//grass, leaves, etc
		fish(2),
		berries(3),
		crop(4),
		shell(5),
		log(6);
	
		private int amount=0, value=0;
		private BufferedImage image;
		
	Item(int value){
		this.value=value;
		
		if(value==0){
			intitialize_stick();
		}
		
	}
	
	public void intitialize_image(String spritename){
		
	}
	
	public void intitialize_stick(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		g2d.setColor(new Color(135,100,25,255));
		g2d.setStroke(new BasicStroke(4));
		g2d.drawLine(0,0,31,31);
		
	}
	
	public void add(int amount){
		this.amount+=amount;
	}
	
	public BufferedImage getImage(){
		return image;
	}
}
