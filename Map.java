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
	SeaCamera.initializeCamera(seaplayer.getX(), seaplayer.getY());
}

	
	public void paint(Graphics g, int screenwidth , int screenheight){
		
		Isle isleonposition=updateLanding();
		
		if(isleonposition!=null){
			isleonposition.paintOnLand(g,screenwidth,screenheight);
		}else{
			paintAtSea(g, screenwidth, screenheight);
		}
	}
	
	public void paintAtSea(Graphics g, int screenwidth , int screenheight){
		
		double scalex=screenwidth/sizex;
		double scaley=screenheight/sizey;
		
		int tmpx=(int)(scalex*(seaplayer.getX()-SeaCamera.getCameraX()));
		int tmpy=(int)(scaley*(seaplayer.getY()-SeaCamera.getCameraY()));
		
		g.setColor(Color.green.darker());
		
		for(int i=0; i<sizex; i++){
			for(int j=0; j<sizey; j++){
				if(ilands[i][j]!=null){
					ilands[i][j].paintAtSea(g , (int)((i-SeaCamera.getCameraX()%1)*scalex) , (int)((j-SeaCamera.getCameraY()%1)*scaley));
					g.fillOval((int)((i+1-SeaCamera.getCameraX()%1)*scalex) , (int)((j+1-SeaCamera.getCameraY()%1)*scaley),4,4);
				}
			}	
		}
		
		g.setColor(Color.black);
		g.fillRect(395+tmpx,395+tmpy, 10 , 10);
		g.drawLine(400+tmpx, 400+tmpy, 
		400+tmpx+(int)(10*Math.cos(seaplayer.returnTheta())), 400+tmpy+(int)(10*Math.sin(seaplayer.returnTheta())));
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
		SeaCamera.initializeCamera(seaplayer.getX(), seaplayer.getY());
		refresh();//so that the movement takes effect
		//potential teardown, not sure if needed
	}
	
	public void refresh(){
		
		seaplayer.resetDirectionPosition(KeyBoard.returnSpeed() , sizex , sizey);
		seaplayer.resetAngle(KeyBoard.returnTheta());
		
		SeaCamera.update(seaplayer.getX(), seaplayer.getY());
	
		int playerx=(int)SeaCamera.getCameraX();
		int playery=(int)SeaCamera.getCameraY();
	
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
		
		int tmpx=sizey/2-1+(int)(seaplayer.getX()-SeaCamera.getCameraX());
		int tmpy=sizey/2-1+(int)(seaplayer.getY()-SeaCamera.getCameraY());
		
		if(ilands[tmpx][tmpy]!=null)
			ilands[tmpx][tmpy].initializeLand();
		
		return ilands[tmpx][tmpy];	
	}
}
