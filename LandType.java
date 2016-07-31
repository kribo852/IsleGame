import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

enum LandType{
	grass(0, 65, 150 ,50),water(1, 25, 100, 150),dirt(2 , 150 ,150 , 25),clay(3, 190,185,200);
	
	private int value, red, green, blue; 
	private LandType(int value, int red, int green, int blue) {
		 this.value = value;
		 this.red=red ;
		 this.green=green;
		 this.blue=blue;
	}
	
	public Color getColour(){
		return new Color(red,green,blue);
	}
	
	public int getValue(){
		return value;
	}

}

class LandTexture{
	
	static BufferedImage[] grasstiles;
	static BufferedImage[] sandtiles;
	static BufferedImage claytile;
	
	private static BufferedImage makeTexture(int size, Color groundcolour){
		BufferedImage rtn=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				Color c=groundcolour;
				if((new Random()).nextInt(3)==0){
					c=groundcolour.darker();
				}
				if((new Random()).nextInt(3)==0){
					c=c.darker();
				}
				
				rtn.setRGB(i,j,c.getRGB());
			}	
		}
		return rtn;
	}
	
	public static void initialize(int size){
		grasstiles=new BufferedImage[97];
		sandtiles=new BufferedImage[3];
		
		for(int i=0; i<grasstiles.length; i++)
			grasstiles[i]=makeTexture(size, LandType.grass.getColour());
			
		for(int i=0; i<sandtiles.length; i++)
			sandtiles[i]=makeTexture(size, LandType.dirt.getColour());
			
			claytile=makeTexture(size, LandType.clay.getColour());
	}
	
	public static BufferedImage getbuffer(int x, int y, LandType l){
		
		int seed=x+1+x*y;
		
		if(l==LandType.grass){
			seed%=grasstiles.length;
			return grasstiles[seed];
		}
		else if(l==LandType.dirt){
			seed%=sandtiles.length;
			return sandtiles[seed];
		}else if(l==LandType.clay)
			return claytile;
		
		return null;
	}
	
}
