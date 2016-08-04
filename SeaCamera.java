

// a class for handeling a delayed view of the seafaring escapades 
class SeaCamera{

	static double camerax, cameray;
	
	public static void initializeCamera(double camerax, double cameray){
		SeaCamera.camerax=camerax;
		SeaCamera.cameray=cameray;
	}

	public static void update(double x, double y){
		camerax=0.9875*camerax+0.0125*x;
		cameray=0.9875*cameray+0.0125*y;
	}
	
	public static double getCameraX(){return camerax;}
	
	public static double getCameraY(){return cameray;}
}


