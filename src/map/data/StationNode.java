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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof StationNode))
			return false;
		StationNode other = (StationNode) obj;
		if (number != other.number)
			return false;
		return true;
	}
}
