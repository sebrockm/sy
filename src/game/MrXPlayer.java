package game;

public class MrXPlayer extends Player {
	private int numberOfBlackTickets;
	private int numberOfDoubleMoves;
	private int numberOfMovesDone;
	
	@Override
	protected void checkTicketType(int ticketType) {
		if(ticketType == BLACK_TICKET)
			return;
		
		super.checkTicketType(ticketType);
	}
	
	@Override
	protected void decrementTicketCounter(int ticketType) {
		if(ticketType == BLACK_TICKET) {
			if(--numberOfBlackTickets < 0)
				throwTooFewTickets();
		} else {
			super.decrementTicketCounter(ticketType);
		}
	}
	
	public MrXPlayer(int startStationId, String name, 
			int numberOfTaxiTickets, int numberOfBusTickets, int numberOfUndergroundTickets,
			int numberOfBlackTickets, int numberOfDoubleMoves) {
		
		super(startStationId, name, numberOfTaxiTickets, numberOfBusTickets, numberOfUndergroundTickets);
		this.numberOfBlackTickets = numberOfBlackTickets;
		this.numberOfDoubleMoves = numberOfDoubleMoves;
		numberOfMovesDone = 0;
	}
	
	@Override
	public void moveTo(int stationId, int ticketType) {
		super.moveTo(stationId, ticketType);
		++numberOfMovesDone;
	}
	
	public int getNumberOfBlackTickets() {
		return numberOfBlackTickets;
	}
	
	public int getNumberOfDoubleMoves() {
		return numberOfDoubleMoves;
	}
	
	public void doDoubleMove() {
		if(--numberOfDoubleMoves < 0) {
			throw new IllegalStateException("Mr. X has no double moves left.");
		}
	}
	
	@Override
	public boolean isVisible() {
		switch(numberOfMovesDone) {
		case 3: case 8: case 13: case 18: case 24: return true;
		default: return false;
		}
	}
}
