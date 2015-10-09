package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import map.data.GraphData;

public class ImageComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private static final String mapPath = "res/sy_map.jpg";
	private static final String dataPath = "res/sy_map.data";
	private static final double SCALE_FACTOR = 1.1;
	private static final double MAX_SCALE = 5;
	private static final double MIN_SCALE = 0.3;
	
	private final BufferedImage mapImage;
	private GraphData graphData;
	private double scale;
	private double topLeftCornerX;
	private double topLeftCornerY;

	public ImageComponent() throws IOException {
		super();
		
		setDefaultScale();
		topLeftCornerX = 0;
		topLeftCornerY = 0;
		
		try {
			graphData = GraphData.loadFromFile(dataPath);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		mapImage = ImageIO.read(new File(mapPath));
		setVisible(true);
		setDoubleBuffered(true);
		
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
			private int lastX;
			private int lastY;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					lastX = e.getX();
					lastY = e.getY();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					translate(e.getX() - lastX, e.getY() - lastY);
					repaint();
					lastX = e.getX();
					lastY = e.getY();
				}
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation() < 0)
					for(int i = 0; i < -e.getWheelRotation(); ++i)
						zoomIn(e.getX(), e.getY());
				else
					for(int i = 0; i < e.getWheelRotation(); ++i)
						zoomOut(e.getX(), e.getY());
				
				repaint();
			}
		};
		addMouseListener(mouseInputAdapter);
		addMouseMotionListener(mouseInputAdapter);
		addMouseWheelListener(mouseInputAdapter);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(g instanceof Graphics2D) {
	        Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	        
	        AffineTransform at = AffineTransform.getTranslateInstance(topLeftCornerX, topLeftCornerY);
	        at.scale(scale, scale);
	        
	        g2.drawRenderedImage(mapImage, at);
	        
	        for(int i = 1; i < GraphData.STATION_COUNT; ++i) {
	        	Shape area = graphData.getArea(i);
	        	if(area == null)
	        		continue;
	        	
	        	Color lastColor = g2.getColor();
	        	final Color transparentRed = new Color(1.f, 0, 0, 0.5f);
	        	g2.setColor(transparentRed);
	        	g2.draw(at.createTransformedShape(area));
	        	g2.setColor(lastColor);
	        }
		}
	}
	
	public GraphData getGraphData() {
		return graphData;
	}
	
	public AffineTransform fromOuterToImageTransform() {
		AffineTransform at = AffineTransform.getScaleInstance(1/scale, 1/scale);
		at.translate(-topLeftCornerX, -topLeftCornerY);
		return at;
	}
	
	public void zoomIn(double x, double y) {
		if(scale < MAX_SCALE) {
			topLeftCornerX = x + (topLeftCornerX - x) * SCALE_FACTOR;
			topLeftCornerY = y + (topLeftCornerY - y) * SCALE_FACTOR;
			scale *= SCALE_FACTOR;
		}
	}
	
	public void zoomOut(double x, double y) {
		if(scale > MIN_SCALE) {
			topLeftCornerX = x + (topLeftCornerX - x) / SCALE_FACTOR;
			topLeftCornerY = y + (topLeftCornerY - y) / SCALE_FACTOR;
			scale /= SCALE_FACTOR;
		}
	}
	
	public double getCurrentZoomFactor() {
		return scale;
	}
	
	public void setDefaultScale() {
		scale = 1;
	}
	
	public void translate(double dx, double dy) {
		topLeftCornerX += dx;
		topLeftCornerY += dy;
	}
}