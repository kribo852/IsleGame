import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.lang.Runnable;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.Graphics2D;

class Isle implements Runnable{
	
static final int islesize=320;
LandType[][] layout;
LandObject[][] objects;
BufferedImage tile;
int seedx,seedy;//used to generate diff islands
LandPlayer landplayer;//null if at sea
Rainfall rainfall;

	class Tuple<Type>{
		public Type a,b;
		
		public Tuple(Type a, Type b){
			this.a=a;
			this.b=b;
		}	
	}

public Isle(int seedx, int seedy){
		this.seedx=seedx;
		this.seedy=seedy;
		landplayer=null;
		//Tree.setSprites();
		rainfall=new Rainfall();
}

	//generates island in new thread
	public void run(){
		layout=new LandType[islesize][islesize]; 
		for(int i=0; i<islesize; i++){
			for(int j=0; j<islesize; j++){
				layout[i][j]=LandType.water;
			}
		}
		
		layout[islesize/2][islesize/2]=LandType.dirt;
		newFloodFill(layout, islesize/2, islesize/2);
		
		layout=slimArray(layout);
		
		tile=new BufferedImage(layout.length , layout[0].length , BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]==LandType.dirt){
					tile.setRGB(i, j, LandType.dirt.getColour().getRGB());
				}
			}	
		}
	}
	
	protected boolean newFloodFill(LandType[][] layout, int x , int y){
		
		ArrayList<Tuple<Integer>> progress=new ArrayList<Tuple<Integer>>();
	
		progress.add(new Tuple<Integer>(x , y));
		
		while(!progress.isEmpty()){
			
			Tuple<Integer> tupel=progress.remove(0);
			
			for(int i=-1; i<2; i++){
				for(int j=-1; j<2; j++){
					if(Math.abs(i)+Math.abs(j)==1){
						int newx=tupel.a+i;
						int newy=tupel.b+j;
						
						if(Map.randomNumber(newx*seedx , newy*seedy)%299==0){
							layout[newx][newy]=LandType.dirt;
							if(newFloodFill(layout, newx, newy))
								return true;
						}
							
						
						if(newx==-1 || newx==layout.length || newy==-1 || newy==layout[0].length)
							return true;
						
						if(layout[newx][newy]==LandType.water){
							layout[newx][newy]=LandType.dirt;
							progress.add(new Tuple<Integer>(newx , newy));
						}
					}
				}
			}
		}
		return false;
	}
	
	public LandType[][] slimArray(LandType[][] layout){
		int minlandx=0;
		int minlandy=0;
		int maxlandx=0;
		int maxlandy=0;
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]==LandType.dirt){
					minlandx=Math.min(i , minlandx);
					minlandy=Math.min(j , minlandy);
					maxlandx=Math.max(i , maxlandx);
					maxlandy=Math.max(j , maxlandy);
				}
			}
		}
		
		LandType[][] rtn=new LandType[maxlandx+1-minlandx][maxlandy+1-minlandy];
		
		for(int i=minlandx; i<=maxlandx; i++){
			for(int j=minlandy; j<=maxlandy; j++){
				rtn[i-minlandx][j-minlandy]=layout[i][j];
			}
		}
		return rtn;
	}
	
	public void initializeLand(){
		
		if(landplayer!=null)
			return;
			
		LandObject[] treemap=new LandObject[15];
		int treeindex=0;
		
		objects=new LandObject[layout.length][layout[0].length];
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				
				boolean nowateraround=true;
				
				for(int k=-1; k<2; k++){
					for(int l=-1; l<2; l++){
						if(i+k>=0 && i+k<layout.length && j+l>=0 && j+l<layout[0].length){
							if(layout[i+k][j+l]==LandType.water){
								nowateraround=false;
								k=2;
								break;
							}
						}
					}
				}	
				
				if(nowateraround){
					layout[i][j]=LandType.grass;
					if(objects[i][j]==null){
						if((new Random()).nextInt(5)<2){
								if(treemap[treeindex]==null){
									treemap[treeindex]=new FractalTree();
								}
								
								objects[i][j]=treemap[treeindex];
								treeindex=(treeindex+1)%treemap.length;
						}
					}
				}
			}	
		}
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]!=LandType.water && (new Random()).nextInt(20)==0){
					int rnd=(new Random()).nextInt(20);
					objects[i][j]=new LandObject();
					if(rnd<10)
						objects[i][j].inventoryGive(Item.stick , 1+(new Random()).nextInt(2));
					else if(rnd<18)
						objects[i][j].inventoryGive(Item.berries , 1+(new Random()).nextInt(2));
					else
						objects[i][j].inventoryGive(Item.stone , 1+(new Random()).nextInt(2));
					
				}
			}
		}
		
		landplayer=new LandPlayer();
		landplayer.x=layout.length/2;
		landplayer.y=layout[0].length/2;
		landplayer.inventoryGive(Item.stick , 0);
		
		while(layout[landplayer.x][landplayer.y]!=LandType.dirt){
			landplayer.x=(new Random()).nextInt(layout.length);
			landplayer.y=(new Random()).nextInt(layout[0].length);
		}
		
		objects[landplayer.x][landplayer.y]=landplayer;
	}
	
	public boolean updateLand(){
		int	px=landplayer.x;
		int	py=landplayer.y;
		landplayer.wantedMove();
		
		{
		boolean moved=false;
		if(px!=landplayer.x || py!=landplayer.y)
			if(landplayer.x>=0 && landplayer.x<layout.length && landplayer.y>=0 && landplayer.y<layout[0].length){
				if(layout[landplayer.x][landplayer.y]!=LandType.water){
					if(objects[landplayer.x][landplayer.y]==null || !(
					 objects[landplayer.x][landplayer.y].getClass()==Tree.class ||
					!objects[landplayer.x][landplayer.y].getClass().isAssignableFrom(Tree.class)
					)){
						objects[landplayer.x][landplayer.y]=objects[px][py];
						objects[px][py]=null;
						moved=true;
					}
				}
			}
			if(!moved){
				landplayer.x=px;
				landplayer.y=py;
			}
		}
		
		rainfall.update();
		
		return (layout[landplayer.x][landplayer.y]==LandType.dirt && KeyBoard.returnKeyPress()==KeyEvent.VK_S);		
	}
	
	public void paintOnLand(Graphics g, int screenwidth , int screenheight){
		
		int numtiles=10;
		int scalex=screenwidth/(numtiles*2);
		int scaley=screenheight/(numtiles*2);
		
		for(int i=-numtiles; i<numtiles; i++){
			for(int j=-numtiles; j<numtiles; j++){
				if(i+landplayer.x>=0 && i+landplayer.x<layout.length && j+landplayer.y>=0 && j+landplayer.y<layout[0].length){
					LandType l=layout[i+landplayer.x][j+landplayer.y];
					if(l!=LandType.water){
						g.setColor(l.getColour());
						g.fillRect((numtiles+i)*scalex,(numtiles+j)*scaley,scalex,scaley);
					}
				}
			}	
		}
		
		//painting trees
		for(int i=-numtiles; i<numtiles; i++){
			for(int j=-numtiles; j<numtiles; j++){
				if(i+landplayer.x>=0 && i+landplayer.x<layout.length && j+landplayer.y>=0 && j+landplayer.y<layout[0].length){
					
					if(objects[i+landplayer.x][j+landplayer.y]!=null){
						objects[i+landplayer.x][j+landplayer.y].paint((Graphics2D)g,i+numtiles,j+numtiles,scalex);
					}
				}
			}	
		}
		
		
		rainfall.paint(g,screenwidth,screenheight);
	}
	
	public void paintAtSea(Graphics g , int x , int y){
		if(tile!=null)
			g.drawImage(tile, x, y, (int)(layout.length*0.4) , (int)(layout[0].length*0.4) , null);
	}

}
	
	class Raindrop{
		static BufferedImage image=new BufferedImage(20,20, BufferedImage.TYPE_INT_ARGB);
		double x , y , speed , angle;
		static final int red=25, green=75, blue=125;
		
		
		static public void initializeBuffer(){
			for(int i=0; i<image.getWidth(); i++){
				for(int j=0; j<image.getHeight(); j++){
					double distance=0.35*Math.sqrt(Math.pow(9.5-i,2)+Math.pow(9.5-j,2));
					int alpha=(int)(255*Math.pow(Math.E , -distance));
					image.setRGB(i,j, (new Color(red,green,blue,alpha)).getRGB());
				}
			}
		} 
	}
	
	class Rainfall{
		boolean active=true;
		int chance=750;
		BufferedImage clouds=new BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB);
		Raindrop[] rain;
		
		public Rainfall(){
			Graphics g=clouds.getGraphics();
			g.setColor(new Color(0,25,25,35));
			g.fillRect(0,0,clouds.getWidth(),clouds.getHeight());
			Raindrop.initializeBuffer();
			rain=new Raindrop[250];
		
			for(int i=0; i<rain.length; i++){
				rain[i]=new Raindrop();
				rain[i].angle=Math.PI/2+Math.PI*0.1*(new Random()).nextDouble();
				rain[i].speed=10+5*(new Random()).nextDouble();
				rain[i].y=(new Random()).nextInt(1000);
				rain[i].x=(new Random()).nextInt(1000);
			}	
		}
		
		public void update(){
			for(int i=0; i<rain.length; i++){
				if(active && rain[i].y>1000){
					rain[i].y=-(new Random()).nextInt(1000);
					rain[i].x=-100+(new Random()).nextInt(1000);
				}else{
					rain[i].x+=rain[i].speed*Math.cos(rain[i].angle);
					rain[i].y+=rain[i].speed*Math.sin(rain[i].angle);
				}
			}
			active= (new Random()).nextInt(chance)==0 ? !active : active;
		}
		
		public void paint(Graphics g , int screenwidth, int screenheight){
			for(int i=0; i<rain.length; i++){
				g.drawImage(Raindrop.image, (int)rain[i].x , (int)rain[i].y , null);
			}
			if(active && (new Random()).nextInt(10)!=0)
				g.drawImage(clouds,0,0,null);
		}
	}
