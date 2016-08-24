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
	ArrayList<InventoryTransformer> transformers;
	
	public Inventory(){
		items=new EnumMap<Item,Integer>(Item.class);
		transformers=new ArrayList<InventoryTransformer>();
	}
	
	public Inventory(Item newitem, Integer amount){
		items=new EnumMap<Item,Integer>(Item.class);
		give(newitem , amount);
		transformers=new ArrayList<InventoryTransformer>();
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
	
	public void paint(Graphics g, int x, int y, int tilesize){
		g.drawImage(getSprite(), x*tilesize,  y*tilesize, tilesize, tilesize,null);
	}
	
	public Inventory returnInventory(){
		return this;
	}
	
	public void addTransformer(InventoryTransformer transformer){
		transformers.add(transformer);
	}
	
	//should add outbound items
	public boolean updateTransformPossible(){
		
		for(InventoryTransformer transformer : transformers){
			
			if(transformer.countdown())return true;
			
			
			if(items.containsKey(transformer.getInbound())){
				Integer amount=items.get(transformer.getInbound());
				
				if(amount>1){
					items.put(transformer.getInbound(), amount-1);
					return true;
				}else{
					items.remove(transformer.getInbound());
				}
			}	
		}	
		return false;
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
	static final int rate=350;
	int timer=rate;
	
	public InventoryTransformer(Item inbound, Item outbound){
		this.inbound=inbound;
		this.outbound=outbound;
	}
	
	public boolean countdown(){
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
		rtn.addTransformer(new InventoryTransformer(Item.berries, null));//transformer could be static
		return rtn;
	}
	
	public static Inventory createHumanoidInventory(){
		
		Inventory rtn=new Inventory();
		rtn.give(Item.berries , 10+(new Random()).nextInt(5));
		rtn.addTransformer(new InventoryTransformer(Item.berries, null));//transformer could be static
		return rtn;
	}

}
