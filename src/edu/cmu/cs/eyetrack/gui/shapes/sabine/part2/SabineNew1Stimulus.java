package edu.cmu.cs.eyetrack.gui.shapes.sabine.part2;

import java.awt.Color;
import java.awt.Polygon;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class SabineNew1Stimulus extends Stimulus {
	private Polygon polygon;
	
	public SabineNew1Stimulus(String name, Color color, int width, int height) {

		super(name, color, width, height, color);

		polygon = new Polygon();
		polygon.addPoint((int) (0.2*width), 0);
		polygon.addPoint(0, height);
		polygon.addPoint((int) (0.85*width), height);
		polygon.addPoint((int) (0.77*width), (int) (0.6*height));
		polygon.addPoint((int) (0.90*width), (int) (0.6*height));
		polygon.addPoint(width, (int) (0.1*height));
		polygon.addPoint((int) (0.67*width), (int) (0.1*height));
		polygon.addPoint((int) (0.65*width), 0);
		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new SabineNew1Stimulus(name, this.color, width, height);
		} else {
			return new SabineNew1Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
