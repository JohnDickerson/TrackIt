package edu.cmu.cs.eyetrack.gui.shapes.sabine.part2;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class SabineNew8Stimulus extends Stimulus {

	private static double kappa = 0.5522847498;
	private int radius;  // Radius of the little bump circle

	public SabineNew8Stimulus(String name, Color color, int width, int height) {
		super(name, color, width, height, color);
		this.radius = (int) (Math.min(width, height)*0.25);
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath s = new GeneralPath();
		s.moveTo(x+width-radius, y+0);  // Start at the top of the little bump circle
		int curx=x+width-radius; int cury=y+radius;
		s.curveTo(curx+radius*kappa, cury-radius, curx+radius, cury-radius*kappa, curx+radius, cury);
		s.curveTo(curx+radius, cury+radius*kappa, curx+radius*kappa, cury+radius, curx, cury+radius);
		
		s.lineTo(x+width-radius, y+radius+radius);  // At the buttom of the little bump circle
		s.lineTo(x+width-radius, y+height);
		s.lineTo(x+(int)(0.2*width), y+(int)(0.85*height));
		s.lineTo(x, y+(int)(0.5*height));
		s.lineTo(x+(int)(0.2*width), y+(int)(0.15*height));
		
		s.closePath();
		shape = s;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new SabineNew8Stimulus(name, this.color, width, height);
		} else {
			return new SabineNew8Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
