import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;

class Map{

final int sizex ,sizey;//
Isle[][] ilands;
int old_player_x, old_player_y;
static final int seed=23;
Player seaplayer;


public Map(){

	sizey=sizex=20;	
	ilands=new Isle[sizex][sizey];
	refreshAll();
	seaplayer=new Player();
	LandTexture.initialize(40);
}

	
	public void paint(Graphics g, int screenwidth , int screenheight){
		
		if(updateLanding()!=null){
			paintOnLand(g,screenwidth,screenheight);
		}else{
			paintAtSea(g, screenwidth, screenheight);
		}
	}
	
	public void paintAtSea(Graphics g, int screenwidth , int screenheight){
		
		double scalex=screenwidth/sizex;
		double scaley=screenheight/sizey;
		
		for(int i=0; i<sizex; i++){
			for(int j=0; j<sizey; j++){
				if(ilands[i][j]!=null){
					
					ilands[i][j].paintAtSea(g , (int)((i-seaplayer.getX()%1)*scalex) , (int)((j-seaplayer.getY()%1)*scaley));
				}
			}	
		}
		
		g.setColor(Color.black);
		g.fillRect(395,395, 10 , 10);
		g.drawLine(400, 400, 
		400+(int)(10*Math.cos(seaplayer.returnTheta())), 400+(int)(10*Math.sin(seaplayer.returnTheta())));
	}
	
	public void paintOnLand(Graphics g, int screenwidth , int screenheight){
		if(ilands[sizex/2-1][sizey/2-1]!=null)
			ilands[sizex/2-1][sizey/2-1].paintOnLand(g,screenwidth,screenheight);
	}
	
	public void update(){
		
		Isle i=updateLanding();
		if(i!=null){
			if(i.updateLand()){
				System.out.println("##############");
				leaveIsland();
			}
			
		}else
			refresh();
		
	}
	
	public void leaveIsland(){
		seaplayer.x++;
		refresh();//so that the movement takes effect
		//potential teardown, not sure if needed
	}
	
	public void refresh(){
		
		seaplayer.resetDirectionPosition(KeyBoard.returnSpeed() , sizex , sizey);
		seaplayer.resetAngle(KeyBoard.returnTheta());
	
		int playerx=(int)seaplayer.getX();
		int playery=(int)seaplayer.getY();
	
		if(playerx>old_player_x){
			old_player_x=playerx;
			for(int i=0; i<sizex-1; i++){
				ilands[i]=ilands[i+1];		
			}
			
			ilands[sizex-1]=new Isle[sizey];
			
			for(int j=0; j<sizey; j++){
				refreshSquare(sizex-1 , j);	
			}
		}
		
		if(playerx<old_player_x){
			old_player_x=playerx;
			for(int i=sizex-1; i>0; i--){
				ilands[i]=ilands[i-1];		
			}
			
			ilands[0]=new Isle[sizey];
			
			for(int j=0; j<sizey; j++){
				refreshSquare(0 , j);	
			}
		}
		
		if(playery>old_player_y){
			old_player_y=playery;
			for(int j=0; j<sizey-1; j++){
				for(int i=0; i<sizex; i++){
					ilands[i][j]=ilands[i][j+1];		
				}
			}
			
			for(int i=0; i<sizex; i++){
				refreshSquare(i , sizey-1);	
			}
		}
		
		if(playery<old_player_y){
			old_player_y=playery;
			for(int j=sizey-1; j>0; j--){
				for(int i=0; i<sizey; i++){
					ilands[i][j]=ilands[i][j-1];		
				}
			}
			
			for(int i=0; i<sizex; i++){
				refreshSquare(i , 0);	
			}
		}
	}
	
	public void refreshAll(){
		for(int i=0; i<sizex; i++){
			for(int j=0; j<sizey; j++){
				refreshSquare(i,j);
				
			}
		}	
	}
	
	public void refreshSquare(int x , int y){
		if(randomNumber(x+old_player_x, y+old_player_y)%350==0){
			ilands[x][y]=new Isle(x+old_player_x, y+old_player_y, 320);
			new Thread(ilands[x][y]).start();	
		}else if(x+old_player_x==(sizex/2)-1 && y+old_player_y==(sizey/2)-1){
			ilands[x][y]=new Isle(x+old_player_x, y+old_player_y, 160);
			new Thread(ilands[x][y]).start();
		}else{
			ilands[x][y]=null;
		}
	}
	
	public int getSizeX(){
		return sizex;
	}
	
	public int getSizeY(){
		return sizey;
	}
	
	//pseudo random number that assures that layouts doesn't change
	public static long randomNumber(int x , int y){
		String s=y+""+x+""+seed;
		return s.hashCode();
	}
	
	/**
	 * this horribly named method returns true while the player is on land
	 * and initializes an isle if player comes ashore
	 */
	public Isle updateLanding(){
		
		if(ilands[sizex/2-1][sizey/2-1]!=null)
			ilands[sizex/2-1][sizey/2-1].initializeLand();
		
		return ilands[sizex/2-1][sizey/2-1];	
	}
}
