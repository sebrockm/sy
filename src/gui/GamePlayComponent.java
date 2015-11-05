package gui;

import game.Player;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import map.data.GraphData;

public class GamePlayComponent extends JComponent {
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
	
	private Shape highlightedArea = null;

	private Player[] players = null;
	
	public GamePlayComponent() throws IOException {
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
				setHighlighted(e);
				if(SwingUtilities.isLeftMouseButton(e)) {
					translate(e.getX() - lastX, e.getY() - lastY);
					lastX = e.getX();
					lastY = e.getY();
					repaint();
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
				
				setHighlighted(e);
				repaint();
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if(setHighlighted(e))
					repaint();
			}
			
			private boolean setHighlighted(MouseEvent e) {
				boolean changed = false;
				int i;
				for(i = 1; i <= GraphData.STATION_COUNT; ++i) {
		        	Shape area = graphData.getArea(i);
		        	if(area == null)
		        		continue;
		        	
		        	area = fromImageToOuterTransform().createTransformedShape(area);
		        	
		        	if(area.contains(e.getX(), e.getY())) {
		        		if(area != highlightedArea) {
		        			highlightedArea = area;
		        			changed = true;
		        		}
		        		break;
		        	}
				}
				
				if(i > GraphData.STATION_COUNT && highlightedArea != null) {
					highlightedArea = null;
					changed = true;
				}
				
				return changed;
			}
		};
		addMouseListener(mouseInputAdapter);
		addMouseMotionListener(mouseInputAdapter);
		addMouseWheelListener(mouseInputAdapter);
	}
	
	@Override
	protected void paintComponent(Graphics g) {		
		super.paintComponent(g);
		if(!(g instanceof Graphics2D)) 
			return;
		
        Graphics2D g2 = (Graphics2D)g;
        
        AffineTransform at = fromImageToOuterTransform();     
        g2.drawRenderedImage(mapImage, at);
           
        drawPlayerTokens(g2);
        
        if(highlightedArea != null) {
	        drawHighlightedArea(highlightedArea, g2);
        }
	}
	
	private void drawPlayerTokens(Graphics2D g2) {
		if(players == null)
			return;
		
		for(Player player : players) {
			if(!player.isVisible())
				continue;
			
			Rectangle2D r = graphData.getArea(player.getCurrentStationId()).getBounds2D();
			double atY = r.getMinY() - TokenShape.getHeight() + r.getHeight();
	        AffineTransform at = AffineTransform.getTranslateInstance(r.getMinX(), atY);
	        
	        Shape transformedToken = at.createTransformedShape(TokenShape.getInstance());
			at = fromImageToOuterTransform();
	        transformedToken = at.createTransformedShape(transformedToken);
	
	        Rectangle2D bb = transformedToken.getBounds2D();
	        float cx = (float)bb.getCenterX();
	        
	        g2.setPaint(new GradientPaint(cx, (float)bb.getMaxY(), Color.BLACK, cx, (float)bb.getMinY(), player.getColor()));
	        g2.fill(transformedToken);
		}
	}
	
	private void drawHighlightedArea(Shape area, Graphics2D g2) {
		Color lastColor = g2.getColor();
        
		AffineTransform at = fromOuterToImageTransform();        
        Rectangle2D bounds = area.getBounds2D();
        
        for(int y = 0; y < bounds.getHeight(); ++y) {
    		for(int x = 0; x < bounds.getWidth(); ++x) {
    			Point2D p = new Point2D.Double(bounds.getMinX() + x, bounds.getMinY() + y);
    			if(!area.contains(p))
    				continue;
    			
    			Point2D imageP = at.transform(p, null);
    			Color pixelColor = new Color(mapImage.getRGB((int)imageP.getX(), (int)imageP.getY()), true);
    			pixelColor = pixelColor.brighter();
    			g2.setColor(pixelColor);
    			g2.draw(new Rectangle((int)p.getX(), (int)p.getY(), 1, 1));
    		}
    	}
        
        g2.setColor(lastColor);
	}
	
	public GraphData getGraphData() {
		return graphData;
	}
	
	public AffineTransform fromOuterToImageTransform() {
		AffineTransform at = AffineTransform.getScaleInstance(1/scale, 1/scale);
		at.translate(-topLeftCornerX, -topLeftCornerY);
		return at;
	}
	
	public AffineTransform fromImageToOuterTransform() {
		AffineTransform at = AffineTransform.getTranslateInstance(topLeftCornerX, topLeftCornerY);
        at.scale(scale, scale);
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
	
	public void setPlayers(Player[] players) {
		this.players = players;
	}
}
