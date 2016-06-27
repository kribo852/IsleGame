import java.util.ArrayList;
import java.awt.image.BufferedImage;

class Inventory{
	
	ArrayList<Item> items; 
	
	public Inventory(){
		items=new ArrayList<Item>();
	}
	
	public void give(Item newitem , int amount){
		
		for(Item i: items){
			if(i==newitem){
				i.add(amount);
				return;
			}
		}
		
		newitem.add(amount);
		items.add(newitem);
		
	}
	
	public BufferedImage getSprite(){
		
		if(items!=null && !items.isEmpty())
			return items.get(0).getImage();
		
		return null;
	}
}
