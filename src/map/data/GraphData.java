package map.data;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;


public class GraphData implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int STATION_COUNT = 199;

	private final StationNode[] stations;
	private final HashSet<StationLink>[] adjacencyList;
	
	private void checkNodeNumber(int nodeNumber) {
		if(nodeNumber < 1 || nodeNumber > STATION_COUNT)
			throw new IllegalArgumentException(nodeNumber + " is an illegal number of a station.");	
	}
	
	public static GraphData loadFromFile(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			File file = new File(fileName);
			if(!file.exists() || !file.isFile())
				return new GraphData();
			ois = new ObjectInputStream(new FileInputStream(file));		
			return (GraphData)ois.readObject();
		} catch (FileNotFoundException e) {
			return new GraphData();
		} catch (Exception e) {
			throw e;
		} finally {
			if(ois != null)
				ois.close();
		}
	}
	
	public Shape getArea(int nodeNumber) {
		checkNodeNumber(nodeNumber);
		if (stations[nodeNumber - 1] == null)
			return null;
		return stations[nodeNumber - 1].getArea();
	}
	
	private LinkedList<Shape> getAdjacentAreas(int nodeNumber, int linkType) {
		checkNodeNumber(nodeNumber);
		
		HashSet<StationLink> stationLinks = adjacencyList[nodeNumber - 1];
		
		LinkedList<Shape> adjacentNodes = new LinkedList<Shape>();
		for (StationLink link : stationLinks) {
			if ((link.getLinkType() & linkType) != 0) {
				if (link.getSourceStation().getNumber() == nodeNumber)
					adjacentNodes.add(link.getTargetStation().getArea());
				else
					adjacentNodes.add(link.getSourceStation().getArea());
			}
		}
		return adjacentNodes;
	}
	
	public LinkedList<Shape> getAdjacentTaxiAreas(int nodeNumber) {
		return getAdjacentAreas(nodeNumber, StationLink.TAXI_LINK);
	}
	
	public LinkedList<Shape> getAdjacentBusAreas(int nodeNumber) {
		return getAdjacentAreas(nodeNumber, StationLink.BUS_LINK);
	}
	
	public LinkedList<Shape> getAdjacentUndergroundAreas(int nodeNumber) {
		return getAdjacentAreas(nodeNumber, StationLink.UNDERGROUND_LINK);
	}
	
	public LinkedList<Shape> getAllAdjacentAreas(int nodeNumber) {
		return getAdjacentAreas(nodeNumber, 
				StationLink.TAXI_LINK | StationLink.BUS_LINK | StationLink.UNDERGROUND_LINK | StationLink.BOAT_LINK);
	}
	
	// ----------------------------------------------------------------------------------------------------------
	// this section is for creating graph data during development
	
	public int getNodeAtPosition(double x, double y) {
		for(int i = 1; i <= stations.length; ++i) {
			Shape area = getArea(i);
			if(area == null)
				continue;
			
			if(area.contains(x, y))
				return i;
		}
		
		return 0;
	}
	
	@SuppressWarnings("unchecked") // stupid java cannot create a generic type array...
	public GraphData() {
		stations = new StationNode[STATION_COUNT];
		adjacencyList = new HashSet[STATION_COUNT];
		
		for(int i = 0; i < adjacencyList.length; ++i)
			adjacencyList[i] = new HashSet<StationLink>();
	}
	
	public void createNode(int nodeNumber, Shape area) {
		checkNodeNumber(nodeNumber);
		
		if(stations[nodeNumber - 1] != null)
			throw new IllegalArgumentException("Station node " + nodeNumber + " has already been created.");
		
		stations[nodeNumber - 1] = new StationNode(nodeNumber, area);
	}
	
	public void createLink(int nodeNumber1, int nodeNumber2, int linkType, Path2D path) {
		checkNodeNumber(nodeNumber1);
		checkNodeNumber(nodeNumber2);
		
		StationNode node1 = stations[nodeNumber1 - 1];
		StationNode node2 = stations[nodeNumber2 - 1];
		
		if(node1 == null)
			throw new NullPointerException("Station node " + nodeNumber1 + " has not been created yet.");
		
		if(node2 == null)
			throw new NullPointerException("Station node " + nodeNumber2 + " has not been created yet.");
		
		StationLink link1 = new StationLink(node1, node2, linkType, path);
		StationLink link2 = new StationLink(node2, node1, linkType, path);

		boolean added1 = adjacencyList[nodeNumber1 - 1].add(link1);
		boolean added2 = adjacencyList[nodeNumber2 - 1].add(link2);
		
		if(!added1 && !added2) {
			System.err.println("Link already exists!");
		} else if(added1 ^ added2) {
			throw new RuntimeException("Link consistency error!");
		}
	}
	
	public void removeAllLinks(int linkType) {		
		for(int i = 0; i < adjacencyList.length; ++i) {
			LinkedList<StationLink> toRemove = new LinkedList<StationLink>();
			for(StationLink l : adjacencyList[i]) {
				if(l.getLinkType() == linkType) {
					toRemove.add(l);
				}
			}
			adjacencyList[i].removeAll(toRemove);
		}
	}
	
	public void store(String fileName) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
		oos.writeObject(this);
		oos.close();
	}
	
	public ArrayList<Integer> getUnsetStations() {
		ArrayList<Integer> result = new ArrayList<Integer>(STATION_COUNT);
		
		for(int i = 0; i < stations.length; ++i)
			if(stations[i] == null)
				result.add(i + 1);
		
		return result;
	}
	
	public int getNumberOfLinks(int linkType) {
		int counter = 0;
		for(int i = 0; i < adjacencyList.length; ++i) {
			for(StationLink l : adjacencyList[i]) {
				if(l.getLinkType() == linkType) {
					++counter;
				}
			}
		}
		return counter / 2;
	}
}
