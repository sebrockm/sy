package game;

public class PlayerMove {
	
	private int destinationId;
	private int transportation;
	private boolean isDoubleMove;
	
	public PlayerMove(int destinationId, int transportation, boolean isDoubleMove) {
		this.destinationId = destinationId;
		this.transportation = transportation;
		this.isDoubleMove = isDoubleMove;
	}
	
	public int getDestinationId() {
		return destinationId;
	}
	
	public int getTransportation() {
		return transportation;
	}
	
	public boolean isDoubleMove() {
		return isDoubleMove;
	}
	
	public boolean isValid() {
		return destinationId != 0 && transportation != 0;
	}
}
