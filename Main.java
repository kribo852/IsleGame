import javax.swing.JFrame;
import java.util.*;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Thread;
import java.lang.Math;

class main{

public static void main(String[] args){
	
	int screensize=800;
	
	JFrame jframe=new JFrame();
	
	jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	jframe.setVisible(true);
	jframe.setSize(screensize , screensize);
	jframe.addKeyListener(new KeyBoard());
	
	Map map=new Map();
	Rainfall rainfall=new Rainfall();
	
	Graphics g=jframe.getGraphics(); 
	BufferedImage sea[]=new BufferedImage[]{new BufferedImage(screensize,screensize,1)
		,new BufferedImage(screensize,screensize,1),new BufferedImage(screensize,screensize,1)};
	
	makeSea(screensize, sea[0]);
	makeNightSkySea(screensize, sea[1]);
	makeNightStormSea(screensize, sea[2]);
	
	BufferedImage drawbuffer=new BufferedImage(screensize,screensize,BufferedImage.TYPE_INT_ARGB);
	Graphics drawbuffergraphics=drawbuffer.getGraphics();
	
	long millispassed=System.currentTimeMillis();
	map.update();
	
	ShowCrafteables.setGrapihcs(g);
	
	while(true){
		(new ShowCrafteables()).main();
		
		boolean onland=map.update();
		DayCycleClass.update();
		
		drawbuffergraphics.drawImage(DayCycleClass.isDay() ? sea[0] : 
		Rainfall.isActive()? sea[2] : sea[1], 0, 0, null);
		map.paint(drawbuffergraphics, screensize, screensize);
		
		rainfall.paint(drawbuffergraphics,screensize,screensize);
		new Thread(rainfall).start();
		g.drawImage(drawbuffer, 0, 0 , null);
		
		try {
			if(onland)
				Thread.sleep(Math.max(0, 75+millispassed-System.currentTimeMillis()));
			else
				Thread.sleep(Math.max(0, 25+millispassed-System.currentTimeMillis()));
			millispassed=System.currentTimeMillis();
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	}
	
	private static void makeSea(int screensize, BufferedImage sea){
		for(int j=0; j<screensize; j++){
			double redbase=75;
			double greenbase=100;
			double bluebase=125;
			double scale=(0.35*(screensize-j-1))/screensize;
			scale+=1.0;
		
			Color c=new Color(Math.min(255 , (int)(scale*redbase)) , 
			Math.min(255 , (int)(scale*greenbase)),  Math.min(255 , (int)(scale*bluebase)));
		
			for(int i=0; i<screensize; i++){
				sea.setRGB(i, j, c.getRGB());
			}
		}
	}
	
	private static void makeNightSkySea(int screensize, BufferedImage sea){
		for(int j=0; j<screensize; j++){
			double redbase=60;
			double greenbase=50;
			double bluebase=80;
			double scale=(0.35*(screensize-j-1))/screensize;
			scale+=1.0;
		
			Color c=new Color(Math.min(255 , (int)(scale*redbase)) , 
			Math.min(255 , (int)(scale*greenbase)),  Math.min(255 , (int)(scale*bluebase)));
		
			for(int i=0; i<screensize; i++){
				sea.setRGB(i, j, c.getRGB());
			}
		}
		Random RND=new Random();
		Graphics g=sea.getGraphics();
		for(int i=0; i<5000; i++){
			Color c=new Color(200,220,255,RND.nextInt(100));
			g.setColor(c);
			g.fillRect(RND.nextInt(screensize), RND.nextInt(screensize),2,2);
		}
		
	}
	
	private static void makeNightStormSea(int screensize, BufferedImage sea){
		for(int j=0; j<screensize; j++){
			double redbase=45;
			double greenbase=60;
			double bluebase=75;
			double scale=(0.35*(screensize-j-1))/screensize;
			scale+=1.0;
		
			Color c=new Color(Math.min(255 , (int)(scale*redbase)) , 
			Math.min(255 , (int)(scale*greenbase)),  Math.min(255 , (int)(scale*bluebase)));
		
			for(int i=0; i<screensize; i++){
				sea.setRGB(i, j, c.getRGB());
			}
		}
	}
	
}
