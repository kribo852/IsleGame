import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

/**
 *class for growth of trees 
 * 
 */
class Growth implements Runnable{
	
	short[][] distance_to_sea;
	ArrayList<short[]> growpositions;
	ArrayList<short[]> diepositions;
	HashMap<Integer, Double> cachedprobabilities;
	
	public Growth(){
		growpositions=new ArrayList<short[]>();
		diepositions=new ArrayList<short[]>();
		cachedprobabilities=new HashMap<Integer, Double>();
	}
	
	public void setDistance(short[][] distance_to_sea){
		this.distance_to_sea=distance_to_sea;
		for(int i=0; i<distance_to_sea.length; i++)for(int j=0; j<distance_to_sea[0].length; j++){
			cachedprobabilities.put(((int)distance_to_sea[i][j]), Math.min(0.45, 1-Math.exp(-0.042*distance_to_sea[i][j])));
		}
	}
	
	public void run(){
		
		for(short i=0; i<distance_to_sea.length; i++)for(short j=0; j<distance_to_sea[i].length; j++){	
			if(distance_to_sea[i][j]>0){
					
				if((new Random()).nextInt(50000)==0){
					diepositions.add(new short[]{i,j});
				}
				
				if( (new Random()).nextInt(25000)==0 && (new Random()).nextDouble()<treeChance(i,j) ){
					growpositions.add(new short[]{i,j});
				}
					
			}
		}
	}
	
	public double treeChance(int x, int y){
		return cachedprobabilities.get((int)distance_to_sea[x][y]);
	}
	
	public ArrayList<short[]> getGrowPos(){
		ArrayList<short[]> tmp=growpositions;
		growpositions=new ArrayList<short[]>();
		return tmp;
	}
	
	public ArrayList<short[]> getDiePos(){
		ArrayList<short[]> tmp=diepositions;
		diepositions=new ArrayList<short[]>();
		return tmp;
	}	
}
