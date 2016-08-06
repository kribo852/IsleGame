import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

//contains in this order
//AI persons walk randomly
//AI persons collect items
//AI stores items in deposits, shares them (food among other things)
//NPC:s escapes darkness and rain
// one per island-> not static 
//how to output both person to be moved and its new position

class AICoordinator{

HashSet<Integer> AIpositions;
int[] savedmove=null;

public AICoordinator(){

}


//returns the current and wanted next position of a player
int[] update(final LandObject[][] map){
	
	if(AIpositions==null){
		AIpositions=new HashSet<Integer>();//remove later
		searchForUnits(map);
	}else{
		if(savedmove!=null)
			if(map[savedmove[2]][savedmove[3]]!=null && map[savedmove[2]][savedmove[3]].getClass()==TribesHumaniod.class){
				AIpositions.remove(transform(savedmove[0],savedmove[1]));
				AIpositions.add(transform(savedmove[2],savedmove[3]));
			}
	}
	
	Integer[] positions=AIpositions.toArray(new Integer[0]);
	
	if(positions.length==0)
		return null;
	
	int index=(new Random()).nextInt(positions.length);
	int x=positions[index]&0x0000FFFF;
	int y=positions[index]>>16;
	
	int nx=x-1+(new Random()).nextInt(3);
	int ny=y-1+(new Random()).nextInt(3);
	
	while(nx<0 || nx>=map.length || ny<0 || ny>=map[nx].length || AIpositions.contains(transform(nx,ny))){
		nx=x-1+(new Random()).nextInt(3);
		ny=y-1+(new Random()).nextInt(3);
	}
	
	int[] newposition=new int[]{x,y,nx,ny};
	
	savedmove=newposition;
	
	return newposition;
}

private void searchForUnits(final LandObject[][] map){
	for(int i=0; i<map.length; i++){
		for(int j=0; j<map[i].length; j++){
			if(map[i][j]!=null)
				if(map[i][j].getClass()==TribesHumaniod.class){
					AIpositions.add(transform(i,j));
			}
		}
	}
}

Integer transform(int x, int y){
	return (y<<16)+x; 
}

}
