import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Random;

enum Item{
		stick(0),
		plantfiber(1),//grass, leaves, etc
		fish(2),
		berries(3),
		crop(4),
		shell(5),
		log(6),
		stone(7);
	
		private int amount=0, value=0;
		private BufferedImage image;
		
	Item(int value){
		this.value=value;
		
		if(value==0){
			intitialize_stick();
		}
		if(value==3){
			intitialize_berries();
		}
		if(value==7){
			intitialize_stone();
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
	
	public void intitialize_berries(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		Random rnd=new Random();
		for(int i=0; i<5; i++){
			g2d.setColor(new Color(rnd.nextInt(255),rnd.nextInt(255),rnd.nextInt(255),255));
			g2d.fillOval(rnd.nextInt(image.getWidth()),rnd.nextInt(image.getHeight()), 8+rnd.nextInt(4), 8+rnd.nextInt(4));
		}
	}
	
	public void intitialize_stone(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		g2d.setColor(new Color(135,135,100));
		g2d.fillOval(16,16,8,8);
	}
	
	public void add(int amount){
		this.amount+=amount;
	}
	
	public BufferedImage getImage(){
		return image;
	}
}
