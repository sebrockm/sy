package game;

import java.awt.Color;
import java.util.LinkedList;

public class GameStatus {
	private final LinkedList<Player> players = new LinkedList<>();
	private MrXPlayer mrX = null;
	private int currentPlayerId = 0;
	private final int agentTaxiTickets;
	private final int mrXTaxiTickets;
	private final int agentBusTickets;
	private final int mrXBusTickets;
	private final int agentUndergroundTickets;
	private final int mrXUnderGroundTickets;
	private final int blackTickets;
	private final int doubleMoves;
	private boolean isEnd = false;
	
	public interface GameEndCallback {
		public void mrXWins();
		public void agentsWin();
	}
	
	private GameEndCallback gameEndCallback = null;
	
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
		currentPlayerId = (currentPlayerId + 1) % players.size();
	}
	
	public void setGameEndCallback(GameEndCallback gameEndCallback) {
		this.gameEndCallback = gameEndCallback;
	}
	
	public boolean isGameEnd() {
		return isEnd;
	}
	
	public Player getCurrentPlayer() {
		return players.get(currentPlayerId);
	}
	
	public void addAgent(int startStationId, String name, Color color) {
		players.addLast(new Player(startStationId, name, color,
				agentTaxiTickets, agentBusTickets, agentUndergroundTickets));
	}
	
	public void addMrX(int startStationId, String name) {
		if (mrX != null)
			throw new IllegalStateException("You cannot have two Mr. X.");
		
		mrX = new MrXPlayer(startStationId, name, 
				mrXTaxiTickets, mrXBusTickets, mrXUnderGroundTickets, blackTickets, doubleMoves);
		players.addFirst(mrX);
	}
	
	public void moveCurrentPlayer(PlayerMove move) {
		if (move.isValid()) {
			getCurrentPlayer().moveTo(move.getDestinationId(), move.getTransportation());
			if (move.isDoubleMove()) {
				if(getCurrentPlayer() != mrX)
					throw new IllegalStateException("Only Mr. X can do double moves.");
				mrX.doDoubleMove();
			}
				
			if (getCurrentPlayer() != mrX) {
				mrX.incrementTicketCounter(move.getTransportation());
				if (mrX.getCurrentStationId() == getCurrentPlayer().getCurrentStationId()) {
					if (gameEndCallback != null) {
						gameEndCallback.agentsWin();
						isEnd = true;
					}
					return;
				}
			}
			nextPlayer();
		}
		else {
			if (getCurrentPlayer() == mrX) {
				if (gameEndCallback != null) {
					gameEndCallback.agentsWin();
					isEnd = true;
				}
			}
			else
				nextPlayer();
		}
	}
	
	public boolean isDoubleMovePossible() {
		if(getCurrentPlayer() != mrX)
			return false;
		
		return mrX.getNumberOfDoubleMoves() > 0;
	}
	
	public LinkedList<Player> getPlayers() {
		return players;
	}
	
	public boolean isPositionOccupiedbyAgent(int stationId) {
		for (Player player : players) {
			if (player != mrX && player.getCurrentStationId() == stationId)
				return true;
		}
		return false;
	}
}
