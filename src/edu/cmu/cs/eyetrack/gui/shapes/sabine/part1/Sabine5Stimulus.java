package edu.cmu.cs.eyetrack.gui.shapes.sabine.part1;

import java.awt.Color;
import java.awt.Polygon;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine5Stimulus extends Stimulus {
	private Polygon polygon;
	private static double steepnessYFrac = 0.15;
	private static double steepnessXFrac = 0.6;
	
	public Sabine5Stimulus(String name, Color color, int width, int height) {

		super(name, color, width, height, color);

		polygon = new Polygon();
		polygon.addPoint(width, height);
		polygon.addPoint((int) ((1-steepnessXFrac)*width), (int) ((1-steepnessYFrac)*height));
		polygon.addPoint(0, (int)(height*0.5));
		polygon.addPoint((int) ((1-steepnessXFrac)*width), (int) (steepnessYFrac * height));
		polygon.addPoint(width, 0);

		shape = polygon;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine5Stimulus(name, this.color, width, height);
		} else {
			return new Sabine5Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		//polygon.translate(-oldX + newX - (int) (0.5 * width), -oldY + newY - (int) (0.5 * height));
		polygon.translate(-((int) polygon.getBounds().getX()) + newX - (int) (0.5 * width), 
				-((int) polygon.getBounds().getY()) + newY - (int) (0.5 * height));
	}
}
