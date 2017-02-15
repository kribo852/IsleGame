import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

enum Item{
		stick(0),
		plantfiber(1),//grass, leaves, etc
		fish(2),
		berries(3),
		crop(4),
		shell(5),
		log(6),
		stone(7),
		stoneaxe(8),
		rope(9),
		fishnet(10),
		reed(11);
	
		private BufferedImage image;
		private int value;
		
	Item(int value){
		this.value=value;
		
		if(value==0){
			intitialize_stick();
		}
		if(value==1){
			intitialize_plantfiber();
		}
		if(value==2){
			intitialize_fish();
		}
		if(value==3){
			intitialize_berries();
		}
		if(value==6){
			intitialize_log();
		}
		if(value==7){
			intitialize_stone();
		}
		if(value==8){
			intitialize_stoneaxe();
		}
		if(value==9){
			intitialize_rope();
		}
		if(value==10){
			intitialize_fishnet();
		}if(value==11){
			intitialize_reed();
		}
		
	}
	
	public void intitialize_image(String spritename){
		
	}
	
	private void intitialize_stick(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		g2d.setColor(new Color(135,100,25,255));
		g2d.setStroke(new BasicStroke(4));
		g2d.drawLine(0,0,31,31);
		
	}
	
	private void intitialize_berries(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		Random rnd=new Random();
		for(int i=0; i<4; i++){
			g2d.setColor(new Color(150+rnd.nextInt(50),rnd.nextInt(200),rnd.nextInt(50),255));
			g2d.fillOval(8+rnd.nextInt(image.getWidth()-16),8+rnd.nextInt(image.getHeight()-16), 8+rnd.nextInt(4), 8+rnd.nextInt(4));
		}
	}
	
	private void intitialize_stone(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		g2d.setColor(new Color(135,135,100));
		g2d.fillOval(16,16,8,8);
	}
	
	private void intitialize_plantfiber(){
		
		try{
			image=ImageIO.read(new File("PlantFiber.png"));
			maskSpriteColour(new Color(image.getRGB(0,0)));
		}catch(IOException e){
			
		}
	}
	
	private void intitialize_fish(){
		
		try{
			image=ImageIO.read(new File("Fish.png"));
			maskSpriteColour(new Color(image.getRGB(0,0)));
		}catch(IOException e){
			
		}
	}
	
	private void intitialize_stoneaxe(){
		
		try{
			image=ImageIO.read(new File("StoneAxe.png"));
			maskSpriteColour(new Color(image.getRGB(0,0)));
		}catch(IOException e){
			
		}
	}
	
	private void intitialize_rope(){
		
		try{
			image=ImageIO.read(new File("Rope.png"));
			maskSpriteColour(new Color(image.getRGB(0,0)));
		}catch(IOException e){
			
		}
	}
	
	private void intitialize_fishnet(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		g2d.setColor(new Color(150,125,25));
		
		for(int i=0; i<4; i++){
			g2d.drawLine(i*image.getWidth()/4 , 0 , i*image.getWidth()/4  , image.getHeight());
			g2d.drawLine(0 ,  i*image.getHeight()/4 , image.getWidth() , i*image.getHeight()/4);
		}
	}
	
	private void intitialize_log(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		g2d.setColor(new Color(150,125,25));
		g2d.fillOval(4,8,8,16);
		g2d.fillRect(8,8,16,16);
		g2d.setColor(new Color(200,175,150));
		g2d.fillOval(20,8,8,16);
		
	}
	
	private void intitialize_reed(){
		
		image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g2d=image.createGraphics();
		g2d.setColor(new Color(255,0,255,0));
		g2d.fillRect(0,0,image.getWidth(), image.getHeight());
		
		for(int i=0; i<16; i++){
			g2d.setColor(new Color(50,125,25));
			g2d.drawLine(i*image.getWidth()/16 , 4 , i*image.getWidth()/16  , 8);
			g2d.setColor(new Color(150,125,25));
			g2d.drawLine(i*image.getWidth()/16 , 8 , i*image.getWidth()/16  , image.getHeight());
		}
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	protected void maskSpriteColour(Color c){
			if(image==null)
				return;
			
			int rgb=c.getRGB();
			int maskcolour=(new Color(255,0,255,0)).getRGB();
			for(int i=0; i<image.getWidth(); i++){
				for(int j=0; j<image.getHeight(); j++){
					if(image.getRGB(i,j)==rgb){
						image.setRGB(i,j,maskcolour);
					}
				}
			}	
		}
}
