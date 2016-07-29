import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

class Tooltip{



	public static void paintSailTip(Graphics g){
		g.setColor(Color.red);
		g.setFont(new Font("TimesRoman", Font.BOLD, 16)); 
		g.drawString("Press S to Sail to Sea" , 400, 400);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 10)); 

	}



}
