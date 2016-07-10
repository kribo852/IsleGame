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
	
	Random RND=new Random();
	Graphics g=jframe.getGraphics(); 
	BufferedImage sea=new BufferedImage(screensize,screensize,1);
	
	for(int j=0; j<screensize; j++){
		double redbase=75;
		double greenbase=75;
		double bluebase=125;
		double scale=(0.35*(screensize-j-1))/screensize;
		scale+=1.0;
		
		Color c=new Color(Math.min(255 , (int)(scale*redbase)) , 
		Math.min(255 , (int)(scale*greenbase)),  Math.min(255 , (int)(scale*bluebase)));
		
		for(int i=0; i<screensize; i++){
			sea.setRGB(i, j, c.getRGB());
		}
	}
	
	BufferedImage drawbuffer=new BufferedImage(screensize,screensize,BufferedImage.TYPE_INT_ARGB);
	Graphics drawbuffergraphics=drawbuffer.getGraphics();
	while(true){
		map.update();
		
		drawbuffergraphics.drawImage(sea, 0, 0, null);
		map.paint(drawbuffergraphics, screensize, screensize);
		
		
		g.drawImage(drawbuffer, 0, 0 , null);
		
		try {
			Thread.sleep(35);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	}
	
}
