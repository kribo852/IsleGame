import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
//import java.lang.ReflectiveOperationException.InvocationTargetException;

abstract class Assembler{
	
	static final Inventory pfib= new Inventory(Item.plantfiber,1);
	static final Inventory stone=new Inventory(Item.stone     ,1);
	static final Inventory stick=new Inventory(Item.stick     ,1);
	static final Inventory rop=new Inventory(Item.rope     ,1);
	static final Inventory log=new Inventory(Item.log     ,1);
	static final Inventory reed=new Inventory(Item.reed     ,1);
	
	static class Recipe{
		
		public Recipe(){}
		
		Inventory[][] recipeinventory;
		
		public boolean lessOrEqual(final LandObject[][] map, int x, int y){
			for(int i=0; i<recipeinventory.length; i++){
				for(int j=0; j<recipeinventory[i].length; j++){
					if(i+x>=0 && i+x<map.length && j+y>=0 && j+y<map[0].length){
						
						Inventory tmp=new Inventory();
						
						if(map[i+x][j+y]==null || map[i+x][j+y].getClass()!=InventoryHolder.class){
						
						}else{
							tmp.give(map[i+x][j+y].returnInventory());
						} 
							
							if(recipeinventory[i][j]!=null && tmp.LessItems(recipeinventory[i][j])){
								return false;
							}else{
								
							}
							
					}else{
						return false;
					}
				}
			}
			return true;
		}
		
		public void SubtractItems(final LandObject[][] map, int x, int y){
			for(int i=0; i<recipeinventory.length; i++){
				for(int j=0; j<recipeinventory[i].length; j++){
					if(i+x>=0 && i+x<map.length && j+y>=0 && j+y<map[0].length){
						
						Inventory tmp=new Inventory();
						//this is strange
						if(map[i+x][j+y]==null || map[i+x][j+y].getClass()!=InventoryHolder.class){
						
						}else{
							tmp.give(map[i+x][j+y].returnInventory());
						} 
							
						if(recipeinventory[i][j]!=null){
							tmp.subtractOthersItems(recipeinventory[i][j]);
							if(!tmp.returnItems().isEmpty())
								map[i+x][j+y].inventoryGive(tmp);
							else
								map[i+x][j+y]=null;
						}
					}
				}
			}
		}
		
		public int getWidth(){return recipeinventory.length;}
		public int getHeight(){return recipeinventory[0].length;}
		
		public void paint(int yoffset, int imagesize,  Graphics g){
			
			for(int i=0; i<recipeinventory.length; i++)for(int j=0; j<recipeinventory[i].length; j++){
				if(recipeinventory[i][j]!=null)
					g.drawImage(recipeinventory[i][j].getSprite() , i*imagesize, yoffset+j*imagesize , null);
			}
			g.setColor(Color.green);
			g.drawString(""+describe(), 200, 200);		
		}
		
		public String describe(){return "";}
		
	}
	
	
	public static void craft(LandObject[][] map, int x, int y){};
	
}

// a class that transforms items in crafting formations on the ground
class BuildingAssembler extends Assembler{
	
	
	static class BuildingRecipe extends Recipe{
	
		private Building building;
		
		public void setBuilding(Building building){this.building=building;}
		
		public Building getBuilding(){return building;}
	
		public String describe(){return building.getClass().getName();}
	}
	
	static BuildingRecipe createBoatRecipe(){
		BuildingRecipe rtn=new BuildingRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{rop,log,rop},{stick,null,stick},
			{rop,stick,rop}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setBuilding(new Boat());
		return rtn;
	}
	
	static BuildingRecipe createFireRecipe(){
		BuildingRecipe rtn=new BuildingRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{stone,log,stone},{log,log,log},{stone,log,stone}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setBuilding(new FirePlace());
		return rtn;
	}
	
	static BuildingRecipe createHouse1Recipe(){
		BuildingRecipe rtn=new BuildingRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{reed,stick,log},{reed,stick,stick},{reed,stick,log}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setBuilding(new House());
		return rtn;
	}
	
	static BuildingRecipe createHouse2Recipe(){
		BuildingRecipe rtn=new BuildingRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{null,stick,stick,log},{stick,stick,stick,log},{null,stick,stick,log}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setBuilding(new House());
		return rtn;
	}
	
	static BuildingRecipe createTorchRecipe(){
		BuildingRecipe rtn=new BuildingRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{pfib,null,null},{pfib,stick,stick},{pfib,null,null}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setBuilding(new Torch());
		return rtn;
	}
	
	static BuildingRecipe createPalisadeRecipe(){
		BuildingRecipe rtn=new BuildingRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{stick,null,stick},{null,stick,null},{stick,null,stick}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setBuilding(new Palisade());
		return rtn;
	}
	
	//too bad that this manipulates map
	public static void craft(LandObject[][] map, int x, int y){
		for(Method m:BuildingAssembler.class.getDeclaredMethods())
			if(m.getReturnType() == BuildingRecipe.class){
				try{
					Object o=m.invoke(null,new Object[0]);
					if(craftBuilding(map,x,y,((BuildingRecipe)o)))return;
				}catch(IllegalAccessException iae){
				}catch(InvocationTargetException ite){}
			}
	}
	
	//this is in common between the Assemblers
	private static boolean craftBuilding(LandObject[][] map, int x, int y, BuildingRecipe r){
			
		for(int i=0; i>=-r.getWidth(); i--){
			for(int j=0; j>=-r.getHeight(); j--){
				if(r.lessOrEqual(map, x+i, y+j)){
					r.SubtractItems(map, x+i, y+j);
					if(map[x][y]==null){//problematic if items dissapear
						map[x][y]=r.getBuilding();
						
						if(r.getBuilding() instanceof Torch){
							map[x][y]=new Torch(x,y);
						}else if(r.getBuilding() instanceof FirePlace)
							map[x][y]=new FirePlace(x,y);
						
						System.out.println("crafting");
						return true;
					}
				}
			}	
		}
		return false;
	}
	
}

// a class that transforms items in crafting formations on the ground
class ItemAssembler extends Assembler{
	
	
	static class ItemRecipe extends Recipe{
		
		Inventory outbound;//the returned item(s)
		
		public void setReturnItem(Inventory outbound){
			this.outbound=outbound;
		}
		
		public Inventory returnItems(){
			Inventory rtn=new Inventory();
			rtn.give(outbound);
			return rtn;
		}
		
		public String describe(){return outbound.describe();}
	}
	
	 static ItemRecipe createStoneAxeRecipe(){
		ItemRecipe rtn=new ItemRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{pfib,null,null},{stone,stick,null},{pfib,null,null}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setReturnItem(new Inventory(Item.stoneaxe,1));
		return rtn;
	}
	
	static ItemRecipe createRopeRecipe(){
		ItemRecipe rtn=new ItemRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{pfib,pfib,pfib,pfib}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setReturnItem(new Inventory(Item.rope,1));
		return rtn;
	}
	
	static ItemRecipe createFishnetRecipe(){
		ItemRecipe rtn=new ItemRecipe();
		Inventory[][] recipeinventory=new Inventory[][]{{rop,null,rop,null},{null,rop,null,rop},
			{rop,null,rop,null},{null,rop,null,rop}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setReturnItem(new Inventory(Item.fishnet,1));
		return rtn;
	}
	
	//too bad that this manipulates map
	public static void craft(LandObject[][] map, int x, int y){
		
		for(Method m:ItemAssembler.class.getDeclaredMethods())
			if(m.getReturnType() == ItemRecipe.class){
				try{
					Object o=m.invoke(null,new Object[0]);
					if(craftItem(map,x,y,((ItemRecipe)o)))return;
				}catch(IllegalAccessException iae){
					System.err.println("reflection IllegalAccessException");
				}catch(InvocationTargetException ite){
					System.err.println("reflection InvocationTargetException");
				}
			}
	}
	
	private static boolean craftItem(LandObject[][] map, int x, int y, ItemRecipe r){
		
		
		for(int i=0; i>=-r.getWidth(); i--){
			for(int j=0; j>=-r.getHeight(); j--){
				if(r.lessOrEqual(map, x+i, y+j)){
					r.SubtractItems(map, x+i, y+j);
					if(map[x][y]==null)
						map[x][y]=new InventoryHolder();
						
					map[x][y].inventoryGive(r.returnItems());
					System.out.println("crafting");
					return true;
				}
			}	
		}
		return false;
	}
	
}

//These can be shown at any time, sea or land
class ShowCrafteables{
	
	Assembler items;
	Assembler buildings;
	static Graphics graphics;
	ArrayList<Method> recipemethods;
	
	
	public ShowCrafteables(){
		items=new ItemAssembler();
		buildings=new BuildingAssembler();
		recipemethods=new ArrayList<Method>();
		
		for(Method m:ItemAssembler.class.getDeclaredMethods()){
			if(m.getReturnType() == ItemAssembler.ItemRecipe.class){
				recipemethods.add(m);
			}
		}
		
		for(Method m:BuildingAssembler.class.getDeclaredMethods()){
			if(m.getReturnType() == BuildingAssembler.BuildingRecipe.class){
				recipemethods.add(m);
			}
		}
		
	}
	
	public void main(){
		
		if(KeyBoard.returnKeyPress()!=KeyEvent.VK_ENTER)
			return;
			
			
			int placearray=0;
		while(true){
			
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_ESCAPE)
				break;
			
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_LEFT){
				placearray--;
				if(placearray<0)placearray=0;
			}
			
			if(KeyBoard.returnKeyPress()==KeyEvent.VK_RIGHT){
				placearray++;
				if(placearray>=recipemethods.size())placearray=recipemethods.size()-1;
			}
			
			Method m=recipemethods.get(placearray);
			
			
			paint(m);
			try {
				Thread.sleep(Math.max(0, 75));
			} catch(InterruptedException ex) {
			
			}
		}
		
	}
	
	public static void setGrapihcs(Graphics g){
		graphics=g;
	}
	
	
	public void paint(Method m){
		
		graphics.setColor(new Color(50,75,35));
		graphics.fillRect(0,0,400,400);
		try{
			Object o=m.invoke(null,new Object[0]);
			((Assembler.Recipe)o).paint(25,32,graphics);
			
		}catch(IllegalAccessException iae){
			System.err.println("reflection IllegalAccessException");
		}catch(InvocationTargetException ite){
			System.err.println("reflection InvocationTargetException");
		}	
	}
}
