package edu.cmu.cs.eyetrack.gui.shapes.sabine.part1;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine4Stimulus extends Stimulus {

	private static double sideCurveFrac = 0.6;
	private static double middlePointFrac = 0.4;
	public Sabine4Stimulus(String name, Color color, int width, int height) {
		super(name, color, width, height, color);
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath s = new GeneralPath();
		s.moveTo(x+width, y);
		s.lineTo(x+(int)(width * (1-sideCurveFrac)), y);
		s.curveTo(x, y, 
				x, y+(middlePointFrac*height),
				x+(width * (0.25*sideCurveFrac)), y+(middlePointFrac*height));
		s.curveTo(x, y+((1-middlePointFrac)*height), 
				x, y+height,
				x+(int)(width*(1-sideCurveFrac)), y+height);

		s.lineTo(x+width, y+height);
		s.lineTo(x+width,y);

		shape = s;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine4Stimulus(name, this.color, width, height);
		} else {
			return new Sabine4Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
