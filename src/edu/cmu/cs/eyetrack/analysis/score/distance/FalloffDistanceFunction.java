package edu.cmu.cs.eyetrack.analysis.score.distance;

import edu.cmu.cs.eyetrack.analysis.struct.Frame;

public class FalloffDistanceFunction extends DistanceFunction {

	private double exponent;
	
	public FalloffDistanceFunction(double exponent) {
		this.exponent = exponent;
	}
	
	
	@Override
	public Double distance(Frame a, Frame b) {
		return Math.pow( a.getPosition().distance(b.getPosition()), exponent );
	}


	public double getExponent() { return exponent; }
	public void setExponent(double exponent) { this.exponent = exponent; }
}
