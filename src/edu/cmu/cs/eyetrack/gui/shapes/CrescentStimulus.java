package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class CrescentStimulus extends Stimulus {
	
	public CrescentStimulus(String name, Color color, int width, int height) {	
		super(name, color, width, height);	
		makeShape(0,0);
	}

	private void makeShape(int x, int y) {
		
		double heightFrac = 0.7;
		double widthFrac = 0.7;
		
		// Major outer circle forms the full moon
		Area crescent = new Area(
				new Ellipse2D.Double(x, y, width, height));
		// Subtract minor inner circle to create crescent
		crescent.subtract(new Area(
				new Ellipse2D.Double(x-2, y + ((1.0 - heightFrac) * 0.5) * height, widthFrac * width, heightFrac * height)));
		
		shape = crescent;
	}
	
	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new CrescentStimulus(name, this.color, width, height);
		} else {
			return new CrescentStimulus(name, color, width, height);
		}
	}
	
	@Override
	public void move(int newX, int newY) {
		makeShape(newX - (int) (0.5 * width),
				newY - (int) (0.5 * height));
	}
}
