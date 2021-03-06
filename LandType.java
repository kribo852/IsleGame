import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.Graphics2D;

enum LandType{
	grass(0, 100, 150 ,50),water(1, 25, 100, 150),sand(2 , 160 ,150 , 130),clay(3, 150,160,175);
	
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
	
	 BufferedImage[] grasstiles;
	 BufferedImage[] sandtiles;
	 BufferedImage claytile;
	 static final byte GRASS_BUFFER_SIZE=23;
	 
	 public LandTexture(int size, boolean daytextures){
		grasstiles=new BufferedImage[GRASS_BUFFER_SIZE];
		sandtiles=new BufferedImage[7];
		
		{
		BufferedImage tmp=makeGrassTexture(size, LandType.grass.getColour());
			for(int i=0; i<grasstiles.length; i++){
				grasstiles[i]=new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				Graphics g=grasstiles[i].getGraphics();
				g.drawImage(tmp , 0,0,size,size,
				0, (i*size),size,(i*size)+size ,null);
			}
		}
			
		for(int i=0; i<sandtiles.length; i++)
			sandtiles[i]=makeNoisyTexture(size, LandType.sand.getColour());
			
			claytile=makeNoisyTexture(size, LandType.clay.getColour());
			
		if(!daytextures)
				useAsNightTextures();
	}
	
	private BufferedImage makeSandTexture(int size, Color groundcolour){
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
	
	private BufferedImage makeClayTexture(int size, Color groundcolour){
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
	
	private BufferedImage makeNoisyTexture(int size, Color groundcolour){
		BufferedImage rtn=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				
				double strength=0.9+(new Random()).nextDouble()*0.2;
				Color c=groundcolour;
				
				int red=(int)(c.getRed()*strength);
				int green=(int)(c.getGreen()*strength);
				int blue=(int)(c.getBlue()*strength);
				
				rtn.setRGB(i,j,(new Color(red, green, blue)).getRGB());
			}	
		}
		return rtn;
	}
	
	//the double painting is done to prevent horizontal lines due to cuttof in grassstraw generations
	private BufferedImage makeGrassTexture(int size, Color groundcolour){
		BufferedImage rtn=new BufferedImage(size,GRASS_BUFFER_SIZE*size,BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0; i<size; i++){
			for(int j=0; j<rtn.getHeight(); j++){
				rtn.setRGB(i,j,groundcolour.getRGB());
			}	
		}
		
		for(int iteration=0; iteration<2250; iteration++){
			
			int x=((new Random()).nextInt(size));
			int y=((new Random()).nextInt(rtn.getHeight()));
			int length=8+((new Random()).nextInt(8));
			
			for(int i=0; i<length && i<rtn.getHeight(); i++){
				Color c=new Color(rtn.getRGB(x , (y+i)%rtn.getHeight()));
				rtn.setRGB(x , (y+i)%rtn.getHeight() , c.darker().getRGB());
			}	
		}
		
		return rtn;
	}
	
	public BufferedImage getbuffer(int x, int y, LandType l){
		
		int seed=x+1+x*y;
		
		if(l==LandType.grass){
			seed=y+x*x+x;

			return grasstiles[seed%GRASS_BUFFER_SIZE];
		}
		else if(l==LandType.sand){
			seed%=sandtiles.length;
			return sandtiles[seed];
		}else if(l==LandType.clay)
			return claytile;
		
		return null;
	}
	
	private void useAsNightTextures(){
		Color mask=new Color(25,35,50,120);
		for(BufferedImage image: grasstiles){
			
			Graphics2D g=image.createGraphics();
			g.setColor(mask);
			g.fillRect(0,0,image.getWidth(), image.getHeight());
		}
		
		for(BufferedImage image: sandtiles){
			
			Graphics2D g=image.createGraphics();
			g.setColor(mask);
			g.fillRect(0,0,image.getWidth(), image.getHeight());
		}
		Graphics2D g=claytile.createGraphics();
		g.setColor(mask);
		g.fillRect(0,0,claytile.getWidth(), claytile.getHeight());
	}
}
