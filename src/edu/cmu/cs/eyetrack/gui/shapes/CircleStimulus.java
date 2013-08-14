package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class CircleStimulus extends Stimulus {
	
	private Ellipse2D.Float circle;
	
	public CircleStimulus(String name, Color color, int diameter) {
		
		super(name, color, diameter, diameter);
		
		circle = new Ellipse2D.Float(0, 0, width, height);
		shape = circle;
	}

	@Override
	public Stimulus factoryClone(Color color) {
		if(color==null) {
			return new CircleStimulus(name, this.color, height);
		} else {
			return new CircleStimulus(name, color, height);
		}
	}
	
	@Override
	public void move(int newX, int newY) {
		shape = circle = new Ellipse2D.Float(newX - (int) (0.5 * width)
				,newY - (int) (0.5 * height)
				,width
				,height);
	}
}
