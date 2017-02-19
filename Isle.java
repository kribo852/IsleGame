import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.lang.Runnable;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;

class Isle implements Runnable{
	
	int islesize=320;
	LandType[][] layout;
	LandObject[][] objects;
	
	short[][] distance_to_sea;
	BufferedImage tile;
	int seedx,seedy;//used to generate diff islands
	LandPlayer landplayer;//null if at sea
	ArrayList<Humanoid> population;
	ArrayList<Ghost> ghosts;
	ArrayList<House> houses;
	
	SingletonTreeFactory singletontreefactory;
	static Random RND=new Random();
	static LandTexture daytextures=new LandTexture(40, true);//40 is the tilesize
	static LandTexture nighttextures=new LandTexture(40, false);
	Growth growth=null;
	
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
		growth=new Growth();
	}

	//generates island in new thread
	public void run(){
		layout=new LandType[islesize][islesize]; 
		for(int i=0; i<islesize; i++){
			for(int j=0; j<islesize; j++){
				layout[i][j]=LandType.water;
			}
		}
		
		layout[islesize/2][islesize/2]=LandType.sand;
		islandCreateFloodFill(layout, islesize/2, islesize/2,0);
		
		layout=slimArray(layout);
		
		tile=new BufferedImage(layout.length , layout[0].length , BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]==LandType.sand){
					tile.setRGB(i, j, LandType.sand.getColour().getRGB());
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
							layout[newx][newy]=LandType.sand;
							progress.add(new Tuple<Integer>(newx , newy));
						}
						
						if((depth<80 && Map.randomNumber(newx*seedx , newy*seedy)%299==0) || (depth>=80 && Map.randomNumber(newx*seedx , newy*seedy)%697==0)){
							layout[newx][newy]=LandType.sand;
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
		int minlandx=layout.length;
		int minlandy=layout[0].length;
		int maxlandx=0;
		int maxlandy=0;
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]==LandType.sand){
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
		houses=new ArrayList<House>();
		singletontreefactory=new SingletonTreeFactory();
		
		distance_to_sea=calculateSeaDistance(layout);
		growth.setDistance(distance_to_sea);
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				
				boolean nowateraround=true;
				
				if(i==0 || i==layout.length-1 || j==0 || j==layout[0].length){
					nowateraround=false;
				}else{
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
				}
				
				if(nowateraround){
					layout[i][j]=LandType.grass;
					if(isEmpty(i,j)){
						if(RND.nextDouble()<growth.treeChance(i,j)){
							objects[i][j]=singletontreefactory.getTree(true);		
						}
						else if(RND.nextInt(1250)==0){
							objects[i][j]=new FirePlace(i,j);
						}
					}
				}else if(distance_to_sea[i][j]>1 && layout[i][j]==LandType.sand){
					if(RND.nextBoolean())
						layout[i][j]=LandType.clay;
					else{
						layout[i][j]=LandType.grass;
						if(RND.nextDouble()<growth.treeChance(i,j)){
							objects[i][j]=singletontreefactory.getTree(true);		
						}
					}
					if(isEmpty(i,j) && RND.nextInt(4)==0){
						objects[i][j]=new Reed();
					}
				}
			}	
		}
		
		for(int i=0; i<layout.length; i++){
			for(int j=0; j<layout[0].length; j++){
				if(layout[i][j]!=LandType.water){
					if(RND.nextInt(200)==0){
						objects[i][j]=new InventoryHolder();
						objects[i][j].inventoryGive(InventoryFactory.createGroundInventory());
					
					}else if(RND.nextInt(1000)==0){
						Humanoid h=new TribesHumanoid(true);
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
		
		while(layout[landplayer.x][landplayer.y]!=LandType.sand){
			landplayer.setX(RND.nextInt(layout.length));
			landplayer.setY(RND.nextInt(layout[0].length));
		}
		
		objects[landplayer.getX()][landplayer.getY()]=landplayer;
		population.add(landplayer);
		
		ghosts=new ArrayList<Ghost>();
	}
	
	public boolean updateLand(){
		
		for(short[] s:growth.getGrowPos()){
			if(isEmpty(s[0],s[1])){
				if(layout[s[0]][s[1]]==LandType.grass){
					ArrayList<Class> livingtrees=findSeedTrees(s[0],s[1]);
					if(!livingtrees.isEmpty())
						if(RND.nextInt(20)!=0)
							objects[s[0]][s[1]]=singletontreefactory.getLiveTreeOfType(livingtrees.get(RND.nextInt(livingtrees.size())));
						else {
							objects[s[0]][s[1]]=new InventoryHolder();
							objects[s[0]][s[1]].inventoryGive(InventoryFactory.createMushroomInventory());
						}
						
				}else if(layout[s[0]][s[1]]==LandType.sand){
					objects[s[0]][s[1]]=new InventoryHolder();
					objects[s[0]][s[1]].inventoryGive(InventoryFactory.createShellInventory());
				}
			}
		}
		
		for(short[] s:growth.getDiePos()){
			if(isTree(s[0],s[1])){
				objects[s[0]][s[1]]=singletontreefactory.getDeadTreeOfType(objects[s[0]][s[1]].getClass());
			}
		}
		
		Thread t=new Thread(growth);//restart the growth cycle for the next iteration
		t.start();
		
		updateHumanoids(population);
		{
			Inventory tmp=landplayer.updateInventory();
			if(tmp!=null){
				if(insideMapPos(landplayer.getPlaceX(), landplayer.getPlaceY()) &&
				layout[landplayer.getPlaceX()][landplayer.getPlaceY()]!=LandType.water){
					//empty placeholding landbjects with inventories should never exist and hence are not concerned
					if(objects[landplayer.getPlaceX()][landplayer.getPlaceY()]==null){
						objects[landplayer.getPlaceX()][landplayer.getPlaceY()]=new InventoryHolder();
						objects[landplayer.getPlaceX()][landplayer.getPlaceY()].inventoryGive(tmp);
						ItemAssembler.craft(objects,landplayer.getPlaceX(), landplayer.getPlaceY());
						
						//something built
						if(BuildingAssembler.craft(objects,landplayer.getPlaceX(), landplayer.getPlaceY())){
							
							if(objects[landplayer.getPlaceX()][landplayer.getPlaceY()] instanceof House){
								houses.add((House)objects[landplayer.getPlaceX()][landplayer.getPlaceY()]);
							}
						}
					}
					else{
						landplayer.inventoryGive(tmp);
					}
					
				}else{
					landplayer.inventoryGive(tmp);
				}
			}
		}
		
		ArrayList<Ghost> tmplist=new ArrayList<Ghost>();
		for(Ghost g: ghosts){
			g.setPosition(10);//speed
			int x=(int)g.getX(40);
			int y=(int)g.getY(40);
			if(insideMapPos(x,y)){
				if(!isPalisade(x,y)){
					tmplist.add(g);
				}
				if(!DayCycleClass.positionLit(x,y))
					removeHumanoid(x,y);
			}
			
		}
		
		ghosts=tmplist;
		double area=layout.length*layout[0].length;
		
		if(RND.nextDouble()<area/750000){
			
			if(RND.nextBoolean())
				ghosts.add(new Ghost(RND.nextInt(layout[0].length), true));
			else
				ghosts.add(new Ghost(RND.nextInt(layout.length), false));
		}
		
		return (canSail(landplayer) && KeyBoard.returnKeyPress()==KeyEvent.VK_S);		
	}
	
	public ArrayList<Class> findSeedTrees(int treex, int treey){
		double sumtrees=0, activesquares=0;
		ArrayList<Class> livingtrees=new ArrayList<Class>();
		
		for(int i=-2; i<3; i++)for(int j=-2; j<3; j++){
			if(insideMapPos(treex+i, treey+j) && !isEmpty(treex+i,treey+j)){
				++activesquares;
				if(isTree(treex+i, treey+j)){
					++sumtrees;
					if(((FractalTree)objects[treex+i][treey+j]).isAlive()){
						livingtrees.add(objects[treex+i][treey+j].getClass());
					}
				}
			}
		}
		return livingtrees;			
	}
	
	public void updateHumanoids(final ArrayList<Humanoid> population){
		
		{
		int markforremoval=-1;
			for(int i=0; i<population.size(); i++){
				if(population.get(i).starving()){
					markforremoval=i;
				}
			}
			if(markforremoval!=-1){
				Inventory tmp=objects[population.get(markforremoval).getX()][population.get(markforremoval).getY()].returnInventory();
				objects[population.get(markforremoval).getX()][population.get(markforremoval).getY()]=new InventoryHolder();
				objects[population.get(markforremoval).getX()][population.get(markforremoval).getY()].inventoryGive(tmp);
				population.remove(markforremoval);
			}
		}
		
		tryAddPopulation();
		
		for(Humanoid human: population){
			int cposx=human.getX();
			int cposy=human.getY();	
			human.wantedMove(this);
		
			boolean moved=false;
			if(cposx!=human.getX() || cposy!=human.getY())
				if(insideMapPos(human.getX() , human.getY()) && validMovePosition(human.getX() , human.getY())){
					if(isBush(human.getX(), human.getY())){
						LayeredDecorator laydec=new LayeredDecorator();
						laydec.addLayer(human);
						laydec.addLayer(objects[human.getX()][human.getY()]);
						objects[human.getX()][human.getY()]=laydec;
					}
					//items picked up by an inhabitant
					else{
					
						if(objects[human.getX()][human.getY()]!=null && objects[human.getX()][human.getY()].getClass()==InventoryHolder.class){
							objects[cposx][cposy].inventoryGive(objects[human.getX()][human.getY()].returnInventory());
						}
						objects[human.getX()][human.getY()]=human;
				    }
				
					if(objects[cposx][cposy].getClass()==LayeredDecorator.class){
						objects[cposx][cposy]=((LayeredDecorator)objects[cposx][cposy]).removeLayer(human);
					}else{
						objects[cposx][cposy]=null;
				    }
					moved=true;
					
				}else{
					human.setX(cposx);
					human.setY(cposy);
				}
				
			//treecutting
			if(human.getAction()){
				if(insideMapPos(human.getPlaceX(), human.getPlaceY())){
					if(objects[human.getPlaceX()][human.getPlaceY()]!=null){
						
						if(isBush(human.getPlaceX(),human.getPlaceY())){
							Inventory dropped=objects[human.getPlaceX()][human.getPlaceY()].returnInventory();
								objects[human.getPlaceX()][human.getPlaceY()]=new InventoryHolder();
								objects[human.getPlaceX()][human.getPlaceY()].inventoryGive(dropped);
						}
						else if(isTree(human.getPlaceX(),human.getPlaceY()) && human.itemActive(Item.stoneaxe)){
							Inventory dropped=objects[human.getPlaceX()][human.getPlaceY()].returnInventory();
								objects[human.getPlaceX()][human.getPlaceY()]=new InventoryHolder();
								objects[human.getPlaceX()][human.getPlaceY()].inventoryGive(dropped);
						}
					}//fishing
					if(distance_to_sea[human.getPlaceX()][human.getPlaceY()]==0 && human.itemActive(Item.fishnet)){
						if(RND.nextInt(10)==0)human.inventoryGive(new Inventory(Item.fish,1));
					}
				}
			}	
		}
	}
	
	boolean validMovePosition(int x, int y){
		if(layout[x][y]!=LandType.water){
					if(isEmpty(x,y) || ((!isTree(x,y) || isBush(x,y)) && !isHumanoid(x,y) && !isBuilding(x,y))){
						return true;
					}
			}
		return false;
	}
	
	boolean isEmpty(int x, int y){
		return objects[x][y]==null;
	}
	
	boolean isTree(int x, int y){
		if(isEmpty(x,y))return false;//null is not a tree
		return (objects[x][y] instanceof Tree);
	}
	
	boolean isBush(int x, int y){
		if(isEmpty(x,y))return false;//null is not a bush
		return (objects[x][y] instanceof FractalBush);
	}
	
	boolean isHumanoid(int x, int y){
		if(isEmpty(x,y))return false;
		return (objects[x][y] instanceof Humanoid);
	}
	
	boolean isBuilding(int x, int y){
		if(isEmpty(x,y))return false;
		return (objects[x][y] instanceof Building);
	}
	
	boolean isItem(int x, int y){
		if(isEmpty(x,y))return false;//null is not a bush
		return (objects[x][y] instanceof InventoryHolder);
	}
	
	boolean isPalisade(int x, int y){
		if(isEmpty(x,y))return false;//null is not a bush
		return (objects[x][y] instanceof Palisade);
	}
	
	boolean isWater(int x, int y){
		return layout[x][y]==LandType.water;
	}
	
	boolean canSail(LandPlayer h){
		
		for(int i=-1; i<2; i++){
			for(int j=-1; j<2; j++){
				if(insideMapPos(i+h.getX() , j+h.getY()) ){
					if(objects[i+h.getX()][j+h.getY()] instanceof Boat){
						if(distance_to_sea[i+h.getX()][j+h.getY()]<=1){
							return true;
						}
					}
				}
			}	
		}
		return false;
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
						
						LandTexture tmp=(DayCycleClass.positionLit(i+landplayer.x,j+landplayer.y) ? daytextures: nighttextures);
						
						g.drawImage(tmp.getbuffer(i+landplayer.x,j+landplayer.y,l),(numtiles+i)*scalex,(numtiles+j)*scaley,null);//returns a semirandom texture
					}
				}
			}	
		}
		
		//painting trees
		for(int j=-numtiles; j<=numtiles; j++){
			for(int i=-numtiles; i<numtiles; i++){
				if(insideMapPos(i+landplayer.x,j+landplayer.y)){
						
					if(objects[i+landplayer.x][j+landplayer.y]!=null){
						
						if(isTree(i+landplayer.x,j+landplayer.y) && objects[i+landplayer.x][j+landplayer.y].getClass()!=Tree.class){
							double angle=Rainfall.getWindAngle()+Math.PI*(i+landplayer.x+(j+landplayer.y)/3.0)/10;
							angle=(Math.PI/12)*Math.sin(angle);
							((FractalTree)objects[i+landplayer.x][j+landplayer.y]).paint(g,i+numtiles,j+numtiles,scalex,angle);
						}else{
							objects[i+landplayer.x][j+landplayer.y].paint((Graphics2D)g,i+numtiles,j+numtiles,scalex);
						}
					}
				}	
			}	
		}
		
		for(Ghost ghost: ghosts){
			double paintx=ghost.getX(scalex), painty=ghost.getY(scalex); 
			if(paintx-landplayer.x>=-numtiles && paintx-landplayer.x<=numtiles && painty-landplayer.y>=-numtiles && painty-landplayer.y<=numtiles){		
				if(!DayCycleClass.positionLit((int)paintx, (int)painty)){
					ghost.paint(g, landplayer.x-numtiles, landplayer.y-numtiles, scalex);
				}
			}
		}
		
		g.setColor(Color.green);
		g.drawString("population: "+population.size(), 500, 75);
		
		if(canSail(landplayer))
			Tooltip.paintSailTip(g);
		Tooltip.paintFadingTip(g);
	}
	
	public void paintAtSea(Graphics g , int x , int y){
		if(tile!=null)
			g.drawImage(tile, x, y, (int)(layout.length*0.4) , (int)(layout[0].length*0.4) , null);
	}
	
	short[][] calculateSeaDistance(LandType[][] layout){
		short[][] rtn=new short[layout.length][layout[0].length];
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
							if(rtn[tmp[0]+i][tmp[1]+j]==-1|| rtn[tmp[0]+i][tmp[1]+j]>rtn[tmp[0]][tmp[1]]+1){
								if(rtn[tmp[0]][tmp[1]]==0 && layout[tmp[0]+i][tmp[1]+j]==LandType.water){
									rtn[tmp[0]+i][tmp[1]+j]=0;
								}else{
									rtn[tmp[0]+i][tmp[1]+j]=(short)(rtn[tmp[0]][tmp[1]]+1);
								}
								positions.add(new int[]{tmp[0]+i, tmp[1]+j});
							}
						}
					}	
				}	
		}
		return rtn;
	}
	
	public boolean otherPersonClose(int x, int y, int selfx, int selfy, double radius){
		
		for(int i=(int)-radius; i<=radius; i++)for(int j=(int)-radius; j<=radius; j++){
			if( !(i+x==selfx && j+y==selfy) && insideMapPos(i+x, j+y) && isHumanoid(i+x, j+y) && Math.hypot(i, j)<radius){
				return true;
			}
		}
		
		return false;
	}
	
	public void removeHumanoid(int x, int y){
		
		if(isHumanoid(x,y)){
			Humanoid h=(Humanoid)objects[x][y];
			int index=-1;
			for(int i=0; i<population.size(); i++){
				if(population.get(i)==h){
					index=i;
					break;
				}
			}
			if(index!=-1){
				population.remove(index);
				objects[x][y]=null;
			}
		}
	}
	
	public void tryAddPopulation(){
		
		if(population.size()>1){
			if(houses.size()*10>population.size()){
				int speed=Math.max(1200/population.size(), 1);
				if(RND.nextInt(speed)==0){
					House tmp=houses.get(RND.nextInt(houses.size()));
					for(int i=-1; i<2; i++)for(int j=-1; j<2; j++){
						if(addFolk(i+tmp.getX(),j+tmp.getY())){
							return;
						}	
					}
				}
			}
		}
	}
	//reuseable at initialization
	public boolean addFolk(int x, int y){
		
		if(insideMapPos(x,y) && isEmpty(x,y) && !isWater(x,y)){//check for water?
			Humanoid h=new TribesHumanoid(false);
			objects[x][y]=h;
			h.setX(x);
			h.setY(y);
			population.add(h);
			return true;
		}
		
		return false;
	}
	
}



class Growth implements Runnable{
	
	short[][] distance_to_sea;
	ArrayList<short[]> growpositions;
	ArrayList<short[]> diepositions;
	HashMap<Integer, Double> cachedprobabilities;
	
	public Growth(){
		growpositions=new ArrayList<short[]>();
		diepositions=new ArrayList<short[]>();
		cachedprobabilities=new HashMap<Integer, Double>();
	}
	
	public void setDistance(short[][] distance_to_sea){
		this.distance_to_sea=distance_to_sea;
		for(int i=0; i<distance_to_sea.length; i++)for(int j=0; j<distance_to_sea[0].length; j++){
			cachedprobabilities.put(((int)distance_to_sea[i][j]), Math.min(0.45, 1-Math.exp(-0.042*distance_to_sea[i][j])));
		}
	}
	
	public void run(){
		
		for(short i=0; i<distance_to_sea.length; i++)for(short j=0; j<distance_to_sea[i].length; j++){	
			if(distance_to_sea[i][j]>0){
					
				if((new Random()).nextInt(50000)==0){
					diepositions.add(new short[]{i,j});
				}
				
				if( (new Random()).nextInt(25000)==0 && (new Random()).nextDouble()<treeChance(i,j) ){
					growpositions.add(new short[]{i,j});
				}
					
			}
		}
	}
	
	public double treeChance(int x, int y){
		return cachedprobabilities.get((int)distance_to_sea[x][y]);
	}
	
	public ArrayList<short[]> getGrowPos(){
		ArrayList<short[]> tmp=growpositions;
		growpositions=new ArrayList<short[]>();
		return tmp;
	}
	
	public ArrayList<short[]> getDiePos(){
		ArrayList<short[]> tmp=diepositions;
		diepositions=new ArrayList<short[]>();
		return tmp;
	}	
}
