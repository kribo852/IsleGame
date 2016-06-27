import java.lang.Math;


class Player{
double x=0, y=0;
final double speed_base=2.5;
double theta;//angle


public void resetDirectionPosition(double speed , double mapxsize, double mapysize){
	x+=speed*speed_base*Math.cos(theta)/mapxsize;
	y+=speed*speed_base*Math.sin(theta)/mapysize;
}

public void resetAngle(double theta){
	this.theta+=theta;
}

public double getX(){
	return x;
}

public double getY(){
	return y;
}

public double returnTheta(){
	return theta;
}

} 
