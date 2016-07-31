

// a class for handeling a delayed view of the seafaring escapades 
class SeaCamera{

	static double camerax, cameray;
	
	public static void initializeCamera(double camerax, double cameray){
		SeaCamera.camerax=camerax;
		SeaCamera.cameray=cameray;
	}

	public static void update(double x, double y){
		camerax=0.98*camerax+0.02*x;
		cameray=0.98*cameray+0.02*y;
	}
	
	public static double getCameraX(){return camerax;}
	
	public static double getCameraY(){return cameray;}
}


