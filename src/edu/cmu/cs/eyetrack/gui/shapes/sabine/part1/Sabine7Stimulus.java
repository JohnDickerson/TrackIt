package edu.cmu.cs.eyetrack.gui.shapes.sabine.part1;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class Sabine7Stimulus extends Stimulus {

	private static double kappa = 0.5522847498;
	private int radius;
	
	public Sabine7Stimulus(String name, Color color, int diameter) {
		super(name, color, diameter, diameter, color);
		this.radius = diameter/2;
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		// Recenter shape
		x += radius;
		y += radius;
		
		GeneralPath s = new GeneralPath();
		s.moveTo(x, y-radius); // move to A
		s.curveTo(x+radius*kappa, y-radius, x+radius, y-radius*kappa, x+radius, y); // curve to A', B', B
		s.curveTo(x+radius, y+radius*kappa, x+radius*kappa, y+radius, x, y+radius );
		s.curveTo(x-radius*kappa, y+radius, x-radius, y+radius*kappa, x-radius, y);
		//s.curveTo(x-radius, y-radius*kappa, x-radius*kappa, y-radius, x, y-radius );
		s.lineTo(x-radius, y-radius);
		s.closePath();
		shape = s;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new Sabine7Stimulus(name, this.color, radius*2);
		} else {
			return new Sabine7Stimulus(name, color, radius*2);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
