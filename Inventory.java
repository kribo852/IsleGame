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
import java.util.EnumMap;
import java.util.Set;

class Inventory{
	
	EnumMap<Item,Integer> items;
	protected static final int spritesize=32;
	InventoryTransformer[] transformers;
	
	public Inventory(){
		items=new EnumMap<Item,Integer>(Item.class);
	}
	
	public Inventory(Item newitem, Integer amount){
		items=new EnumMap<Item,Integer>(Item.class);
		give(newitem , amount);
	}
	
	public void give(Item newitem , int amount){
		
		if(amount==0)
			return;
			
		if(items.containsKey(newitem)){
			items.put(newitem, items.get(newitem)+amount);
		}else{
			items.put(newitem, amount);
		}
	}
	
	public void give(Inventory inventory){
		
		for(Entry< Item ,Integer> tmp: inventory.returnItems()){
			give(tmp.getKey(), tmp.getValue());
		}
	}
	
	public Set<Entry<Item ,Integer>> returnItems(){
		return items.entrySet();
	}
	
	public boolean contains(Item item){
		return items.containsKey(item);
	}
	
	protected ArrayList<Entry<Item,Integer>> getAsArrayList(){
		ArrayList<Entry<Item,Integer>> rtn=new ArrayList<Entry<Item,Integer>>();
		rtn.addAll(items.entrySet());
		return rtn;
	}
	
	//this inventory contains less  of one item compared to other
	public boolean LessItems(final Inventory other){
		
		for(Entry<Item , Integer> entry: other.returnItems()){
			if(!items.containsKey(entry.getKey())){
				if(entry.getValue()>0)
					return true;
			}else{
				if(entry.getValue()>items.get(entry.getKey())){
					return true;
				}
			}
		}
		return false;
	}
	
	public void subtractOthersItems(final Inventory other){
		
		for(Entry<Item ,Integer> entry: other.returnItems()){
			items.put(entry.getKey(), items.get(entry.getKey())-entry.getValue());
				
			if(items.get(entry.getKey())<=0){
				items.remove(entry.getKey());
			}	
		}
	}
	
	public BufferedImage getSprite(){
		if(items!=null && !items.isEmpty())
			return getAsArrayList().get(0).getKey().getImage();
		
		return null;
	}
}

class PlayerInventory extends Inventory{
	
	int activeindex=0;
	
	public PlayerInventory(){
		super();
	}

	public Inventory update(){
		
		if(KeyBoard.returnKeyPress()==KeyEvent.VK_E && items!=null && !items.isEmpty()){
				 
			Entry<Item,Integer> tmp=getAsArrayList().get(activeindex);
			
			if(tmp.getValue()>0){
				Inventory rtn=new Inventory();
				rtn.give(tmp.getKey(), 1);
				
				if(tmp.getValue()==1){
					items.remove(tmp.getKey());
					if(activeindex!=0 && activeindex>=items.size())
						activeindex--;
				}else{
					items.put(tmp.getKey(), tmp.getValue()-1);
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
	
	public Item returnActive(){
		if(items==null || items.size()<=activeindex)
			return null;
		
		return getAsArrayList().get(activeindex).getKey();
	}
	
	public void paint(Graphics g){
		
		int index=0;
		
		for(Entry<Item,Integer> i: items.entrySet()){
			g.setColor(new Color(50,35,25));
			g.fillRect(index*spritesize, 36, spritesize, spritesize);
			g.drawImage(i.getKey().getImage(), index*spritesize, 36 , spritesize, spritesize , null);
			g.setColor(Color.green);
			g.drawString(""+i.getValue(), index*spritesize, 48);
			++index;
		}
		
		((Graphics2D)g).setStroke(new BasicStroke(1));
		g.setColor(Color.green);
		g.drawRect(activeindex*spritesize, 36, spritesize, spritesize);
		if(items!=null && !items.isEmpty())
			g.drawString(""+getAsArrayList().get(activeindex).getKey(), index*spritesize, 64);
		
	}
	
}

// a class that holds information about item transformations inside inventories
// for example, fireplace inventories might consume wood, plantfibers, berries, fish
// and produce cooked fish and charcoal(and light, but that is not an item).
//humaniods might consume food for survival.
class InventoryTransformer{
	Item inbound=null, outbound=null;
	static final int rate=1000;
	int timer=rate;
	
	public InventoryTransformer(Item inbound, Item outbound){
		this.inbound=inbound;
		this.outbound=outbound;
	}
	
	public boolean transform(){
		if(--timer<0){
			timer=rate;
			return false;
		}else return true;
	}
	
	public Item getInbound(){
		return inbound;
	}
	
	public Item getOutbound(){
		return outbound;
	}
}

class InventoryFactory{
	
	public static Inventory createTreeInventory(){
		Inventory rtn=new Inventory();
		rtn.give(Item.plantfiber , (new Random()).nextInt(3));
		rtn.give(Item.stick , (new Random()).nextInt(3));
		
		return rtn;	
	}
	
	public static Inventory createPineInventory(){	
		Inventory rtn=new Inventory();
		rtn.give(Item.stick , (new Random()).nextInt(3));
		rtn.give(Item.log , (new Random()).nextInt(2));
			
		return rtn;
	}
	
	public static Inventory createBushInventory(){	
		Inventory rtn=new Inventory();
		rtn.give(Item.plantfiber , (new Random()).nextInt(5));
		if((new Random()).nextInt(5)==0)
			rtn.give(Item.berries , 10+(new Random()).nextInt(5));
			
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
		rtn.give(Item.berries , 5+(new Random()).nextInt(5));
		return rtn;
	}
	
	
	
}

// a class that transforms items in crafting formations on the ground
final class ItemAssembler{
	
	static final Inventory pfib= new Inventory(Item.plantfiber,1);
	static final Inventory stone=new Inventory(Item.stone     ,1);
	static final Inventory stick=new Inventory(Item.stick     ,1);
	static final Inventory rop=new Inventory(Item.rope     ,1);
	
	static final Inventory stone_axe=new Inventory(Item.stoneaxe     ,1);
	
	static class Recipe{
		
		public Recipe(){
		
		}
		
		public void setReturnItem(Inventory outbound){
			this.outbound=outbound;
		}
		
		Inventory[][] recipeinventory;
		Inventory outbound;//the returned item(s)
		
		public boolean lessOrEqual(final LandObject[][] map, int x, int y){
			for(int i=0; i<recipeinventory.length; i++){
				for(int j=0; j<recipeinventory[i].length; j++){
					if(i+x>=0 && i+x<map.length && j+y>=0 && j+y<map[0].length){
						
						Inventory tmp=new Inventory();
						
						if(map[i+x][j+y]==null || map[i+x][j+y].getClass()!=LandObject.class){
						
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
			rtn.give(outbound);
			return rtn;
		}
	}
	
	 static Recipe createStoneAxeRecipe(){
		Recipe rtn=new Recipe();
		Inventory[][] recipeinventory=new Inventory[][]{{pfib,null,null},{stone,stick,null},{pfib,null,null}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setReturnItem(new Inventory(Item.stoneaxe,1));
		return rtn;
	}
	
	static Recipe createRopeRecipe(){
		Recipe rtn=new Recipe();
		Inventory[][] recipeinventory=new Inventory[][]{{pfib,pfib,pfib,pfib}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setReturnItem(new Inventory(Item.rope,1));
		return rtn;
	}
	
	static Recipe createFishnetRecipe(){
		Recipe rtn=new Recipe();
		Inventory[][] recipeinventory=new Inventory[][]{{rop,null,rop,null},{null,rop,null,rop},
			{rop,null,rop,null},{null,rop,null,rop}}; 
		
		rtn.recipeinventory=recipeinventory;
		rtn.setReturnItem(new Inventory(Item.fishnet,1));
		return rtn;
	}
	
	//too bad that this manipulates map
	public static void craftItem(LandObject[][] map, int x, int y){
		
		if(craftItem(map,x,y,createStoneAxeRecipe()))
			return;
			
		if(craftItem(map,x,y,createRopeRecipe()))
			return;
			
		if(craftItem(map,x,y,createFishnetRecipe()))
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
