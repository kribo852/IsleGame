import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

class Tooltip{

	static int alphaval=255;

	public static void paintSailTip(Graphics g){
		g.setColor(Color.red);
		g.setFont(new Font("TimesRoman", Font.BOLD, 16)); 
		g.drawString("Press S to Sail to Sea" , 400, 400);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 10)); 

	}
	
	public static void paintFadingTip(Graphics g){
		if(alphaval<0)
			return;
		
		g.setColor(new Color(255,0,0,alphaval));
		--alphaval;
		g.setFont(new Font("TimesRoman", Font.BOLD, 16)); 
		g.drawString("I must help my people to survive.", 200, 200);
		g.drawString("If I a can build a boat, I can sail to other islands" , 200, 220);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 10)); 

	}



}
