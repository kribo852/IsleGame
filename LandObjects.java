import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.event.KeyEvent;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.Random;
import java.util.ArrayList;
import java.awt.Graphics;

class LandObject{
	static BufferedImage sprites[];
	
	Inventory inventory=null;
	
	public BufferedImage getSprite(){
		return inventory.getSprite();
	}
	
	public void inventoryGive(Inventory other){
		if(inventory==null)
			inventory=other;
		else	
			inventory.give(other);
	}
	
	public void inventoryGive(Item item, int amount){
		inventory.give(item, amount);
	}
	
	public Inventory returnInventory(){
		return inventory;
	}
	
	protected static void maskSpriteColour(Color c){
		if(sprites==null)
			return;
		
		int rgb=c.getRGB();
		int maskcolour=(new Color(255,0,255,0)).getRGB();
		
		for(int sindex=0; sindex<sprites.length; sindex++){
			for(int i=0; i<sprites[sindex].getWidth(); i++){
				for(int j=0; j<sprites[sindex].getHeight(); j++){
					if(sprites[sindex].getRGB(i,j)==rgb){
						sprites[sindex].setRGB(i,j,maskcolour);
					}
				}
			}	
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(inventory.getSprite(), x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
}
	
class LandPlayer extends Humanoid{
	Color c=new Color(100 , 100, 25);
	PlayerInventory inventory=null;
	int savedkeypress=0;
	
	public LandPlayer(){
		inventory=InventoryFactory.createPlayerInventory();//also gives some starting items
		if(sprites==null){
			setSprites();
			maskSpriteColour(new Color(sprites[0].getRGB(0,0)));
		}
	}
	
	public void wantedMove(Isle island){
		
		//saved key is used to detect space-presses in order to update pacex and placey
		if(savedkeypress!=KeyEvent.VK_SPACE){
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_UP)
				y--;
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_DOWN)
				y++;
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_LEFT)
				x--;
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_RIGHT)
				x++;
		}else{
			
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_UP){
				placex=0;
				placey=-1;
			}if(KeyBoard.returnKeyPress()==KeyEvent.VK_DOWN){
				placex=0;
				placey=1;
			}if(KeyBoard.returnKeyPress()==KeyEvent.VK_LEFT){
				placex=-1;
				placey=0;
			}if(KeyBoard.returnKeyPress()==KeyEvent.VK_RIGHT){
				placex=1;
				placey=0;
			}
		}		
				savedkeypress=KeyBoard.returnKeyPress();
	}
	
	public Inventory updateInventory(){
		
		return inventory.update();
	}
	
	public final Item activeItem(){
		return inventory.returnActive();
	}
	
	public void inventoryGive(Inventory other){
		if(inventory==null)
			inventory=new PlayerInventory();
		inventory.give(other);
	}
	
	public void inventoryGive(Item item, int amount){
		if(inventory==null)
			inventory=new PlayerInventory();
		inventory.give(item, amount);
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		super.paint(g,x,y,tilesize);
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);	
		inventory.paint(g);
	}
	
}

class Tree extends LandObject{
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("jungle_tree.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)));
				
			}catch(IOException e){
		
			}
		}
	}
	
	public BufferedImage getSprite(){
		return sprites[0];
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(getSprite(), x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
}

class FractalTree extends Tree{

	static Color barkColour=new Color(75 ,75 , 50);
	static Color[] leafcolour=new Color[]{new Color(50, 100, 0), new Color(50, 100, 0) , new Color(65, 110, 25), new Color(75, 110, 25)};
	double firstlength=15, spread=Math.PI/5, deviation=Math.PI/4;
	Branch trunk;
	
	class Branch{
		double angle, length;
		
		Branch[] next;
		
		public Branch(){};//dummy for inheritance
		
		public Branch(int depth, double length, double angle){
			this.length=length;
			this.angle=angle;
			
			if(depth>=5)
			return;
			
			next=new Branch[(new Random()).nextInt(3)+1];
			
			double startangle=angle-((next.length-1)/2.0)*spread;
			
			
			for(int i=0; i<next.length; i++){
				next[i]=new Branch(++depth, length*0.95, startangle+spread*i+(-0.5+(new Random()).nextDouble())*deviation);
			}		
		}
		
		public void paint(Graphics g, int x, int y, double windangle){paint((Graphics2D)g, x, y, 8, 0, windangle,0);}
		
		protected void paint(Graphics2D g, double x, double y, int broadness, int countleafcolour, final double windangle,double depth){
			
			g.setStroke(new BasicStroke(Math.max(1,broadness)));
		
			if(next!=null)
				g.setColor(barkColour);
			else{
				g.setColor(leafcolour[countleafcolour]);
				g.setStroke(new BasicStroke(Math.max(1,broadness)));
			}
		
			g.drawLine((int)x , (int)y,
			(int)(length*Math.cos(angle+depth*windangle)+x),
			(int)(length*Math.sin(angle+depth*windangle)+y));
			
			if(next!=null && broadness>0){
				for(Branch branch: next){
					countleafcolour++;
					countleafcolour%=leafcolour.length;
					branch.paint(g, length*Math.cos(angle+depth*windangle)+x , length*Math.sin(angle+depth*windangle)+y, broadness-1, countleafcolour, windangle,depth+1);
				}
			}
		}
	}
	
	public FractalTree(){trunk=new Branch(0,16,3*Math.PI/2);}
	
	public void paint(Graphics g, int x, int y, int tilesize, double windangle){
		if(trunk!=null){
			trunk.paint(g,x*tilesize+tilesize/2,y*tilesize+tilesize/2, windangle);
		}
	}
	
	public void free(){
		trunk=null;
	}
	
	public Inventory returnInventory(){
		return InventoryFactory.createTreeInventory();
	}
	
}

class PineTree extends FractalTree{
	
	static Color[] leafcolour=new Color[]{new Color(35, 70, 0) , new Color(65, 110, 25)};
	
	enum PinePart{stem(3*Math.PI/2),
					leftbranch(Math.PI/12),
				rightbranch(Math.PI-Math.PI/12),
				needle(3*Math.PI/2);
			
			PinePart(double angle){this.angle=angle;}
			
			double angle;
	} 
	
class PineBranch extends Branch{
	
	public PineBranch(int depth, double length, PinePart mepart){
		
		this.length=length;
		this.angle=mepart.angle;
		
		if(depth>=6)
		return;
		
		switch(mepart){
		
			case stem:
				next=new Branch[3];
				next[0]=new PineBranch(depth+1, 0.95*length, PinePart.stem);
				next[1]=new PineBranch(1, 0.72*length, PinePart.leftbranch);
				next[2]=new PineBranch(1, 0.72*length, PinePart.rightbranch);
				
			break;
			case leftbranch:
			
				next=new Branch[2];
				next[0]=new PineBranch(depth+1, 0.72*length, PinePart.leftbranch);
				next[1]=new PineBranch(0, 3, PinePart.needle);
		
			break;
			case rightbranch:
			
				next=new Branch[2];
				next[0]=new PineBranch(depth+1, 0.72*length, PinePart.rightbranch);
				next[1]=new PineBranch(0, 3, PinePart.needle);
		
			break;
		}
	}
}
	
	public PineTree(){trunk=new PineBranch(0,16,PinePart.stem);}
	
	// I reduce the windangle, pretty notch
	public void paint(Graphics g, int x, int y, int tilesize, double windangle){
		super.paint(g, x, y, tilesize, windangle/8);
	}
	
	public Inventory returnInventory(){
		return InventoryFactory.createPineInventory();
	}
}

class FractalBush extends FractalTree{
	Branch[] trunk;
	double[] branchpositions;
	Color barkColour=new Color(50 ,100 , 0);
	
	public FractalBush(){
		trunk=new Branch[8];
		branchpositions=new double[trunk.length];
		for(int i=0; i<trunk.length; i++){
			branchpositions[i]=(new Random()).nextDouble();
			trunk[i]=new BushBranch(0, 8 , Math.PI*(branchpositions[i]-0.5)/12, false);
		}
	}
	
	class BushBranch extends Branch{
		boolean leaf=true;
	
		public BushBranch(int depth, double length, double addangle , boolean leaf){
			this.length=length;
			this.leaf=leaf;
			
			if(leaf){
				if(addangle>0){
					this.angle=Math.PI+depth*addangle;
				}else{
					this.angle=2*Math.PI+depth*addangle;
				}
			}else{
				this.angle=3*Math.PI/2+depth*addangle;
			}
			
			if(depth>12 || leaf)
				return;
				
			next=new Branch[2];
			next[0]=new BushBranch(depth+1, length*0.95, addangle, false);
			next[1]=new BushBranch(depth+1, length*0.95, addangle, true);
			
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize, double windangle){
		if(trunk!=null){
			for(int i=0; i<trunk.length; i++){
				int xoffset=(int)(tilesize*branchpositions[i]);
				trunk[i].paint(g,x*tilesize+xoffset,y*tilesize+tilesize/2, windangle);
			}
		}
	}
	
	public Inventory returnInventory(){
		return InventoryFactory.createBushInventory();
	}
}

abstract class Humanoid extends LandObject{
	
	protected int x, y;
	protected int placex=1, placey=0;// square where items are placed if dropped out of the inventory
	private static final Color placementcolor=new Color(75,175,100); 
	
	protected static final int[][]manimage=
	   {{0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0},
		{0,0,0,1,1,0,1,0,1,1,0,0,0,0,0,0},
		{0,0,0,1,1,0,1,0,1,1,0,0,0,0,0,0},
		{0,0,0,1,1,1,0,1,1,1,0,0,0,0,0,0},
		{0,0,0,0,1,1,0,1,1,0,0,0,0,0,0,0},
		{0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0},
		{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
		{0,0,0,0,0,1,1,1,0,0,0,0,0,1,0,0},
		{0,0,0,0,1,0,1,0,1,0,0,0,1,0,0,0},
		{0,0,0,1,0,0,1,0,0,1,0,1,0,0,0,0},
		{0,0,1,0,0,1,1,1,0,0,1,0,0,0,0,0},
		{0,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0},
		{0,0,1,0,1,0,0,1,0,0,0,0,0,0,0,0},
		{0,0,0,0,1,0,0,1,1,0,0,0,0,0,0,0},
		{0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0},
		{0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0}};
		
		//best singleton pattern 
	static void setSprites(){
		sprites=new BufferedImage[1];
		sprites[0]=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		Graphics g=sprites[0].getGraphics();
		
		g.setColor(Color.cyan);
		g.fillRect(0, 0  ,sprites[0].getWidth() ,sprites[0].getHeight());
		g.setColor(new Color(75,50,25));
				
		for(int i=0; i<manimage.length; i++){
			for(int j=0; j<manimage[i].length; j++){
				if(manimage[i][j]==1)
					g.fillRect(j*2, i*2 ,2 ,2);
			}
		}					
	}
	
	public abstract void wantedMove(Isle island);
	public abstract Inventory updateInventory();
	
	//paints the active item placement square
	public void paint(Graphics g, int x, int y, int tilesize){
		((Graphics2D)g).setStroke(new BasicStroke(1));
		g.setColor(placementcolor);
		g.drawRect((placex+x)*tilesize, (placey+y)*tilesize, tilesize, tilesize);
	}
	
	public int getPlaceX(){
		return placex+x;
	}
	
	public int getPlaceY(){
		return placey+y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int x){
		this.x=x;
	}
	
	public void setY(int y){
		this.y=y;
	}
	
}

class BushMan extends Humanoid{
	
	public BushMan(){
		if(sprites==null){
			setSprites();
			maskSpriteColour(new Color(sprites[0].getRGB(0,0)));
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
			super.paint(g,x,y,tilesize);
			g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
			//System.out.println("#############");
	}
	
	public Inventory updateInventory(){return null;}
	
	public void wantedMove(Isle island){
		if((new Random()).nextInt(5)!=0)
			return;
		
		ArrayList<int[]>possiblemoves=new ArrayList<int[]>();
		
		for(int i=-1; i<2; i++){
			for(int j=-1; j<2; j++){
				if(island.insideMapPos(i+x,  j+y) && island.validMovePosition(i+x,  j+y)){
					possiblemoves.add(new int[]{i+x,j+y});
				}
			}
		}
		
		if(!possiblemoves.isEmpty()){
			int[] tmp=possiblemoves.get((new Random()).nextInt(possiblemoves.size()));
			x=tmp[0];
			y=tmp[1];
		}
	}
}
