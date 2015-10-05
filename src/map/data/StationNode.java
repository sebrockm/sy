package map.data;

import java.awt.Shape;
import java.io.Serializable;

public class StationNode implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final int number;
	private final Shape area;
	
	public StationNode(int number, Shape area) {
		this.number = number;
		this.area = area;
	}
	
	public int getNumber() {
		return number;
	}
	
	public Shape getArea() {
		return area;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof StationNode))
			return super.equals(other);
		
		return number == ((StationNode)other).number;
	}
}
