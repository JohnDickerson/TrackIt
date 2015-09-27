package edu.cmu.cs.eyetrack.gui.shapes.sabine.part2;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;

public class SabineNew7Stimulus extends Stimulus {

	private static double kappa = 0.5522847498;
	private int radius;  // Radius of the little bump circles

	public SabineNew7Stimulus(String name, Color color, int width, int height) {
		super(name, color, width, height, color);
		this.radius = (int) (Math.min(width, height)*0.25);
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {

		GeneralPath s = new GeneralPath();
		s.moveTo(x+radius, y+(int) (0.25*height));  // Start at the top of the left-hand bump
		s.lineTo(x+radius+(int)(0.1*width), y+(int) (0.1*height));
		s.lineTo(x+width-radius, y+0);
		s.lineTo(x+width-radius, y+radius);  // At the top of the right-hand bump
		
		int curx=x+width-radius; int cury=y+radius+radius;
		s.curveTo(curx+radius*kappa, cury-radius, curx+radius, cury-radius*kappa, curx+radius, cury);
		s.curveTo(curx+radius, cury+radius*kappa, curx+radius*kappa, cury+radius, curx, cury+radius);
		
		s.lineTo(x+width-radius, y+height);  // At the buttom of the right-hand bump
		s.lineTo(x+radius+(int)(0.1*width), y+(int) (0.9*height));
		s.lineTo(x+radius, y+height-radius);
		
		curx=x+radius; cury=y+height-radius-radius;
		s.curveTo(curx-radius*kappa, cury+radius, curx-radius, cury+radius*kappa, curx-radius, cury);
		s.curveTo(curx-radius, cury-radius*kappa, curx-radius*kappa, cury-radius, curx, cury-radius);
		
		s.closePath();
		shape = s;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new SabineNew7Stimulus(name, this.color, width, height);
		} else {
			return new SabineNew7Stimulus(name, color, width, height);
		}
	}

	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
