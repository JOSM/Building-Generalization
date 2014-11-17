package org.openstreetmap.josm.plugins.buildinggeneralization;
public class Point {
	
	double x;
	double y;
	
	public Point(){
		this.x = 0;
		this.y = 0;
	}
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	
//	public static Point project(Point x1, Point x2, Point toProject)
//	{
//	    double m = (x2.y - x1.y) / (x2.x - x1.x);
//	    double b = x1.y - (m * x1.y);
//	 
//	    double x = (m * toProject.y + toProject.x - m * b) / (m * m + 1);
//	    double y = (m * m * toProject.y + m * toProject.x + b) / (m * m + 1);
//	 
//	    return new Point(x,y);
//	}
	
	public static double distance(Point x1, Point x2){
		
		return Math.sqrt(Math.pow(x2.x - x1.x, 2) + Math.pow(x2.y - x1.y, 2)); // always positive
		
	}
	
	public static double distance(Point lineX1, Point lineX2, Point p){
		double A = p.x - lineX1.x;
		double B = p.y - lineX1.y;
		double C = lineX2.x - lineX1.x;
		double D = lineX2.y - lineX1.y;
		
		double dot = A * C + B * D;
		double len_sq = C*C + D*D;
		double param = dot/len_sq;
		double xx = lineX1.x + param * C;
		double yy = lineX1.y + param * D;
		double dx = p.x - xx;
		double dy = p.y - yy;
		return Math.sqrt(dx * dx + dy * dy);
		
	}
	
	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}
	
}
