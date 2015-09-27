package gui.dev;

import gui.ImageComponent;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import map.data.GraphData;

public class GraphDataInput {

	private static boolean rightHold = false;
	private static int startX, startY, endX, endY;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		final JFrame frame = new JFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				
				if(g instanceof Graphics2D && rightHold) {
			        Graphics2D g2 = (Graphics2D)g;
			        g2.drawRect(startX, startY, endX - startX, endY - startY);
				}
			} 
		};
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final ImageComponent mapImage = new ImageComponent();
		frame.add(mapImage);
		
		frame.setVisible(true);
		
		final GraphData graphData = GraphData.loadFromFile("res/sy_map.data");
		
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {	
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					rightHold = true;
					startX = endX = e.getX();
					startY = endY = e.getY();
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					endX = e.getX();
					endY = e.getY();
					frame.repaint();
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					do {
						String input = JOptionPane.showInputDialog("Number:");
						if(input == null)
							return;
						
						int number = 0;
						try {
							number = Integer.parseInt(input);
						} catch (NumberFormatException ex) { 
							ex.printStackTrace();
							continue; 
						}
						
						double x = mapImage.fromOuterToImageCoordinateX(startX);
						double y = mapImage.fromOuterToImageCoordinateY(startY);
						double width = mapImage.fromOuterToImageCoordinateX(endX) - x;
						double height = mapImage.fromOuterToImageCoordinateX(endY) - y;
						
						try {
							graphData.createNode(number, new Rectangle((int)x, (int)y, (int)width, (int)height));
						} catch (IllegalArgumentException ex) {
							ex.printStackTrace();
							continue; 
						}
						
						System.out.println("Created node " + number + " at " + graphData.getArea(number).toString());
						rightHold = false;
					} while(rightHold);
					frame.repaint();
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
