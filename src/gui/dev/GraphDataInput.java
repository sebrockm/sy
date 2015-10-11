package gui.dev;

import gui.ImageComponent;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import map.data.GraphData;
import map.data.StationLink;

public class GraphDataInput {
	private static int currentMouseX, currentMouseY;
	private static int sourceNodeId;
	
	private static ImageComponent mapImage;
	private static JMenuBar menuBar;
	
	private static JRadioButton addNodesButton;
	private static JRadioButton addTaxiLinksButton;
	private static JRadioButton addBusLinksButton; 
	private static JRadioButton addUndergroundLinksButton;
	private static JRadioButton addBoatLinksButton;
	
	private static Shape getNodeShape() {
		final double scale = 1 / 1.35; // this somehow fits
		final double radius = 10 * scale;
		final double width = 35 * scale;
		final double height = 40 * scale;
		final double corner = 5 * scale;
		
		RoundRectangle2D rect = new RoundRectangle2D.Double(0, radius, width, height - 2*radius, corner, corner);
		Ellipse2D circleTop = new Ellipse2D.Double(width/2 - radius, 0, 2*radius, 2*radius);
		Ellipse2D circleBottom = new Ellipse2D.Double(width/2 - radius, 2*radius, 2*radius, 2*radius);
		
		Area result = new Area(rect);
		result.add(new Area(circleTop));
		result.add(new Area(circleBottom));
		return result;
	}
	
	private static Shape getCurrentNodeShape() {
		Shape nodeShape = getNodeShape();
		Rectangle2D bounds = nodeShape.getBounds2D();
		double zoomFactor = mapImage.getCurrentZoomFactor(); 
		double offsetX = bounds.getWidth() / 2 * zoomFactor;
		double offsetY = bounds.getHeight() / 2 * zoomFactor - menuBar.getHeight();
		
		AffineTransform at = AffineTransform.getTranslateInstance(currentMouseX - offsetX, currentMouseY - offsetY);
		at.scale(zoomFactor, zoomFactor);
		
		return at.createTransformedShape(nodeShape);
	}
	
	private static AffineTransform getOuterToImageTransform() {
		AffineTransform af = mapImage.fromOuterToImageTransform();
		af.translate(0, -menuBar.getHeight());
		return af;
	}
	
	private static void placeNode(GraphData graphData, int nodeNumber) {
		graphData.createNode(nodeNumber, getOuterToImageTransform().createTransformedShape(getCurrentNodeShape()));
	}
	
	private static int getNodeAtMousePosition(GraphData graphData) {
		Point2D imagePoint = mapImage.fromOuterToImageTransform().transform(new Point2D.Double(currentMouseX, currentMouseY), null);
		return graphData.getNodeAtPosition(imagePoint.getX(), imagePoint.getY());
	}
	
	private static int getSelectedLinkType() {
		if(addTaxiLinksButton.isSelected())
			return StationLink.TAXI_LINK;
		if(addBusLinksButton.isSelected())
			return StationLink.BUS_LINK;
		if(addUndergroundLinksButton.isSelected())
			return StationLink.UNDERGROUND_LINK;
		if(addBoatLinksButton.isSelected())
			return StationLink.BOAT_LINK;
		return 0;
	}
	
	/**
	 * Use this class to create nodes or links on a map.
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {			
		menuBar = new JMenuBar();
		final JMenu optionsMenu = menuBar.add(new JMenu("Options"));
		final ButtonGroup buttonGroup = new ButtonGroup();
		addNodesButton = new JRadioButton("Add Nodes");
		buttonGroup.add(addNodesButton);
		optionsMenu.add(addNodesButton);
		addTaxiLinksButton = new JRadioButton("Add Taxi Links");
		buttonGroup.add(addTaxiLinksButton);
		optionsMenu.add(addTaxiLinksButton);
		addBusLinksButton = new JRadioButton("Add Bus Links");
		buttonGroup.add(addBusLinksButton);
		optionsMenu.add(addBusLinksButton);
		addUndergroundLinksButton = new JRadioButton("Add Underground Links");
		buttonGroup.add(addUndergroundLinksButton);
		optionsMenu.add(addUndergroundLinksButton);
		addBoatLinksButton = new JRadioButton("Add Boat Links");
		buttonGroup.add(addBoatLinksButton);
		optionsMenu.add(addBoatLinksButton);
		
		addNodesButton.setSelected(true);
		
		final JMenu actionsMenu = menuBar.add(new JMenu("Actions"));
		final JMenuItem deleteAllLinksItem = actionsMenu.add(new JMenuItem("Remove all links"));

		final JFrame frame = new JFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				
				if(!(g instanceof Graphics2D))
					return;
				
		        Graphics2D g2 = (Graphics2D)g;
				
				if(addNodesButton.isSelected()) {
					g2.draw(getCurrentNodeShape());
				}
			} 
		};
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);

		try {
			mapImage = new ImageComponent();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		frame.add(mapImage);
		
		final GraphData graphData = mapImage.getGraphData();
		
		frame.setVisible(true);
		
		sourceNodeId  = 0;
		
		deleteAllLinksItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int chosen = JOptionPane.showConfirmDialog(null, 
						"Really remove all links of the selected type?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
				if(chosen == JOptionPane.OK_OPTION)
					graphData.removeAllLinks(getSelectedLinkType());
			}
		});
		
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {	
			private int lastInput = 0;
			
			@Override
			public void mouseMoved(MouseEvent e) {
				currentMouseX = e.getX();
				currentMouseY = e.getY();
				if(addNodesButton.isSelected()) {
					frame.repaint(); // repaint the node shape under the cursor
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(addNodesButton.isSelected()) {
					if(SwingUtilities.isLeftMouseButton(e)) {
						String input = JOptionPane.showInputDialog("Number:", lastInput + 1);
						
						try {
							lastInput = Integer.parseInt(input);
						} catch (NumberFormatException ex) {
							System.err.println(input + " is not a valid number");
							return;
						}
						
						placeNode(graphData, lastInput);
					}
				} else {
					if(sourceNodeId < 1 || sourceNodeId > GraphData.STATION_COUNT) {
						sourceNodeId = getNodeAtMousePosition(graphData);
					} else {
						int targetNodeId = getNodeAtMousePosition(graphData);
						if(targetNodeId >= 1 && targetNodeId <= GraphData.STATION_COUNT && targetNodeId != sourceNodeId) {
							int chosen = JOptionPane.showConfirmDialog(null, 
									"Create a link between " + sourceNodeId + " and " + targetNodeId + "?", 
									"Confirm link creation", JOptionPane.OK_CANCEL_OPTION);
							if(chosen == JOptionPane.OK_OPTION)
								graphData.createLink(sourceNodeId, targetNodeId, getSelectedLinkType(), null);
						}
						sourceNodeId = 0;
					}
				}
			}
		};
		
		mapImage.addMouseListener(mouseInputAdapter);
		mapImage.addMouseMotionListener(mouseInputAdapter);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(screenSize.width / 2, screenSize.height / 2);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					graphData.store("res/sy_map.data");
					System.out.println("Missing stations: " + graphData.getUnsetStations());
					System.out.println("Number of taxi links: " + graphData.getNumberOfLinks(StationLink.TAXI_LINK));
					System.out.println("Number of bus links: " + graphData.getNumberOfLinks(StationLink.BUS_LINK));
					System.out.println("Number of underground links: " + graphData.getNumberOfLinks(StationLink.UNDERGROUND_LINK));
					System.out.println("Number of boat links: " + graphData.getNumberOfLinks(StationLink.BOAT_LINK));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
