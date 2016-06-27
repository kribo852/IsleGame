import java.awt.Graphics;
import java.awt.Color;

enum LandType{
	grass(0, 75, 150 ,50),water(1, 25, 100, 150),dirt(2 , 150 ,150 , 25);
	
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

};
