package game.controll;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;

import map.data.GraphData;

import game.GameStatus;
import game.MrXPlayer;
import game.Player;
import game.PlayerMove;
import gui.GamePlayComponent;

public class LocalPlayerInputReceiver extends MouseInputAdapter implements PlayerInputReceiver {
	private PlayerMove move;
	private int nextStation = 0;
	private int nextTransportation = 0;
	private boolean nextIsDouble = false;
	
	private final Object monitor = new Object();
	
	private final Player player;
	private final GamePlayComponent gamePlayComponent;
	private final GameStatus gameStatus;
	
	public LocalPlayerInputReceiver(Player player, GamePlayComponent gamePlayComponent, GameStatus gameStatus) {
		this.player = player;
		this.gamePlayComponent = gamePlayComponent;
		this.gameStatus = gameStatus;
		
		gamePlayComponent.addMouseListener(this);
	}

	@Override
	public PlayerMove waitForMove() {
		synchronized (monitor) {
			try {
				while (this.move == null)
					monitor.wait();
				
				PlayerMove move = this.move;
				this.move = null;
				return move;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (!gamePlayComponent.isGameStarted() || gameStatus.getCurrentPlayer() != player)
			return;
		
		int stationId = lookupStationId(e.getX(), e.getY());
		if (stationId != 0) {
			nextStation = stationId;
			int transportation = askForTransportation(stationId);
			if (transportation > 0) {
				nextTransportation = transportation;
				finishMove();
			}
		}
	}
	
	private void finishMove() {
		synchronized (monitor) {
			move = new PlayerMove(nextStation, nextTransportation, nextIsDouble);
			monitor.notify();
			
			nextStation = 0;
			nextTransportation = 0;
			nextIsDouble = false;
		}
	}
	
	private int lookupStationId(int x, int y) {
		Point2D imageCoordinates = gamePlayComponent.fromOuterToImageTransform()
				.transform(new Point2D.Double(x, y), null);
		
		return gamePlayComponent.getGraphData().getNodeAtPosition(imageCoordinates.getX(), imageCoordinates.getY());
	}
	
	private int askForTransportation(int stationId) {
		if (stationId < 1 || stationId > GraphData.STATION_COUNT)
			throw new IllegalArgumentException(stationId + " is not a valid station id.");
		
		if (!gamePlayComponent.isGameStarted())
			return 0;
		
		if (!canCurrentPlayerMove()) {
			JOptionPane.showMessageDialog(gamePlayComponent, gameStatus.getCurrentPlayer().getName() + " cannot move.");
			return 0;
		}
		
		if (stationId == 0 || gameStatus.isPositionOccupiedbyAgent(stationId))
			return 0;
		
		Player currentPlayer = gameStatus.getCurrentPlayer();
		int currentStationId = currentPlayer.getCurrentStationId();
		
		LinkedList<Object> optionObjects = new LinkedList<Object>();
		int taxiOptionId = -1;
		int busOptionId = -1;
		int undergroundOptionId = -1;
		int blackOptionId = -1;
		
		GraphData graphData = gamePlayComponent.getGraphData();
		
		if (currentPlayer.getNumberOfTaxiTickets() > 0
				&& graphData.getAdjacentTaxiStations(currentStationId).contains(stationId)) {
			taxiOptionId = optionObjects.size();
			optionObjects.add("Taxi");
		}
		
		if (currentPlayer.getNumberOfBusTickets() > 0 
				&& graphData.getAdjacentBusStations(currentStationId).contains(stationId)) {
			busOptionId = optionObjects.size();
			optionObjects.add("Bus");
		}
		
		if (currentPlayer.getNumberOfUndergroundTickets() > 0
				&& graphData.getAdjacentUndergroundStations(currentStationId).contains(stationId)) {
			undergroundOptionId = optionObjects.size();
			optionObjects.add("Underground");
		}
		
		if (currentPlayer instanceof MrXPlayer) {
			MrXPlayer mrX = (MrXPlayer) currentPlayer;
			if (mrX.getNumberOfBlackTickets() > 0 && graphData.getAllAdjacentStations(currentStationId).contains(stationId)) {
				blackOptionId = optionObjects.size();
				optionObjects.add("Black Ticket");
			}
		}
		
		if (optionObjects.isEmpty()) {
			JOptionPane.showMessageDialog(gamePlayComponent, currentPlayer.getName() + " cannot reach station " + stationId + "!");
			return 0;
		}
		
		optionObjects.add("Cancel");
		
		int chosenOption = JOptionPane.showOptionDialog(gamePlayComponent,
				"Go to Station " + stationId + " by ...",
				"Chose transportation.",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				optionObjects.toArray(),
				optionObjects.getLast());
		
		if (chosenOption == optionObjects.size() - 1)
			return 0;
		
		int ticketType = -1;
		if (chosenOption == taxiOptionId)
			ticketType = Player.TAXI_TICKET;
		else if (chosenOption == busOptionId)
			ticketType = Player.BUS_TICKET;
		else if (chosenOption == undergroundOptionId)
			ticketType = Player.UNDERGROUND_TICKET;
		else if (chosenOption == blackOptionId)
			ticketType = Player.BLACK_TICKET;
		else
			throw new RuntimeException(chosenOption + " was not a valid option to chose.");
		
		gamePlayComponent.repaint();
		return ticketType;
	}
	
	private boolean canCurrentPlayerMove() {
		Player currentPlayer = gameStatus.getCurrentPlayer();
		int currentStationId = currentPlayer.getCurrentStationId();
		GraphData graphData = gamePlayComponent.getGraphData();
		
		LinkedList<Integer> neighbors;
		if (currentPlayer.getNumberOfTaxiTickets() > 0 
				&& !(neighbors = graphData.getAdjacentTaxiStations(currentStationId)).isEmpty()) {
			for (int neighbor : neighbors) {
				if (!gameStatus.isPositionOccupiedbyAgent(neighbor))
					return true;
			}
		}
		
		if (currentPlayer.getNumberOfBusTickets() > 0 
				&& !(neighbors = graphData.getAdjacentBusStations(currentStationId)).isEmpty()) {
			for (int neighbor : neighbors) {
				if (!gameStatus.isPositionOccupiedbyAgent(neighbor))
					return true;
			}
		}
		
		if (currentPlayer.getNumberOfUndergroundTickets() > 0 
				&& !(neighbors = graphData.getAdjacentUndergroundStations(currentStationId)).isEmpty()) {
			for (int neighbor : neighbors) {
				if (!gameStatus.isPositionOccupiedbyAgent(neighbor))
					return true;
			}
		}
		
		if (currentPlayer instanceof MrXPlayer) {
			MrXPlayer mrX = (MrXPlayer)currentPlayer;
			if (mrX.getNumberOfBlackTickets() > 0 
					&& !(neighbors = graphData.getAllAdjacentStations(currentStationId)).isEmpty()) {
				for (int neighbor : neighbors) {
					if (!gameStatus.isPositionOccupiedbyAgent(neighbor))
						return true;
				}
			}
		}
		
		return false;
	}
}
