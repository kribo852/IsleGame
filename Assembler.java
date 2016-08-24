
abstract class Assembler{
	
	static final Inventory pfib= new Inventory(Item.plantfiber,1);
	static final Inventory stone=new Inventory(Item.stone     ,1);
	static final Inventory stick=new Inventory(Item.stick     ,1);
	static final Inventory rop=new Inventory(Item.rope     ,1);
	static final Inventory log=new Inventory(Item.log     ,1);
	
	static class Recipe{
		
		public Recipe(){
		
		}
		
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
	}
	
	
	public static void craft(LandObject[][] map, int x, int y){};
}

// a class that transforms items in crafting formations on the ground
class BuildingAssembler extends Assembler{
	
	
	static class BuildingRecipe extends Recipe{
	
		private Building building;
		
		public void setBuilding(Building building){this.building=building;}
		
		public Building getBuilding(){return building;}
	
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
	
	//too bad that this manipulates map
	public static void craft(LandObject[][] map, int x, int y){
		
		if(craftBuilding(map,x,y,createBoatRecipe()))
			return;
			
		if(craftBuilding(map,x,y,createFireRecipe()))
			return;
	}
	
	//this is in common between the Assemblers
	private static boolean craftBuilding(LandObject[][] map, int x, int y, BuildingRecipe r){
			
		for(int i=0; i>=-r.getWidth(); i--){
			for(int j=0; j>=-r.getHeight(); j--){
				if(r.lessOrEqual(map, x+i, y+j)){
					r.SubtractItems(map, x+i, y+j);
					if(map[x][y]==null){//problematic if items dissapear
						map[x][y]=r.getBuilding();
						
						if(r.getBuilding() instanceof FirePlace)
							DayCycleClass.addLitPosition(x,y,6);
						
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
		
		if(craftItem(map,x,y,createStoneAxeRecipe()))
			return;
			
		if(craftItem(map,x,y,createRopeRecipe()))
			return;
			
		if(craftItem(map,x,y,createFishnetRecipe()))
			return;
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
