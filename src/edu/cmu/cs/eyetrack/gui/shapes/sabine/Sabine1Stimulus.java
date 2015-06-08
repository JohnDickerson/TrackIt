package edu.cmu.cs.eyetrack.gui.shapes.sabine;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine1Stimulus extends Stimulus {

	private static double topCurveFrac = 0.6;
	private static double sideCurveFrac = topCurveFrac;

	public Sabine1Stimulus(String name, Color color, int width, int height) {
		super(name, color, width, height, color);
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath s = new GeneralPath();
		s.moveTo(x+width, y+height);
		s.lineTo(x+width, y + ((1-topCurveFrac) * height));
		s.curveTo(x+width, y, 
				x + ((1-sideCurveFrac) * width), y,
				x + ((1-sideCurveFrac) * width), y + ((1-topCurveFrac) * height));

		s.quadTo(x, y + ((1-topCurveFrac) * height),
				x, y + ((1 - topCurveFrac * 0.5) * height));
		s.lineTo(x, y+height);
		s.lineTo(x+width, y+height);

		shape = s;
	}

//	s.moveTo(x, y);
//	s.lineTo(x, y + (topCurveFrac * height));
//	s.curveTo(x, y + height, 
//			x + (sideCurveFrac * width), y + height,
//			x + (sideCurveFrac * width), y + (topCurveFrac * height));
//
//	s.quadTo(x + width, y + (topCurveFrac * height),
//			x + width, y + (topCurveFrac * 0.5 * height));
//	s.lineTo(x+width, y);
//	s.lineTo(x, y);	
	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine1Stimulus(name, this.color, width, height);
		} else {
			return new Sabine1Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
