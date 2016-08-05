import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map.Entry;

class Inventory{
	
	ArrayList<ItemWrapper> items;
	protected static final int spritesize=32;
	double norm=1;//indicates how much of the inventory items will be transformed by transformers
	Transformer[] transformers;
	
	class ItemWrapper{
		Item item;
		int amount;
	}
	
	public Inventory(){
		items=new ArrayList<ItemWrapper>();
	}
	
	public void give(Item newitem , int amount){
		
		if(amount==0)
			return;
		
		for(ItemWrapper itemwrapper: items){
			if(itemwrapper.item==newitem){
				itemwrapper.amount+=amount;
				return;
			}
		}
		ItemWrapper tmp=new ItemWrapper();
		tmp.item=newitem;
		tmp.amount=amount;
		items.add(tmp);
	}
	
	public void give(Inventory inventory){
		
		for(ItemWrapper tmp: inventory.returnItems()){
			give(tmp.item, tmp.amount);
		}
	}
	
	public ArrayList<ItemWrapper> returnItems(){
		return items;
	}
	
	public BufferedImage getSprite(){
		if(items!=null && !items.isEmpty())
			return items.get(0).item.getImage();
		
		return null;
	}
	
	public void shuffle(){
		ArrayList<ItemWrapper> tmp=new ArrayList<ItemWrapper>();
		
		while(!items.isEmpty())
			tmp.add(items.remove((new Random()).nextInt(items.size())));
			
			items=tmp;
	}
}

class PlayerInventory extends Inventory{
	
	int activeindex=0;

	public Inventory update(){
		
		if(KeyBoard.returnKeyPress()==KeyEvent.VK_E && items!=null && !items.isEmpty()){
				 
			ItemWrapper tmp=items.get(activeindex);
			
			if(tmp.amount>0){
				Inventory rtn=new Inventory();
				tmp.amount--;
				rtn.give(tmp.item, 1);
				
				if(tmp.amount==0){
					items.remove(activeindex);
					if(activeindex!=0 && activeindex>=items.size())
						activeindex--;
				}		
				return rtn;
			}
			return null;
		}
		
		if(KeyBoard.returnKeyPress()==KeyEvent.VK_R && items!=null && !items.isEmpty()){
			activeindex=(activeindex+1)%items.size();
		}
			
		return null;
	}
	
	public final Item returnActive(){
		if(items==null || items.size()<=activeindex)
			return null;
			
		return items.get(activeindex).item;
	}
	
	public void paint(Graphics g){
		
		int index=0;
		
		for(ItemWrapper i: items){
			g.setColor(new Color(50,35,25));
			g.fillRect(index*spritesize, 36, spritesize, spritesize);
			g.drawImage(i.item.getImage(), index*spritesize, 36 , spritesize, spritesize , null);
			g.setColor(Color.green);
			g.drawString(""+i.amount, index*spritesize, 48);
			++index;
		}
		
		((Graphics2D)g).setStroke(new BasicStroke(1));
		g.setColor(Color.green);
		g.drawRect(activeindex*spritesize, 36, spritesize, spritesize);
		
	}
	
}

//for looking up items for crafting
class RecipeInventory extends Inventory{
	
	HashMap<Item, Integer> items;
	
	public RecipeInventory(){
		items=new HashMap<Item, Integer>();
	}
	
	public RecipeInventory(Item item, int amount){
		items=new HashMap<Item, Integer>();
		items.put(item,amount);
	}
	
	public void give(Item newitem , int amount){
		
		if(amount==0)
			return;
		
		if(items.containsKey(newitem)){
			items.put(newitem, amount+items.get(newitem));
		}else{
			items.put(newitem, amount);
		}
	}
	
	public ArrayList<ItemWrapper> returnItems(){
		ArrayList<ItemWrapper> rtn=new ArrayList<ItemWrapper>();
		
		for(Entry<Item, Integer> entry :items.entrySet()){
			ItemWrapper tmp=new ItemWrapper();
			tmp.item=entry.getKey();
			tmp.amount=entry.getValue();
			rtn.add(tmp);
		}
		return rtn;
	}
	
	public HashMap<Item, Integer> returnMapedItems(){
		return items;
	}
	
	public BufferedImage getSprite(){
		if(items!=null && !items.isEmpty())
			return returnItems().get(0).item.getImage();
		
		return null;
	}
	
	//this inventory contains less or equal of all items compared to the other one
	public boolean LessEqualItems(final RecipeInventory other){
		
		HashMap<Item, Integer> compareto=other.returnMapedItems();
		
		for(ItemWrapper itemwrapper: returnItems()){
			if(!compareto.containsKey(itemwrapper.item)){
				if(itemwrapper.amount>0)
					return false;
			}else{
				if(itemwrapper.amount>compareto.get(itemwrapper.item)){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void subtractOthersItems(final RecipeInventory other){
		
		for(ItemWrapper itemwrapper: other.returnItems()){
			items.put(itemwrapper.item, items.get(itemwrapper.item)-itemwrapper.amount);
				
			if(items.get(itemwrapper.item)<=0){
				items.remove(itemwrapper.item);
			}	
		}
	}
}

// a class that holds information about item transformations inside inventories
// for example, fireplace inventories might consume wood, plantfibers, berries, fish
// and produce cooked fish and charcoal(and light, but that is not an item).
//humaniods might consume food for survival.
class InventoryTransformer{
	Item inbound=null, outbount=null;
	
}

class InventoryFactory{
	
	public static Inventory createTreeInventory(){
		Inventory rtn=new Inventory();
		rtn.give(Item.plantfiber , (new Random()).nextInt(3));
		rtn.give(Item.stick , (new Random()).nextInt(3));
			
		rtn.shuffle();
		
		return rtn;	
	}
	
	public static Inventory createPineInventory(){	
		Inventory rtn=new Inventory();
		rtn.give(Item.stick , (new Random()).nextInt(3));
		rtn.give(Item.log , (new Random()).nextInt(2));
			
		rtn.shuffle();
		
		return rtn;
	}
	
	public static Inventory createBushInventory(){	
		Inventory rtn=new Inventory();
		rtn.give(Item.plantfiber , (new Random()).nextInt(5));
		if((new Random()).nextInt(5)==0)
			rtn.give(Item.berries , 5+(new Random()).nextInt(25));
			
		rtn.shuffle();
		
		return rtn;
	}
	
	public static Inventory createGroundInventory(){
		Inventory rtn=createTreeInventory();
		rtn.give(createBushInventory());
		if((new Random()).nextInt(2)==0)
			rtn.give(Item.stone,  1+(new Random()).nextInt(2));
		return rtn;
		
	}//currently a proxy, should be remade
	
	public static PlayerInventory createPlayerInventory(){
		
		PlayerInventory rtn=new PlayerInventory();
		rtn.give(Item.plantfiber , 1+(new Random()).nextInt(2));
		rtn.give(Item.stick , 1+(new Random()).nextInt(2));
		rtn.give(Item.berries , 1+(new Random()).nextInt(10));
		return rtn;
	}
	
	
	
}

// a class that transforms items in crafting formations on the ground
class ItemAssembler{
	
	static final RecipeInventory pfib= new RecipeInventory(Item.plantfiber,1);
	static final RecipeInventory stone=new RecipeInventory(Item.stone     ,1);
	static final RecipeInventory stick=new RecipeInventory(Item.stick     ,1);
	static final RecipeInventory rop=new RecipeInventory(Item.rope     ,1);
	
	static final Recipe stoneaxerecipe=createStoneAxeRecipe();
	static final Recipe roperecipe=createRopeRecipe();
	static final Recipe fishnetrecipe=createFishnetRecipe();
	
	//second definition of the same class
	static class ItemWrapper{
		Item item;
		int amount;
		
		public ItemWrapper(Item item, int amount){
			this.item=item;
			this.amount=amount;
		}
	}
	
	static class Recipe{
		
		public Recipe(){
			outbound=new ArrayList<ItemWrapper>();
		}
		
		RecipeInventory[][] recipeinventory;
		ArrayList<ItemWrapper> outbound;//the returned
		
		public boolean lessOrEqual(final LandObject[][] map, int x, int y){
			for(int i=0; i<recipeinventory.length; i++){
				for(int j=0; j<recipeinventory[i].length; j++){
					if(i+x>=0 && i+x<map.length && j+y>=0 && j+y<map[0].length){
						
						RecipeInventory tmp=new RecipeInventory();
						
						if(map[i+x][j+y]==null || map[i+x][j+y].getClass()!=LandObject.class){
						
						}else{
							tmp.give(map[i+x][j+y].returnInventory());
						} 
							
							if(recipeinventory[i][j]!=null && !recipeinventory[i][j].LessEqualItems(tmp)){
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
						
						RecipeInventory tmp=new RecipeInventory();
						//this is strange
						if(map[i+x][j+y]==null || map[i+x][j+y].getClass()!=LandObject.class){
						
						}else{
							tmp.give(map[i+x][j+y].returnInventory());
						} 
							
						if(recipeinventory[i][j]!=null){
							map[i+x][j+y]=new LandObject();
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
		
		public Inventory returnItems(){
			Inventory rtn=new Inventory();
			
			for(ItemWrapper itemwrapper : outbound){
				rtn.give(itemwrapper.item, itemwrapper.amount);
			} 
			return rtn;
		}
		
	}
	
	 static Recipe createStoneAxeRecipe(){
		Recipe rtn=new Recipe();
		RecipeInventory[][] recipeinventory=new RecipeInventory[][]{{pfib,null,null},{stone,stick,null},{pfib,null,null}}; 
		
		rtn.recipeinventory=recipeinventory;
		ItemWrapper tmpwrapper=new ItemWrapper(Item.stoneaxe,1);
		rtn.outbound.add(tmpwrapper);
		
		return rtn;
	}
	
	static Recipe createRopeRecipe(){
		Recipe rtn=new Recipe();
		RecipeInventory[][] recipeinventory=new RecipeInventory[][]{{pfib,pfib,pfib,pfib}}; 
		
		rtn.recipeinventory=recipeinventory;
		ItemWrapper tmpwrapper=new ItemWrapper(Item.rope,1);
		rtn.outbound.add(tmpwrapper);
		
		return rtn;
	}
	
	static Recipe createFishnetRecipe(){
		Recipe rtn=new Recipe();
		RecipeInventory[][] recipeinventory=new RecipeInventory[][]{{rop,null,rop,null},{null,rop,null,rop},
			{rop,null,rop,null},{null,rop,null,rop}}; 
		
		rtn.recipeinventory=recipeinventory;
		ItemWrapper tmpwrapper=new ItemWrapper(Item.fishnet,1);
		rtn.outbound.add(tmpwrapper);
		
		return rtn;
	}
	
	//too bad that is manipulates map
	public static void craftItem(LandObject[][] map, int x, int y){
		
		if(craftItem(map,x,y,stoneaxerecipe))
			return;
			
		if(craftItem(map,x,y,roperecipe))
			return;
			
		if(craftItem(map,x,y,fishnetrecipe))
			return;
	}
	
	private static boolean craftItem(LandObject[][] map, int x, int y, Recipe r){
		
		for(int i=0; i>=-r.getWidth(); i--){
			for(int j=0; j>=-r.getHeight(); j--){
				if(r.lessOrEqual(map, x+i, y+j)){
					r.SubtractItems(map, x+i, y+j);
					if(map[x][y]==null)
						map[x][y]=new LandObject();
						
					map[x][y].inventoryGive(r.returnItems());
					return true;
				}
			}	
		}
		return false;
	}
}
