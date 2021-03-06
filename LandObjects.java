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
import java.util.HashSet;

abstract class LandObject{
	static BufferedImage sprites[];
	
	Inventory inventory=null;
	
	public BufferedImage getSprite(){
		return inventory.getSprite();
	}
	
	// these are prone to error{
	public void inventoryGive(Inventory other){
		if(inventory==null)
			inventory=other;
		else	
			inventory.give(other);
	}
	
	public void inventoryGive(Item item, int amount){
		inventory.give(item, amount);
	}
   //}should be abstract
	
	public Inventory returnInventory(){
		return inventory;
	}
	
	protected static void maskSpriteColour(Color c , BufferedImage sprites[]){
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
	
	public abstract void paint(Graphics g, int x, int y, int tilesize);	
}

class InventoryHolder extends LandObject{
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(inventory.getSprite(), x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
}

class LayeredDecorator extends LandObject{
	ArrayList<LandObject> layers=null;
	
	public LayeredDecorator(){
		layers=new ArrayList<LandObject>();
	}
	
	public void addLayer(LandObject l){
		layers.add(l);
	}
	
	public LandObject removeLayer(LandObject l){
		for(int i=0; i<layers.size(); i++)
			if(layers.get(i)!=l){
				return layers.get(i);
			}
			
		return null;
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		
		for(LandObject l: layers){
			l.paint( g,  x,  y,  tilesize);
		}
	}
}
	
class LandPlayer extends Humanoid{
	Color c=new Color(100 , 100, 25);
	//PlayerInventory inventory=null;
	int savedkeypress=0;
	
	public LandPlayer(){
		super();
		inventory=InventoryFactory.createPlayerInventory();//also gives some starting items
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
			action=true;
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
		
		return ((PlayerInventory)inventory).update();
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
	
	public final boolean itemActive(Item item){
		return ((PlayerInventory)inventory).returnActive()!=null && ((PlayerInventory)inventory).returnActive()==item;
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		super.paint(g,x,y,tilesize);
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);	
		((PlayerInventory)inventory).paint(g);
	}
	
}

class Tree extends LandObject{
	
	public Tree(){
		setSprites();
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("Jungle_Tree.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
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
	boolean treealive=true;
	
	public void setAlive(boolean treealive){
		this.treealive=treealive;
	}
	
	public boolean isAlive(){
		return treealive;
	}
	
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
			
			if(next!=null){
				g.setColor(barkColour);	
			}else{
				if(!treealive)return;
				
				g.setColor(leafcolour[countleafcolour]);
				
			}
			g.setStroke(new BasicStroke(Math.max(1,broadness)));
		
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
		Color[] c=super.leafcolour;
		super.leafcolour=this.leafcolour;
		super.paint(g, x, y, tilesize, windangle/8);
		super.leafcolour=c;
	}
	
	public Inventory returnInventory(){
		return InventoryFactory.createPineInventory();
	}
}

class FractalBush extends FractalTree{
	Branch[] trunk;
	double[] branchpositions;
	static Color barkColour=new Color(50 ,100 , 0);
	
	public FractalBush(){
		trunk=new Branch[8];
		branchpositions=new double[trunk.length];
		for(int i=0; i<trunk.length; i++){
			branchpositions[i]=(new Random()).nextDouble();
			trunk[i]=new BushBranch(0, 8 , Math.PI*(branchpositions[i]-0.5)/12, false);
		}
	}
	
	class BushBranch extends Branch{
	
		public BushBranch(int depth, double length, double addangle , boolean leaf){
			this.length=length;
			
			if(leaf){
				if(addangle>0){
					this.angle=Math.PI+(depth+6.4)*addangle;
				}else{
					this.angle=2*Math.PI+(depth+6.4)*addangle;
				}
			}else{
				this.angle=3*Math.PI/2+(depth+6.4)*addangle;
			}
			
			if(depth>10 || leaf)
				return;
				
			if(depth>=1){
				next=new Branch[2];
				next[0]=new BushBranch(depth+1, length*0.95, addangle, false);
				next[1]=new BushBranch(depth+1, length*0.95, addangle, true);
			}else{
				next=new Branch[1];
				next[0]=new BushBranch(depth+1, length*0.95, addangle, false);
			}
		}
	}
	
	//in order to normalize paint-bush from layeredobject
	public void paint(Graphics g, int x, int y, int tilesize){
		paint(g,x,y,tilesize,0);
	}
	
	public void paint(Graphics g, int x, int y, int tilesize, double windangle){
		Color c=super.barkColour;
		if(treealive){
			super.barkColour=this.barkColour;
		}
		if(trunk!=null){
			for(int i=0; i<trunk.length; i++){
				int xoffset=(int)(tilesize*branchpositions[i]);
				trunk[i].paint(g,x*tilesize+xoffset,y*tilesize+3*tilesize/4, windangle/2);
			}
		}
		super.barkColour=c;
	}
	
	public Inventory returnInventory(){
		return InventoryFactory.createBushInventory();
	}
}

class Reed extends FractalBush{
	static Color barkColour=new Color(200 ,175 , 100);
	
	public Reed(){
		trunk=new Straw[24];
		branchpositions=new double[trunk.length];
		for(int i=0; i<trunk.length; i++){
			branchpositions[i]=(new Random()).nextDouble();
			trunk[i]=new Straw(0, 5 , Math.PI*(branchpositions[i]-0.5)/24);
		}
	}
	
	class Straw extends Branch{
		
		public Straw(int depth, double length, double addangle){
			this.length=length;
			this.angle=3*Math.PI/2+(depth+2)*addangle;
			
			if(depth>12)
				return;	
			
				next=new Straw[1];
				next[0]=new Straw(depth+1, length*0.95, addangle);
			}
	
		public void paint(Graphics g, int x, int y, double windangle){paint((Graphics2D)g, x, y, 1, 0, windangle,0);}
		
		protected void paint(Graphics2D g, double x, double y, int broadness, int countleafcolour, final double windangle,double depth){
			
			if(next!=null){
				g.setColor(barkColour);	
			}else{
				if(!treealive)return;
				
				g.setColor(leafcolour[countleafcolour]);
				
			}
			g.setStroke(new BasicStroke(Math.max(1,broadness)));
		
			g.drawLine((int)x , (int)y,
			(int)(length*Math.cos(angle+depth*windangle)+x),
			(int)(length*Math.sin(angle+depth*windangle)+y));
			
			if(next!=null){
				for(Branch branch: next){
					countleafcolour++;
					countleafcolour%=leafcolour.length;
					branch.paint(g, length*Math.cos(angle+depth*windangle)+x , length*Math.sin(angle+depth*windangle)+y, broadness, countleafcolour, windangle,depth+1);
				}
			}
		}
	}
	
	public Inventory returnInventory(){
		return InventoryFactory.createReedInventory();
	}
}

abstract class Humanoid extends LandObject{
	
	protected int x, y;
	protected int placex=1, placey=0;// square where items are placed if dropped out of the inventory
	private static final Color placementcolor=new Color(75,175,100); 
	protected boolean action=false;
	static BufferedImage sprites[];
	
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
		
	Humanoid(){
		if(sprites==null)
			setSprites();
	}
		
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
		
		maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);			
	}
	
	public abstract void wantedMove(Isle island);
	
	public abstract Inventory updateInventory();//dropped items
	
	public boolean starving(){
		return !inventory.updateTransformPossible();
	}
	
	public final boolean getAction(){
		boolean tmp=action;
		action=false;
		return tmp;
	}
	
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
	
	public boolean itemActive(Item item){
		return inventory.contains(item);
	}	
}

abstract class Building extends LandObject{
	
	
}

class Boat extends Building{
	static BufferedImage[] sprites=null;
	
	public Boat(){
		setSprites();
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("Boat.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
			}catch(IOException e){
		
			}
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
}

class House extends Building{
	static BufferedImage[] sprites=null;
	final int x, y;
	static HashSet<Integer> proximatpositions;
	
	public House(int x, int y){
		setSprites();
		this.x=x;
		this.y=y;
		if(proximatpositions==null)proximatpositions=new HashSet<Integer>();
		addProxies(x,y); 
	}
	
	public House(){x=y=-1;}
	
	private void addProxies(int x, int y){
		if(proximatpositions==null)proximatpositions=new HashSet<Integer>();
		
		for(int i=-1; i<2; i++)for(int j=-1; j<2; j++){
			if(i!=0 || j!=0)proximatpositions.add(((y+j)<<16)+(x+i));
		}
		
	}
	
	static public int[] getRandomProxyPosition(){
		if(proximatpositions==null)return null;
		
		int position=(new Random()).nextInt(proximatpositions.size());
		ArrayList<Integer> positions=new ArrayList<Integer>();
		positions.addAll(proximatpositions);
		return new int[]{positions.get(position) & 0xFFFF, positions.get(position)>>16};
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("House.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
			}catch(IOException e){
		
			}
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
	public int getX(){return x;}
	
	public int getY(){return y;}
	
}

class FirePlace extends Building{
	static BufferedImage[] sprites=null;
	FireFlame[] particles;
	
	public FirePlace(){setSprites();}
	
	public FirePlace(int x, int y){
		this(x, y, 5);
		setSprites();
		particles=new FireFlame[50];
		for(int i=0; i<particles.length; i++){
			particles[i]=new FireFlame();
			setFlamePosition(particles[i]);
		}
	}
	
	protected FirePlace(int x, int y, double litradius){
		DayCycleClass.addLitPosition(x, y, litradius);
	}
	
	private void setFlamePosition(FireFlame fireflame){
		fireflame.x=(new Random()).nextInt(32);
		fireflame.y=8;
		fireflame.lifetime=2+(new Random()).nextInt(12);
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("FirePlace.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
			}catch(IOException e){
		
			}
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
		
		for(FireFlame fireflame: particles){
			if(fireflame.update()){
				setFlamePosition(fireflame);
			}
			g.drawImage(FireFlame.image, x*tilesize+(int)fireflame.getX(),  y*tilesize+(int)fireflame.getY(), null);		
		}
	}
}

class Torch extends FirePlace{
	static BufferedImage[] sprites=null;
	
	public Torch(){setSprites();}
		
	public Torch(int x, int y){
		super(x,y,3.6);
		setSprites();
		particles=new FireFlame[20];
		for(int i=0; i<particles.length; i++){
			particles[i]=new FireFlame();
			setFlamePosition(particles[i]);
		}
	}
	
	private void setFlamePosition(FireFlame fireflame){
		fireflame.x=(new Random()).nextInt(8);
		fireflame.y=2;
		fireflame.lifetime=3+(new Random()).nextInt(15);
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("Torch.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
			}catch(IOException e){
		
			}
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
		
		for(FireFlame fireflame: particles){
			if(fireflame.update()){
				setFlamePosition(fireflame);
			}
			g.drawImage(FireFlame.image, x*tilesize+(int)fireflame.getX(),  y*tilesize+(int)fireflame.getY(), null);		
		}
	}
}

class Palisade extends Building{
	static BufferedImage[] sprites=null;
	
	public Palisade(){
		setSprites();
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("Palisade.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
			}catch(IOException e){
		
			}
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
}

class LightHouse extends Building{
	static BufferedImage[] sprites=null;
	final int x, y;
	static ArrayList<LightHouse> lighthouses;
	static HashSet<Integer> proximatpositions;
	
	public LightHouse(int x, int y){
		setSprites();
		this.x=x;
		this.y=y;
		if(lighthouses==null)lighthouses=new ArrayList<LightHouse>();
		lighthouses.add(this);
		addProxies(x,y);
	}
	
	public LightHouse(){
		x=-1;
		y=-1;
		setSprites();
	}
	
	private void addProxies(int x, int y){
		if(proximatpositions==null)proximatpositions=new HashSet<Integer>();
		
		for(int i=-1; i<2; i++)for(int j=-1; j<2; j++){
			if(i!=0 || j!=0)proximatpositions.add(((y+j)<<16)+(x+i));
		}
		
	}
	
	static public int[] getRandomProxyPosition(){
		int position=(new Random()).nextInt(proximatpositions.size());
		ArrayList<Integer> positions=new ArrayList<Integer>();
		positions.addAll(proximatpositions);
		return new int[]{positions.get(position) & 0xFFFF, positions.get(position)>>16};
	}
	
	static public boolean isProxy(int x, int y){
		return proximatpositions!=null && proximatpositions.contains((y<<16)+x);
	}
	
	static public int getNumberOf(){
		if(lighthouses==null)return 0;
		return lighthouses.size();
	}
	
	//best singleton pattern 
	static void setSprites(){
		if(sprites==null){
			sprites=new BufferedImage[1];
	
			try{
				sprites[0]=ImageIO.read(new File("LightHouse.png"));
				maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);
				
			}catch(IOException e){
		
			}
		}
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(sprites[0], x*tilesize,  (y-1)*tilesize, 2*tilesize, 2*tilesize,null);
	}
	
	public int getX(){return x;}
	
	public int getY(){return y;}
	
}

class SingletonTreeFactory{
	
	private int currenttreecounter=0;
	private FractalTree[] livetrees;
	private FractalTree[] deadtrees;
	
	public SingletonTreeFactory(){
		livetrees=new FractalTree[5];
		deadtrees=new FractalTree[3];
		
		livetrees[0]=new FractalTree();
		livetrees[1]=new PineTree();
		livetrees[2]=new FractalBush();
		livetrees[3]=new FractalTree();
		livetrees[4]=new FractalTree();
		
		deadtrees[0]=new FractalTree();
		deadtrees[1]=new PineTree();
		deadtrees[2]=new FractalBush();
		for(int i=0; i<deadtrees.length; i++)deadtrees[i].setAlive(false);
		
	}
	
	public FractalTree getTree(boolean treealive){
		++currenttreecounter;
		return treealive ? livetrees[currenttreecounter%livetrees.length] : deadtrees[currenttreecounter%deadtrees.length];
	}
	
	public FractalTree getDeadTreeOfType(Class c){
		
		for(FractalTree tree: deadtrees){
			if(tree.getClass()==c)return tree;
		}
		
		return null;
	}
	
	public FractalTree getLiveTreeOfType(Class c){
		if(c==FractalTree.class){
			return livetrees[(3+(new Random()).nextInt(3))%livetrees.length];
		}
		if(c==PineTree.class){
			return livetrees[1];
		}
		if(c==FractalBush.class){
			return livetrees[2];
		}
		
		return null;
	}
}

class Ghost extends Humanoid{
	
	double position;
	int column;
	boolean direction;
	static BufferedImage sprites[];
	
	protected static final int[][] secndimage={{0,0,1,1,1,1,1,1,0,0},
											   {0,1,1,6,6,5,5,1,1,0},
											   {1,1,1,6,6,5,5,1,1,1},
											   {1,7,7,0,0,0,0,4,4,1},
											   {1,7,7,0,1,1,0,4,4,1},
											   {1,8,8,0,1,1,0,3,3,1},
											   {1,8,8,0,0,0,0,3,3,1},
											   {1,1,1,9,9,2,2,1,1,1},
											   {0,0,1,9,9,2,2,1,0,0},
											   {0,0,1,1,1,1,1,1,0,0}};
	
	public Ghost(int column, boolean direction){
		if(sprites==null)
			setSprites();
		this.column=column;
		this.direction=direction;
		position=0;
	}
	
	public void paint(Graphics g, int offsetx, int offsety, int tilesize){
		g.drawImage(sprites[0], (int)((getX(tilesize)-offsetx)*tilesize),  (int)((getY(tilesize)-offsety)*tilesize), tilesize, tilesize , null);
		g.setColor(new Color(50,200,225,20));
		g.fillRect((int)((getX(tilesize)-offsetx)*tilesize),  (int)((getY(tilesize)-offsety)*tilesize)  ,tilesize ,tilesize);
	}
	
	
	static void setSprites(){
		sprites=new BufferedImage[1];
		sprites[0]=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		Graphics g=sprites[0].getGraphics();
		for(int i=0; i<manimage.length; i++){
			for(int j=0; j<manimage[i].length; j++){
				if(manimage[i][j]==1)
					g.setColor(new Color(75,50,25, 125));
				else
					g.setColor(Color.cyan);
				
					g.fillRect(j*2, i*2 ,2 ,2);
			}
		}	
		maskSpriteColour(new Color(sprites[0].getRGB(0,0)) , sprites);			
	}
	
	public Inventory updateInventory(){return null;}
	public void wantedMove(Isle island){}
	
	public int getColumn(){return column;}
	public double getPosition(){return position;}
	public void setPosition(double speed){position+=speed;}
	
	public double getX(double tilesize){
		if(!direction){
			return column;
		}else{
			return position/tilesize;
		}
	}
	
	public double getY(double tilesize){
		if(direction){
			return column;
		}else{
			return position/tilesize;
		}
	}
	
}
