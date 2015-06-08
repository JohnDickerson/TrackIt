package edu.cmu.cs.eyetrack.gui.shapes.sabine;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine6Stimulus extends Stimulus {

	private static double sideCurveFrac = 0.6;

	public Sabine6Stimulus(String name, Color color, int width, int height) {
		super(name, color, width, height);
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath s = new GeneralPath();
		s.moveTo(x+width, y);
		s.lineTo(x+(int)(width * (1-sideCurveFrac)), y);
		s.curveTo(x, y, 
				x, y+(0.5*height),
				x+(width * (0.5*sideCurveFrac)), y+(0.5*height));
		s.curveTo(x, y+(0.5*height), 
				x, y+height,
				x+(int)(width*(1-sideCurveFrac)), y+height);

		s.lineTo(x+width, y+height);
		s.lineTo(x+width,y);

		shape = s;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine6Stimulus(name, this.color, width, height);
		} else {
			return new Sabine6Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
