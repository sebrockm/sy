package game;

import java.util.LinkedList;

public class GameStatus {
	private final LinkedList<Player> players = new LinkedList<Player>();
	private final int agentTaxiTickets;
	private final int mrXTaxiTickets;
	private final int agentBusTickets;
	private final int mrXBusTickets;
	private final int agentUndergroundTickets;
	private final int mrXUnderGroundTickets;
	private final int blackTickets;
	private final int doubleMoves;
	
	public GameStatus(int agentTaxiTickets, int mrXTaxiTickets,
			int agentBusTickets, int mrXBusTickets,
			int agentUndergroundTickets, int mrXUnderGroundTickets,
			int blackTickets, int doubleMoves)
	{
		this.agentTaxiTickets = agentTaxiTickets;
		this.mrXTaxiTickets = mrXTaxiTickets;
		this.agentBusTickets = agentBusTickets;
		this.mrXBusTickets = mrXBusTickets;
		this.agentUndergroundTickets = agentUndergroundTickets;
		this.mrXUnderGroundTickets = mrXUnderGroundTickets;
		this.blackTickets = blackTickets;
		this.doubleMoves = doubleMoves;
	}
	
	private void nextPlayer() {
		Player current = players.removeFirst();
		players.addLast(current);
	}
	
	private Player getCurrentPlayer() {
		return players.getFirst();
	}
	
	public void addAgent(int startStationId, String name) {
		players.addLast(new Player(startStationId, name, 
				agentTaxiTickets, agentBusTickets, agentUndergroundTickets));
	}
	
	public void addMrX(int startStationId, String name) {
		players.addFirst(new MrXPlayer(startStationId, name, 
				mrXTaxiTickets, mrXBusTickets, mrXUnderGroundTickets, blackTickets, doubleMoves));
	}
	
	public void moveOfCurrentPlayer(int stationId, int ticketType) {
		getCurrentPlayer().moveTo(stationId, ticketType);
		nextPlayer();
	}
	
	public void doubleMoveMrX(int firstStationId, int firstTicketType,
			int secondStationId, int secondTicketType) {
		if(!(getCurrentPlayer() instanceof MrXPlayer)) {
			throw new IllegalStateException("Only Mr. X can do double moves.");
		}
		
		MrXPlayer mrX = (MrXPlayer) getCurrentPlayer();
		mrX.doDoubleMove();
		mrX.moveTo(firstStationId, firstTicketType);
		mrX.moveTo(secondStationId, secondTicketType);
	}
	
	public boolean isDoubleMovePossible() {
		if(!(getCurrentPlayer() instanceof MrXPlayer))
			return false;
		
		MrXPlayer mrX = (MrXPlayer) getCurrentPlayer();
		return mrX.getNumberOfDoubleMoves() > 0;
	}
}
