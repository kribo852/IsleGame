import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

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
			tmp.amount--;
		}
		
		if(KeyBoard.returnKeyPress()==KeyEvent.VK_R && items!=null && !items.isEmpty()){
			activeindex=(activeindex+1)%items.size();
		}
		
		
		return null;
	}
	
	public void paint(Graphics g){
		
		int index=0;
		
		for(ItemWrapper i: items){
			g.setColor(new Color(50,35,25));
			g.fillRect(index*spritesize, 24, spritesize, spritesize);
			g.drawImage(i.item.getImage(), index*spritesize, 24 , spritesize, spritesize , null);
			g.setColor(Color.green);
			g.drawString(""+i.amount, index*spritesize, 48);
			++index;
		}
		
		((Graphics2D)g).setStroke(new BasicStroke(1));
		g.setColor(Color.green);
		g.drawRect(activeindex*spritesize, 24, spritesize, spritesize);
		
	}
	
}

class Transformer{
	Item inbound=null, outbount=null;
	
}

class InventoryFactory{
	
	public static Inventory createTreeInventory(){
		
		Inventory rtn=new Inventory();
		rtn.give(Item.plantfiber , (new Random()).nextInt(3));
		rtn.give(Item.stick , (new Random()).nextInt(3));
		if((new Random()).nextInt(15)==0)
			rtn.give(Item.berries , 50+(new Random()).nextInt(50));
			
		rtn.shuffle();
		
		return rtn;
		
	}
	
	public static Inventory createGroundInventory(){
		
		Inventory rtn=createTreeInventory();
		if((new Random()).nextInt(5)==0)
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
