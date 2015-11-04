package gui;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class TokenShape {
	private static final Shape tokenShape;
	
	private static final double scale = 1 / 1.35; // this somehow fits
	private static final double radius = 17.5 * scale;
	private static final double width = 2 * radius;
	private static final double height = 1.25 * width + radius;
	
	static {
		GeneralPath triangle = new GeneralPath();
		triangle.moveTo(0, height);
		triangle.lineTo(width, height);
		triangle.lineTo(width/2, radius);
		triangle.closePath();
		
		Ellipse2D circle = new Ellipse2D.Double(0, 0, 2*radius, 2*radius);
		
		Area result = new Area(triangle);
		result.add(new Area(circle));
		
		tokenShape = result;
	}
	
	public static Shape getInstance() {
		return tokenShape;
	}
	
	public static double getHeight() {
		return height;
	}
}
