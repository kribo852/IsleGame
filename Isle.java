import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.lang.Runnable;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.Graphics2D;

class Isle implements Runnable{
	
int islesize=320;
LandType[][] layout;
LandObject[][] objects;

int[][] distance_to_sea;
BufferedImage tile;
int seedx,seedy;//used to generate diff islands
LandPlayer landplayer;//null if at sea
Rainfall rainfall;
AICoordinator aicoordinator;

	class Tuple<Type>{
		public Type a,b;
		
		public Tuple(Type a, Type b){
			this.a=a;
			this.b=b;
		}	
	}

public Isle(int seedx, int seedy, int islesize){
		this.seedx=seedx;
		this.seedy=seedy;
		this.islesize=islesize;
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
			
		LandObject[] treemap=new LandObject[75];
		int treeindex=0;
		
		objects=new LandObject[layout.length][layout[0].length];
		
		distance_to_sea=calculateSeaDistance(layout);
		
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
						double treeChance=Math.exp(-0.025*distance_to_sea[i][j]);
						if((new Random()).nextDouble()>treeChance){
								if(treemap[treeindex]==null){
									treemap[treeindex]=new FractalTree();
								}
								
								objects[i][j]=treemap[treeindex];
								treeindex=(treeindex+1)%treemap.length;
						}
					}
				}else if(distance_to_sea[i][j]>1 && layout[i][j]==LandType.dirt){
					layout[i][j]=LandType.clay;
				}
			}	
		}
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]!=LandType.water){
					if((new Random()).nextInt(200)==0){
						int rnd=(new Random()).nextInt(20);
						objects[i][j]=new LandObject();
						objects[i][j].inventoryGive(InventoryFactory.createGroundInventory());
						
					
					}else if((new Random()).nextInt(1000)==0){
						objects[i][j]=new BushMan();
					}
				}
			}
		}
		
		landplayer=new LandPlayer();
		landplayer.x=layout.length/2;
		landplayer.y=layout[0].length/2;
		
		while(layout[landplayer.x][landplayer.y]!=LandType.dirt){
			landplayer.x=(new Random()).nextInt(layout.length);
			landplayer.y=(new Random()).nextInt(layout[0].length);
		}
		
		objects[landplayer.x][landplayer.y]=landplayer;
		aicoordinator=new AICoordinator();
		
		new Thread(rainfall).start();
	}
	
	public boolean updateLand(){
		int	px=landplayer.x;
		int	py=landplayer.y;
		landplayer.wantedMove();
		
		{
		boolean moved=false;
		if(px!=landplayer.x || py!=landplayer.y)
			if(landplayer.x>=0 && landplayer.x<layout.length && landplayer.y>=0 && landplayer.y<layout[0].length
			&& validMovePosition(landplayer.x, landplayer.y)){
					//items on this spot
					if(objects[landplayer.x][landplayer.y]!=null){
						objects[px][py].inventoryGive(objects[landplayer.x][landplayer.y].returnInventory());
					}
				
					objects[landplayer.x][landplayer.y]=objects[px][py];
					objects[px][py]=null;
					moved=true;
					
				}else{
					landplayer.x=px;
					landplayer.y=py;
				}
		}
		
		landplayer.updateInventory();
		
		int[] npcmoved=aicoordinator.update(objects);
		
			if(npcmoved!=null){
			if(objects[npcmoved[0]][npcmoved[1]].getClass()==BushMan.class){
			if(npcmoved[2]>=0 && npcmoved[2]<layout.length && npcmoved[3]>=0 && npcmoved[3]<layout[0].length)
				if(validMovePosition(npcmoved[2],npcmoved[3])){
					objects[npcmoved[2]][npcmoved[3]]=objects[npcmoved[0]][npcmoved[1]];
					objects[npcmoved[0]][npcmoved[1]]=null;
				}
			}
		}
		
		if(rainfall.update()){
			new Thread(rainfall).start();
		}	
		return (distance_to_sea[landplayer.x][landplayer.y]==1 && KeyBoard.returnKeyPress()==KeyEvent.VK_S);		
	}
	
	boolean validMovePosition(int x, int y){
		if(layout[x][y]!=LandType.water){
					if(objects[x][y]==null || !(
					 objects[x][y].getClass()==Tree.class ||
					!objects[x][y].getClass().isAssignableFrom(Tree.class)
					)){
						return true;
					}
			}
		return false;
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
						g.drawImage(LandTexture.getbuffer(i+landplayer.x,j+landplayer.y,l),(numtiles+i)*scalex,(numtiles+j)*scaley,null);//returns a semirandom square
					}
					g.setColor(Color.black);
				}
			}	
		}
		
		//painting trees
		for(int i=-numtiles; i<numtiles; i++){
			for(int j=-numtiles; j<numtiles; j++){
				if(i+landplayer.x>=0 && i+landplayer.x<layout.length && j+landplayer.y>=0 && j+landplayer.y<layout[0].length){
					
					if(objects[i+landplayer.x][j+landplayer.y]!=null){
						
						if(objects[i+landplayer.x][j+landplayer.y].getClass()==FractalTree.class){
							double angle=rainfall.getWindAngle()+Math.PI*(i+j/3.0)/50;
							angle=(Math.PI/20)*Math.sin(angle);
							((FractalTree)objects[i+landplayer.x][j+landplayer.y]).paint(g,i+numtiles,j+numtiles,scalex,angle);
						}else{
							objects[i+landplayer.x][j+landplayer.y].paint((Graphics2D)g,i+numtiles,j+numtiles,scalex);
						}
						
					}
				}
			}	
		}
			
		rainfall.paint(g,screenwidth,screenheight);
		
		if(distance_to_sea[landplayer.x][landplayer.y]==1)
			Tooltip.paintSailTip(g);
	}
	
	public void paintAtSea(Graphics g , int x , int y){
		if(tile!=null)
			g.drawImage(tile, x, y, (int)(layout.length*0.4) , (int)(layout[0].length*0.4) , null);
	}
	
	int[][] calculateSeaDistance(LandType[][] layout){
		int[][] rtn=new int[layout.length][layout[0].length];
		ArrayList<int[]> positions=new ArrayList<int[]>();
		for(int i=0; i<rtn.length; i++){
			for(int j=0; j<rtn[i].length; j++){
				rtn[i][j]=-1;
				
				if(i==0 || j==0 || i==rtn.length-1 || j==rtn[0].length-1){
					
					if(layout[i][j]==LandType.water){
						rtn[i][j]=0;
					}else{
						rtn[i][j]=1;
					}
					
					positions.add(new int[]{i,j});
				}
				
			}	
		}
		
		while(!positions.isEmpty()){
			int[] tmp=positions.remove(0);
			
			for(int i=-1; i<2; i++)
				for(int j=-1; j<2; j++){
					if(i!=0 || j!=0){
					
						if(tmp[0]+i>=0 && tmp[0]+i<rtn.length && 
						   tmp[1]+j>=0 && tmp[1]+j<rtn[0].length){
						
							if(rtn[tmp[0]+i][tmp[1]+j]==-1){
								if(rtn[tmp[0]][tmp[1]]==0 && layout[tmp[0]+i][tmp[1]+j]==LandType.water){
									rtn[tmp[0]+i][tmp[1]+j]=0;
								}else{
									rtn[tmp[0]+i][tmp[1]+j]=rtn[tmp[0]][tmp[1]]+1;
								}
								positions.add(new int[]{tmp[0]+i, tmp[1]+j});
							
							}else if(rtn[tmp[0]+i][tmp[1]+j]>rtn[tmp[0]][tmp[1]]+1){
								if(rtn[tmp[0]][tmp[1]]==0 && layout[tmp[0]+i][tmp[1]+j]==LandType.water){
									rtn[tmp[0]+i][tmp[1]+j]=0;
								}else{
									rtn[tmp[0]+i][tmp[1]+j]=rtn[tmp[0]][tmp[1]]+1;
								}
								positions.add(new int[]{tmp[0]+i, tmp[1]+j});
							}
						}
					}	
				}	
		}
		return rtn;
	}
}
	
	class Raindrop{
		static BufferedImage image=new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
		double x , y , dx, dy;
		static final Color raincoloured=new Color(25,100,85);
		static final Color firecoloured=new Color(200,185,25);
		
		
		static public void initializeBuffer(){
			for(int i=0; i<image.getWidth(); i++){
				for(int j=0; j<image.getHeight(); j++){
					double distance=0.25*Math.sqrt(Math.pow(image.getWidth()/2-i-0.5,2)+Math.pow(image.getWidth()/2-j-0.5,2));
					int alpha=(int)(200*Math.pow(Math.E , -distance));
					Color c=new Color(raincoloured.getRGB());
					c=new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
					image.setRGB(i,j, c.getRGB());
				}
			}
		} 
	}
	
	class Rainfall implements Runnable{
		boolean active=true;
		int timer=500;
		BufferedImage rainbuffer[]=new BufferedImage[2];
		Raindrop[] rain;
		double windangle=0;//used for sinus winds in trees
		int currentframe=0;
		boolean nextframeready=false;;
		
		public Rainfall(){
			Raindrop.initializeBuffer();
			rain=new Raindrop[750];
		
			for(int i=0; i<rain.length; i++){
				rain[i]=new Raindrop();
				double angle=Math.PI/2+Math.PI*0.2*(new Random()).nextDouble();
				double speed=10+6*(new Random()).nextDouble();
				rain[i].dx=speed*Math.cos(angle);
				rain[i].dy=speed*Math.sin(angle);
				rain[i].y=(new Random()).nextInt(1000);
				rain[i].x=(new Random()).nextInt(1000);
			}	
			
			rainbuffer[0]=new BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB);
			rainbuffer[1]=new BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB);
		}
		
		public void run(){
			int updateframe= (currentframe+1)%rainbuffer.length;
			
			for(int i=0; i<rain.length; i++){
				if(active && rain[i].y>1000){
					rain[i].y=-(new Random()).nextInt(1000);
					rain[i].x=-100+(new Random()).nextInt(1000);
				}else{
					rain[i].x+=rain[i].dx;
					rain[i].y+=rain[i].dy;
				}
			}
			
			Graphics g=rainbuffer[updateframe].getGraphics();
			if(active)
				((Graphics2D)g).setBackground(new Color(25, 50, 50, 25));
			else
				((Graphics2D)g).setBackground(new Color(255, 255, 255, 0));
			g.clearRect(0,0,800, 800);
			
			for(int i=0; i<rain.length; i++){
				g.drawImage(Raindrop.image, (int)rain[i].x , (int)rain[i].y , null);
			}
			
			nextframeready=true;
		}
		
		public boolean update(){
			boolean rtn=nextframeready;
			if(nextframeready){
				nextframeready=false;
				currentframe=(currentframe+1)%rainbuffer.length;
			}
			
			--timer;
			if(timer<=0){
				if(active)
					timer=(new Random()).nextInt(2500);
				else
					timer=(new Random()).nextInt(750);
				
				active=!active;
			}
			windangle+=(Math.PI/100)%Math.PI;
			
			return rtn;
		}
		
		public void paint(Graphics g , int screenwidth, int screenheight){
			
			g.drawImage(rainbuffer[currentframe], 0 , 0 , null);
			
		}
		
		public double getWindAngle(){
			return windangle;
		}
	}
