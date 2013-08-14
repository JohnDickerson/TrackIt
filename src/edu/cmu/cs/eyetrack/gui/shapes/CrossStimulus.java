package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Polygon;

public class CrossStimulus extends Stimulus {
	
	private Polygon polygon;
	private double hArm = 1.0/3.0;
	private double vArm = 1.0/3.0;
	
	public CrossStimulus(String name, Color color, int width, int height) {

		super(name, color, width, height);
		
		polygon = new Polygon();
		polygon.addPoint(0, (int) (height*hArm));
		polygon.addPoint((int) (width*vArm), (int) (height*hArm));
		polygon.addPoint((int) (width*vArm), 0);
		polygon.addPoint((int) (width*(1.0-vArm)), 0);
		polygon.addPoint((int) (width*(1.0-vArm)), (int) (height*hArm));
		polygon.addPoint(width, (int) (height*hArm));
		polygon.addPoint(width, (int) (height*(1.0-hArm)));
		polygon.addPoint((int) (width*(1.0-vArm)), (int) (height*(1.0-hArm)));
		polygon.addPoint((int) (width*(1.0-vArm)), height);
		polygon.addPoint((int) (width*vArm), height);
		polygon.addPoint((int) (width*vArm), (int) (height*(1.0-hArm)));
		polygon.addPoint(0, (int) (height*(1.0-hArm)));		
		
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new CrossStimulus(name, this.color, width, height);
		} else {
			return new CrossStimulus(name, color, width, height);
		}
	}
	
	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
