package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Polygon;

public class BoxStimulus extends Stimulus {

	private Polygon polygon;
	
	public BoxStimulus(String name, Color color, int width, int height) {

		super(name, color, width, height);
		
		polygon = new Polygon();
		polygon.addPoint(0,0);
		polygon.addPoint(width,0);
		polygon.addPoint(width,width);
		polygon.addPoint(0,width);
		
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new BoxStimulus(name, this.color, width, height);
		} else {
			return new BoxStimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	
	}
	
}
