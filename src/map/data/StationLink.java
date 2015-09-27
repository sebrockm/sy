package map.data;

import java.io.Serializable;

public class StationLink implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int TAXI_LINK = 0x01;
	public static final int BUS_LINK = 0x02;
	public static final int UNDERGROUND_LINK = 0x04;
	public static final int BOAT_LINK = 0x08;
	
	private final StationNode source, target;
	
	private final int linkType;
	
	public StationLink(StationNode source, StationNode target, int linkType) {
		if(linkType != TAXI_LINK && linkType != BUS_LINK && linkType != UNDERGROUND_LINK && linkType != BOAT_LINK)
			throw new IllegalArgumentException(linkType + " is no proper link type.");
		
		this.source = source;
		this.target = target;
		this.linkType = linkType;
	}
	
	public StationNode getSourceStation() {
		return source;
	}
	
	public StationNode getTargetStation() {
		return target;
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
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof StationLink))
			return super.equals(other);
		
		StationLink otherLink = (StationLink)other;
		return linkType == otherLink.linkType && 
				(source == otherLink.source && target == otherLink.target ||
				source == otherLink.target && target == otherLink.source);
	}
}
