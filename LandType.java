import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

enum LandType{
	grass(0, 100, 150 ,50),water(1, 25, 100, 150),sand(2 , 150 ,150 , 25),clay(3, 100,115,130);
	
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
	
	private static BufferedImage makeSandTexture(int size, Color groundcolour){
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
	
	private static BufferedImage makeClayTexture(int size, Color groundcolour){
		BufferedImage rtn=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				Color c=groundcolour;
				if((new Random()).nextInt(3)==0){
					c=groundcolour.darker();
				}
				if((new Random()).nextInt(3)==0){
					c=c.brighter();
				}
				
				rtn.setRGB(i,j,c.getRGB());
			}	
		}
		return rtn;
	}
	
	//the double painting is done to prevent horizontal lines due to cuttof in grassstraw generations
	private static BufferedImage makeGrassTexture(int size, Color groundcolour){
		BufferedImage tmp=new BufferedImage(size,2*size,BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<size; i++){
			for(int j=0; j<tmp.getHeight(); j++){
				tmp.setRGB(i,j,groundcolour.getRGB());
			}	
		}
		
		for(int iteration=0; iteration<320; iteration++){
			
			int x=((new Random()).nextInt(size));
			int y=((new Random()).nextInt(tmp.getHeight()));
			int length=2+((new Random()).nextInt(4));
			
			for(int i=0; i<length && i+y<tmp.getHeight(); i++){
				Color c=new Color(tmp.getRGB(x , y+i));
				tmp.setRGB(x , y+i , c.darker().getRGB());
			}	
		}
		
		BufferedImage rtn=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				rtn.setRGB(i,j,tmp.getRGB(i,j+size/2));
			}	
		}
		
		return rtn;
	}
	
	public static void initialize(int size){
		grasstiles=new BufferedImage[23];
		sandtiles=new BufferedImage[7];
		
		for(int i=0; i<grasstiles.length; i++)
			grasstiles[i]=makeGrassTexture(size, LandType.grass.getColour());
			
		for(int i=0; i<sandtiles.length; i++)
			sandtiles[i]=makeSandTexture(size, LandType.sand.getColour());
			
			claytile=makeClayTexture(size, LandType.clay.getColour());
	}
	
	public static BufferedImage getbuffer(int x, int y, LandType l){
		
		int seed=x+1+x*y;
		
		if(l==LandType.grass){
			seed%=grasstiles.length;
			return grasstiles[seed];
		}
		else if(l==LandType.sand){
			seed%=sandtiles.length;
			return sandtiles[seed];
		}else if(l==LandType.clay)
			return claytile;
		
		return null;
	}
	
}
