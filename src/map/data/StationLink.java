package map.data;

import java.awt.geom.Path2D;
import java.io.Serializable;

public class StationLink implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int TAXI_LINK = 0x01;
	public static final int BUS_LINK = 0x02;
	public static final int UNDERGROUND_LINK = 0x04;
	public static final int BOAT_LINK = 0x08;
	
	private final StationNode source, target;
	private final Path2D path;
	
	private final int linkType;
	
	public StationLink(StationNode source, StationNode target, int linkType, Path2D path) {
		if(linkType != TAXI_LINK && linkType != BUS_LINK && linkType != UNDERGROUND_LINK && linkType != BOAT_LINK)
			throw new IllegalArgumentException(linkType + " is no proper link type.");
		
		this.source = source;
		this.target = target;
		this.linkType = linkType;
		this.path = path;
	}
	
	public StationNode getSourceStation() {
		return source;
	}
	
	public StationNode getTargetStation() {
		return target;
	}
	
	public Path2D getPath() {
		return path;
	}
	
	public boolean isTaxiLink() {
		return linkType == TAXI_LINK;
	}
	
	public boolean isBusLink() {
		return linkType == BUS_LINK;
	}
	
	public boolean isUndergroundLink() {
		return linkType == UNDERGROUND_LINK;
	}
	
	public boolean isBoatLink() {
		return linkType == BOAT_LINK;
	}
	
	public int getLinkType() {
		return linkType;
	}
	
	public boolean equals1(Object other) {
		if(!(other instanceof StationLink))
			return super.equals(other);
		
		StationLink otherLink = (StationLink)other;
		return linkType == otherLink.linkType && 
				(source == otherLink.source && target == otherLink.target ||
				source == otherLink.target && target == otherLink.source);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + linkType;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof StationLink))
			return false;
		StationLink other = (StationLink) obj;
		if (linkType != other.linkType)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
}
