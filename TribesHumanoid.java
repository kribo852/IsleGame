import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.util.Random;

class TribesHumanoid extends Humanoid{
	
	ArrayList<int[]> path;
	ArrayList<int[]> importantPositions;
	
	public TribesHumanoid(boolean fed){
		super();
		path=new ArrayList<int[]>();
		
		if(fed)
			inventory=InventoryFactory.createHumanoidInventory();
		else
			inventory=InventoryFactory.createStarvingHumanoidInventory();
	}
	
	public void paint(Graphics g, int x, int y, int tilesize){
			super.paint(g,x,y,tilesize);
			g.drawImage(sprites[0], x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
	public Inventory updateInventory(){return null;}
	
	public void wantedMove(Isle island){
		
		
		if(DayCycleClass.isDay() &&inventory.contains(Item.fishnet)){
			for(int i=-1; i<2; i++){
				for(int j=-1; j<2; j++){
					if(island.insideMapPos(getX()+i, getY()+j) && island.isWater(getX()+i, getY()+j)){
						action=true;
						placex=i;
						placey=j;
						return;
					}
				}
			}
		}
		
		Scout(island);
	}
	
	public void Scout(Isle island){
		if((new Random()).nextDouble()>0.4)
			return;
		
		ArrayList<int[]>possiblemoves=new ArrayList<int[]>();
		
		for(int i=-1; i<2; i++){
			for(int j=-1; j<2; j++){
				if(island.insideMapPos(i+x,  j+y)){
					if(!island.isEmpty(i+x,  j+y) && island.isBush(i+x,  j+y)){
						placex=i;
						placey=j;
						action=true;
						return;
					}
					
					if(island.validMovePosition(i+x,j+y) && island.isItem(i+x,j+y) && !island.otherPersonClose(i+x,j+y,x,y,4)){
						possiblemoves.add(new int[]{i+x,j+y});
					}
				}
			}
		}
		
		if(!possiblemoves.isEmpty()){
			int[] tmp=possiblemoves.get((new Random()).nextInt(possiblemoves.size()));
			x=tmp[0];
			y=tmp[1];
			return;
		}
		
		if(!path.isEmpty()){	
			int [] nextpos=path.remove(0);
			if(Math.abs(x-nextpos[0]+y-nextpos[1])<=2){
				if(!(DayCycleClass.positionLit(x,y) && !DayCycleClass.positionLit(nextpos[0],nextpos[1]))){
					x=nextpos[0];
					y=nextpos[1];
				}
				return;
			}else{
				path.clear();
			}
		}
		
		int xdir=0;
		int ydir=0;
		do{
			if((new Random()).nextInt(3)>0){
				xdir=-1+2*(new Random()).nextInt(2);
			}else{
				xdir=0;
			}
			
			if((new Random()).nextInt(3)>0){
				ydir=-1+2*(new Random()).nextInt(2);
			}else{
				ydir=0;
			}
			
		}while(xdir==0 && ydir==0);
		
		int dx=xdir, dy=ydir;
		
		while(island.insideMapPos(x+dx, y+dy) && island.validMovePosition(x+dx, y+dy)){
			path.add(new int[]{x+dx, y+dy});
			dx+=xdir;
			dy+=ydir;
		}
	}
	
}
