package map.data;

import java.awt.Rectangle;
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
	
	public Rectangle getArea(int nodeNumber) {
		checkNodeNumber(nodeNumber);
		return stations[nodeNumber - 1].getArea();
	}
	
	
	// ----------------------------------------------------------------------------------------------------------
	// this section is for creating graph data during development
	
	@SuppressWarnings("unchecked") // stupid java cannot create a generic type array...
	public GraphData() {
		stations = new StationNode[STATION_COUNT];
		adjacencyList = new HashSet[STATION_COUNT];
		
		for(int i = 0; i < adjacencyList.length; ++i)
			adjacencyList[i] = new HashSet<StationLink>();
	}
	
	public void createNode(int nodeNumber, Rectangle area) {
		checkNodeNumber(nodeNumber);
		
		if(stations[nodeNumber - 1] != null)
			throw new IllegalArgumentException("Station node " + nodeNumber + " has already been created.");
		
		stations[nodeNumber - 1] = new StationNode(nodeNumber, area);
	}
	
	public void createLink(int sourceNodeNumber, int targetNodeNumber, int linkType) {
		checkNodeNumber(sourceNodeNumber);
		checkNodeNumber(targetNodeNumber);
		
		StationNode sourceNode = stations[sourceNodeNumber - 1];
		StationNode targetNode = stations[targetNodeNumber - 1];
		
		if(sourceNode == null)
			throw new NullPointerException("Station node " + sourceNodeNumber + " has not been created yet.");
		
		if(targetNode == null)
			throw new NullPointerException("Station node " + targetNodeNumber + " has not been created yet.");
		
		StationLink link = new StationLink(sourceNode, targetNode, linkType);

		adjacencyList[sourceNodeNumber - 1].add(link);
		adjacencyList[targetNodeNumber - 1].add(link);
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
}
