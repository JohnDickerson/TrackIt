package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Polygon;

public class DiamondStimulus extends Stimulus {

	private Polygon polygon;
	
	public DiamondStimulus(String name, Color color, int width, int height) {

		super(name, color, width, height);
		
		polygon = new Polygon();
		polygon.addPoint(width/2,0);
		polygon.addPoint(width, height/2);
		polygon.addPoint(width/2,height);
		polygon.addPoint(0, height/2);
		
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new DiamondStimulus(name, this.color, width, height);
		} else {
			return new DiamondStimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
	
}
