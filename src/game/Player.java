package game;

import java.awt.Color;

public class Player {
	public static final int TAXI_TICKET = 1;
	public static final int BUS_TICKET = 2;
	public static final int UNDERGROUND_TICKET = 3;
	public static final int BLACK_TICKET = 4;
	
	private int currentStationId;
	private final String name;
	private final Color color;
	
	private int numberOfTaxiTickets;
	private int numberOfBusTickets;
	private int numberOfUndergroundTickets;
	
	protected void checkTicketType(int ticketType) {
		if(ticketType != TAXI_TICKET && ticketType != BUS_TICKET && ticketType != UNDERGROUND_TICKET)
			throw new IllegalArgumentException("Invalid ticket type: " + ticketType);
	}
	
	protected void throwTooFewTickets() {
		throw new IllegalStateException("Player did not have enough of these tickets.");
	}
	
	protected void decrementTicketCounter(int ticketType) {		
		switch(ticketType) {
		case TAXI_TICKET: if(--numberOfTaxiTickets < 0) throwTooFewTickets(); break;
		case BUS_TICKET: if(--numberOfBusTickets < 0) throwTooFewTickets(); break;
		case UNDERGROUND_TICKET: if(--numberOfUndergroundTickets < 0) throwTooFewTickets(); break;
		default: throwTooFewTickets(); break;
		}
	}
	
	protected void incrementTicketCounter(int ticketType) {
		checkTicketType(ticketType);
		switch(ticketType) {
		case TAXI_TICKET: ++numberOfTaxiTickets; break;
		case BUS_TICKET: ++numberOfBusTickets; break;
		case UNDERGROUND_TICKET: ++numberOfUndergroundTickets; break;
		}
	}
	
	public Player(int startStationId, String name, Color color,
			int numberOfTaxiTickets, int numberOfBusTickets, int numberOfUndergroundTickets) {
		
		currentStationId = startStationId;
		this.name = name;
		this.color = color;
		this.numberOfTaxiTickets = numberOfTaxiTickets;
		this.numberOfBusTickets = numberOfBusTickets;
		this.numberOfUndergroundTickets = numberOfUndergroundTickets;
	}
	
	public int getCurrentStationId() {
		return currentStationId;
	}
	
	public void moveTo(int stationId, int ticketType) {
		checkTicketType(ticketType);
		currentStationId = stationId;
		decrementTicketCounter(ticketType);
	}
	
	public String getName() {
		return name;
	}
	
	public Color getColor() {
		return color;
	}
	
	public int getNumberOfTaxiTickets() {
		return numberOfTaxiTickets;
	}
	
	public int getNumberOfBusTickets() {
		return numberOfBusTickets;
	}
	
	public int getNumberOfUndergroundTickets() {
		return numberOfUndergroundTickets;
	}
	
	public boolean isVisible() {
		return true;
	}
}
