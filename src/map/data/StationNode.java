package map.data;

import java.awt.Rectangle;
import java.io.Serializable;

public class StationNode implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final int number;
	private final Rectangle area;
	
	public StationNode(int number, Rectangle area) {
		this.number = number;
		this.area = area;
	}
	
	public int getNumber() {
		return number;
	}
	
	public Rectangle getArea() {
		return area;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof StationNode))
			return super.equals(other);
		
		return number == ((StationNode)other).number;
	}
}
