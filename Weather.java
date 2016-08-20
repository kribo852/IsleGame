import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Runnable;
import java.util.Random;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

abstract class Particle{
	double x , y , dx, dy;
	
	static public void initializeBuffer(BufferedImage image,  Color particlecolour, double strength){
		for(int i=0; i<image.getWidth(); i++){
			for(int j=0; j<image.getHeight(); j++){
				double distance=strength*Math.sqrt(Math.pow(image.getWidth()/2-i-0.5,2)+Math.pow(image.getWidth()/2-j-0.5,2));
				int alpha=(int)(200*Math.exp(-distance));
				Color c=new Color(particlecolour.getRGB());
				c=new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
				image.setRGB(i,j, c.getRGB());
			}
		}
	} 
}

class Raindrop extends Particle{
	static BufferedImage image=null;
	static final Color raincoloured=new Color(25,50,75);
	
	public Raindrop(){
		if(image!=null)
			return;
		
		image=new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
		initializeBuffer(image,raincoloured,0.32);
	}
	
}

class FireFlame extends Particle{
	static BufferedImage image=null;
	static final Color firecoloured=new Color(250,200,150);
	
	double sineangle;
	int lifetime;
	
	public FireFlame(){
		double speed=2+(new Random()).nextDouble()*3;
		
		dx=(-0.4+(new Random()).nextDouble()*0.8);
		dy=Math.sqrt(1-dx*dx);
		dx*=speed;
		dy*=-speed;
		sineangle=(new Random()).nextDouble()*Math.PI*2;
		
		if(image!=null)
			return;
		
		image=new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
		initializeBuffer(image,firecoloured,0.2);
	}
	
	public boolean update(){
		sineangle+=(Math.PI/8)%(2*Math.PI);
		x+=dx;
		y+=dy;
		--lifetime;
		return lifetime<0;
	}
	
	public double getX(){
		return x+8*Math.cos(sineangle);
	}
	
	public double getY(){
		return y;
	}
}

class Rainfall implements Runnable{
	boolean active=true;
	int timer=500;
	BufferedImage rainbuffer[]=new BufferedImage[2];
	Raindrop[] rain;
	static double windangle=0;//used for sinus winds in trees
	int currentframe=0;
	boolean nextframeready=false;
	Random RND=new Random();
	Color lighcol=new Color(200,200,255,120);
	
	
	public Rainfall(){
		rain=new Raindrop[250];
	
		for(int i=0; i<rain.length; i++){
			rain[i]=new Raindrop();
			double angle=Math.PI/2+Math.PI*0.2*RND.nextDouble();
			double speed=10+10*(new Random()).nextDouble();
			rain[i].dx=speed*Math.cos(angle);
			rain[i].dy=speed*Math.sin(angle);
			rain[i].y=RND.nextInt(1000);
			rain[i].x=RND.nextInt(1000);
		}	
		
		rainbuffer[0]=new BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB);
		rainbuffer[1]=new BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB);
		
	}
	
	public void run(){
		
			int updateframe= (currentframe+1)%rainbuffer.length;
			
			for(int i=0; i<rain.length; i++){
				if(active && rain[i].y>1000){
					rain[i].y=-RND.nextInt(750);
					rain[i].x=RND.nextInt(850);
				}else{
					rain[i].x+=rain[i].dx;
					rain[i].y+=rain[i].dy;
				}
			}
			
			Graphics g=rainbuffer[updateframe].getGraphics();
			if(active){
				if(RND.nextInt(25)==0){
					((Graphics2D)g).setBackground(new Color(25, 50, 50, 40));
					g.clearRect(0,0,800, 800);
					paintLightning(g , 200+RND.nextInt(400), -10 , 200, lighcol , 0); 
					
				}else{
					((Graphics2D)g).setBackground(new Color(25, 50, 50, 80));
					g.clearRect(0,0,800, 800);
				}
			}else{
				((Graphics2D)g).setBackground(new Color(255, 255, 255, 0));
				g.clearRect(0,0,800, 800);
			}
			
			for(int i=0; i<rain.length; i++){
				g.drawImage(Raindrop.image, (int)rain[i].x , (int)rain[i].y , null);
			}
			
			windangle+=(active? Math.PI/50 : Math.PI/400)%Math.PI;	
			
			timer--;
			
			if(timer<0){
				timer=(active? RND.nextInt(5000) : RND.nextInt(750));
				
				active=!active;	
			}
			
				
	}
	
	public void paint(Graphics g , int screenwidth, int screenheight){
		g.drawImage(rainbuffer[currentframe], 0 , 0 , null);
		currentframe=(currentframe+1)%rainbuffer.length;	
	}
	
	private void paintLightning(Graphics g ,int x, int y,double length, Color lighcol, int depth){
		if(depth==0){
			g.setColor(lighcol);
		}
		if(depth>5)return;
		
		do{
			double nextxlen=RND.nextDouble()*length;
			double nextylen=Math.sqrt(length*length-nextxlen*nextxlen);
			
			if(RND.nextBoolean())
				nextxlen=-nextxlen;
			
			((Graphics2D)g).setStroke(new BasicStroke(12-depth));
			g.drawLine(x,y,x+(int)nextxlen, y+(int)nextylen);
			paintLightning( g ,x+(int)nextxlen, y+(int)nextylen, length*0.8 , lighcol, depth+1);
		}while(RND.nextInt(depth+2)==0);
	}
	
	public static double getWindAngle(){
		return windangle;
	}
}
