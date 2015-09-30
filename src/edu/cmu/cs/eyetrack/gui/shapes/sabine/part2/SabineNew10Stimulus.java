package edu.cmu.cs.eyetrack.gui.shapes.sabine.part2;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class SabineNew10Stimulus extends Stimulus {

	private static double kappa = 0.5522847498;
	private int radius;  // Radius of the little bump circle

	public SabineNew10Stimulus(String name, Color color, int width, int height) {
		super(name, color, width, height, color);
		this.radius = (int) (Math.min(width, height)*0.40);
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath s = new GeneralPath();
		s.moveTo(x,y);
		s.lineTo(x+width-radius, y);
		s.lineTo(x+width-radius, y+height-radius-radius);
		
		int curx=x+width-radius; int cury=y+height-radius;  // Now at the top of the right-hand bump circle
		s.curveTo(curx+radius*kappa, cury-radius, curx+radius, cury-radius*kappa, curx+radius, cury);
		s.curveTo(curx+radius, cury+radius*kappa, curx+radius*kappa, cury+radius, curx, cury+radius);
		s.curveTo(curx-radius*kappa, cury+radius, curx-radius, cury+radius*kappa, curx-radius, cury);
		
		s.lineTo(x, y+height-radius);  // At the left of the circle
		s.closePath();
		shape = s;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new SabineNew10Stimulus(name, this.color, width, height);
		} else {
			return new SabineNew10Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
