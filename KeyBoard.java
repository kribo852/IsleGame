import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

final class KeyBoard implements KeyListener{
	
		private static double deltaspeed=1;
		
		private static double deltatheta=0;
		
		private static int savedkeypress=-1;
		
		private static int savedkeytyped=-1;
	
	public void keyPressed(KeyEvent e){
		
		savedkeypress=e.getKeyCode();
		
		if(e.getKeyCode()==e.VK_UP)
			deltaspeed*=0.99;
			
		if(e.getKeyCode()==e.VK_RIGHT)
			deltatheta=Math.PI/40;
			
		if(e.getKeyCode()==e.VK_LEFT)
			deltatheta=-Math.PI/40;
		
	}
	
	public void keyReleased(KeyEvent e){
		
		if(e.getKeyCode()==savedkeypress)
			savedkeypress=-1;
		
		if(e.getKeyCode()==e.VK_UP)
			deltaspeed=1;
		
		if(e.getKeyCode()==e.VK_RIGHT || e.getKeyCode()==e.VK_LEFT)
			deltatheta=0;	
		
	}
	
	public void keyTyped(KeyEvent e){
		savedkeytyped=e.getKeyCode();
	}
	
	
	public static double returnSpeed() {return (1.0-deltaspeed);} 
	public static double returnTheta() {return deltatheta;} 
	
	public static int returnKeyPress() {return savedkeypress;}
	public static int returnKeyTyped() {return savedkeytyped;} 
}
