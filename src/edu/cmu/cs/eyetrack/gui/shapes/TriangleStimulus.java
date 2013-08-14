package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Polygon;

public class TriangleStimulus extends Stimulus {
	
	private Polygon polygon;
	
	public TriangleStimulus(String name, Color color, int height) {
		
		super(name, color, height, height);
		
		polygon = new Polygon();
		polygon.addPoint(height/2,0);
		polygon.addPoint(height,height);
		polygon.addPoint(0,height);
		
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new TriangleStimulus(name, this.color, height);
		} else {
			return new TriangleStimulus(name, color, height);
		}
	}
	
	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
