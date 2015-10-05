package gui.dev;

import gui.ImageComponent;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import map.data.GraphData;

public class GraphDataInput {
	private static int currentMouseX, currentMouseY;
	
	private static ImageComponent mapImage;
	private static JMenuBar menuBar;
	
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
	
	private static void placeNode(GraphData graphData, int nodeNumber) {
		AffineTransform af = mapImage.fromOuterToImageTransform();
		af.translate(0, -menuBar.getHeight());
		graphData.createNode(nodeNumber, af.createTransformedShape(getCurrentNodeShape()));
	}
	
	/**
	 * Use this class to create nodes or links on a map.
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {			
		menuBar = new JMenuBar();
		final JMenu menu = menuBar.add(new JMenu("Options"));
		final ButtonGroup buttonGroup = new ButtonGroup();
		final JRadioButton addNodesButton = new JRadioButton("Add Nodes");
		buttonGroup.add(addNodesButton);
		menu.add(addNodesButton);
		final JRadioButton addTaxiLinksButton = new JRadioButton("Add Taxi Links");
		buttonGroup.add(addTaxiLinksButton);
		menu.add(addTaxiLinksButton);
		final JRadioButton addBusLinksButton = new JRadioButton("Add Bus Links");
		buttonGroup.add(addBusLinksButton);
		menu.add(addBusLinksButton);
		final JRadioButton addUndergroundLinksButton = new JRadioButton("Add Underground Links");
		buttonGroup.add(addUndergroundLinksButton);
		menu.add(addUndergroundLinksButton);
		final JRadioButton addBoatLinksButton = new JRadioButton("Add Boat Links");
		buttonGroup.add(addBoatLinksButton);
		menu.add(addBoatLinksButton);
		
		addNodesButton.setSelected(true);

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
		
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {	
			private int lastInput = 0;
			
			@Override
			public void mouseMoved(MouseEvent e) {
				currentMouseX = e.getX();
				currentMouseY = e.getY();
				if(addNodesButton.isSelected()) {
					frame.repaint();
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					String input = JOptionPane.showInputDialog("Number:", lastInput + 1);
					
					try {
						lastInput = Integer.parseInt(input);
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
						return;
					}
					
					placeNode(graphData, lastInput);
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
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
