import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.lang.Runnable;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.Graphics2D;
import java.util.Collection;

class Isle implements Runnable{
	
	int islesize=320;
	LandType[][] layout;
	LandObject[][] objects;
	
	int[][] distance_to_sea;
	BufferedImage tile;
	int seedx,seedy;//used to generate diff islands
	LandPlayer landplayer;//null if at sea
	Rainfall rainfall;
	ArrayList<Humanoid> population;
	SingletonTreeFactory singletontreefactory;
	static Random RND=new Random();
	
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
		islandCreateFloodFill(layout, islesize/2, islesize/2,0);
		
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
	// the depth is for not passing the heapspace depth shenanigan
	protected boolean islandCreateFloodFill(LandType[][] layout, int x , int y, int depth){
			
		ArrayList<Tuple<Integer>> progress=new ArrayList<Tuple<Integer>>();
	
		progress.add(new Tuple<Integer>(x , y));
		
		while(!progress.isEmpty()){
			
			Tuple<Integer> tupel=progress.remove(0);
			
			for(int i=-1; i<2; i++){
				for(int j=-1; j<2; j++){
					if(Math.abs(i)+Math.abs(j)==1){
						int newx=tupel.a+i;
						int newy=tupel.b+j;
						
						if(!insideMapPos(newx ,newy))
							return true;
						
						if(layout[newx][newy]==LandType.water){
							layout[newx][newy]=LandType.dirt;
							progress.add(new Tuple<Integer>(newx , newy));
						}
						
						if((depth<80 && Map.randomNumber(newx*seedx , newy*seedy)%299==0) || (depth>=80 && Map.randomNumber(newx*seedx , newy*seedy)%697==0)){
							layout[newx][newy]=LandType.dirt;
							if(islandCreateFloodFill(layout, newx, newy,depth+1))
								return true;
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
		
		objects=new LandObject[layout.length][layout[0].length];
		population=new ArrayList<Humanoid>();
		singletontreefactory=new SingletonTreeFactory();
		
		distance_to_sea=calculateSeaDistance(layout);
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				
				boolean nowateraround=true;
				
				for(int k=-1; k<2; k++){
					for(int l=-1; l<2; l++){
						if(insideMapPos(i+k , j+l)){
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
						double treeChance=Math.exp(-0.05*distance_to_sea[i][j]);
						if(RND.nextDouble()>treeChance){
							objects[i][j]=singletontreefactory.getTree(true);		
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
					if(RND.nextInt(200)==0){
						int rnd=RND.nextInt(20);
						objects[i][j]=new LandObject();
						objects[i][j].inventoryGive(InventoryFactory.createGroundInventory());
						
					
					}else if(RND.nextInt(1000)==0){
						Humanoid h=new TribesHumaniod();
						objects[i][j]=h;
						h.setX(i);
						h.setY(j);
						population.add(h);
					}
				}
			}
		}
		
		landplayer=new LandPlayer();
		landplayer.setX(layout.length/2);
		landplayer.setY(layout[0].length/2);
		
		while(layout[landplayer.x][landplayer.y]!=LandType.dirt){
			landplayer.setX(RND.nextInt(layout.length));
			landplayer.setY(RND.nextInt(layout[0].length));
		}
		
		objects[landplayer.getX()][landplayer.getY()]=landplayer;
		population.add(landplayer);
		rainfall=new Rainfall();
		new Thread(rainfall).start();
	}
	
	public boolean updateLand(){
		
		{
			
			int treex=RND.nextInt(layout.length);
			int treey=RND.nextInt(layout[0].length);
			if(RND.nextInt(3)==0){
				if(objects[treex][treey]!=null && isTree(treex, treey)){
					objects[treex][treey]=singletontreefactory.getDeadTreeOfType(objects[treex][treey].getClass());
				}
			}else{
				if(layout[treex][treey]==LandType.grass && objects[treex][treey]==null){
					double sumtrees=0, activesquares=0;
					ArrayList<Class> livingtrees=new ArrayList<Class>();
					
					for(int i=-2; i<3; i++){
						for(int j=-2; j<3; j++){
							if(insideMapPos(treex+i, treey+j) && objects[treex+i][treey+j]!=null){
								++activesquares;
								if(isTree(treex+i, treey+j)){
									++sumtrees;
									if(((FractalTree)objects[treex+i][treey+j]).isAlive()){
										livingtrees.add(objects[treex+i][treey+j].getClass());
									}
								}
							}
						}
					}
					double treeChance=Math.exp(-0.05*distance_to_sea[treex][treey]);
					if(sumtrees/activesquares<treeChance && !livingtrees.isEmpty()){
						objects[treex][treey]=singletontreefactory.getLiveTreeOfType(livingtrees.get(RND.nextInt(livingtrees.size())));
					}
				}
			}
		}
		
		updateHumanoidMovements(population);
		{
			Inventory tmp=landplayer.updateInventory();
			if(tmp!=null){
				if(insideMapPos(landplayer.getPlaceX(), landplayer.getPlaceY()) &&
				layout[landplayer.getPlaceX()][landplayer.getPlaceY()]!=LandType.water){
					//empty placeholding landbjects with inventoies should never exist and hence are not concerned
					if(objects[landplayer.getPlaceX()][landplayer.getPlaceY()]==null){
						objects[landplayer.getPlaceX()][landplayer.getPlaceY()]=new LandObject();
						objects[landplayer.getPlaceX()][landplayer.getPlaceY()].inventoryGive(tmp);
						ItemAssembler.craftItem(objects,landplayer.getPlaceX(), landplayer.getPlaceY());
					}
					else{
						landplayer.inventoryGive(tmp);
					}
					
				}else{
					landplayer.inventoryGive(tmp);
				}
			}
		}
		
		{//tree cutting
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_SPACE){
				if(landplayer.activeItem()==Item.stoneaxe){
					if(insideMapPos(landplayer.getPlaceX(), landplayer.getPlaceY())){
						if(objects[landplayer.getPlaceX()][landplayer.getPlaceY()]!=null)
							if(isTree(landplayer.getPlaceX(),landplayer.getPlaceY())){
								Inventory dropped=objects[landplayer.getPlaceX()][landplayer.getPlaceY()].returnInventory();
								objects[landplayer.getPlaceX()][landplayer.getPlaceY()]=new LandObject();
								objects[landplayer.getPlaceX()][landplayer.getPlaceY()].inventoryGive(dropped);
						}	
					}
				}
			}
		}
		
		if(rainfall.update()){
			new Thread(rainfall).start();
		}	
		return (distance_to_sea[landplayer.x][landplayer.y]==1 && KeyBoard.returnKeyPress()==KeyEvent.VK_S);		
	}
	
	public void updateHumanoidMovements(final Collection<Humanoid> population){
		
		for(Humanoid human: population){
			int cposx=human.getX();
			int cposy=human.getY();	
			human.wantedMove(this);
		
			boolean moved=false;
			if(cposx!=human.getX() || cposy!=human.getY())
				if(insideMapPos(human.getX() , human.getY()) && validMovePosition(human.getX() , human.getY())){
					//items picked up by an inhabitant
					if(objects[human.getX()][human.getY()]!=null){
						objects[cposx][cposy].inventoryGive(objects[human.getX()][human.getY()].returnInventory());
					}
				
					objects[human.getX()][human.getY()]=objects[cposx][cposy];
					objects[cposx][cposy]=null;
					moved=true;
					
				}else{
					human.setX(cposx);
					human.setY(cposy);
				}
		}
	}
	
	boolean validMovePosition(int x, int y){
		if(layout[x][y]!=LandType.water){
					if(objects[x][y]==null || (!isTree(x,y) && !isHumanoid(x,y))){
						return true;
					}
			}
		return false;
	}
	
	boolean isTree(int x, int y){
		Class c=objects[x][y].getClass();
		return (c==Tree.class || c==FractalTree.class || c==PineTree.class || c==FractalBush.class);
	}
	
	boolean isHumanoid(int x, int y){
		Class c=objects[x][y].getClass();
		return (c==LandPlayer.class || c==TribesHumaniod.class);
		}
	
	//trying to standardize these functions
	boolean insideMapPos(int x, int y){return(x>=0 && x<layout.length && y>=0 && y<layout[x].length);}
	
	public void paintOnLand(Graphics g, int screenwidth , int screenheight){
		
		int numtiles=10;
		int scalex=screenwidth/(numtiles*2);
		int scaley=screenheight/(numtiles*2);
		
		for(int i=-numtiles; i<numtiles; i++){
			for(int j=-numtiles; j<numtiles; j++){
				if(insideMapPos(i+landplayer.x,j+landplayer.y)){
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
				if(insideMapPos(i+landplayer.x,j+landplayer.y)){
					
					if(objects[i+landplayer.x][j+landplayer.y]!=null){
						
						if(isTree(i+landplayer.x,j+landplayer.y) && objects[i+landplayer.x][j+landplayer.y].getClass()!=Tree.class){
							double angle=rainfall.getWindAngle()+Math.PI*(i+landplayer.x+(j+landplayer.y)/3.0)/10;
							angle=(Math.PI/12)*Math.sin(angle);
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
		Tooltip.paintFadingTip(g);
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
						if(insideMapPos(tmp[0]+i,tmp[1]+j)){
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
