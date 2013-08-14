package edu.cmu.cs.eyetrack.analysis.score.distance;

import edu.cmu.cs.eyetrack.analysis.struct.Frame;

public class EuclideanDistanceFunction extends DistanceFunction {

	public EuclideanDistanceFunction() { }
	
	@Override
	public Double distance(Frame a, Frame b) {
		return a.getPosition().distance(b.getPosition());
	}

}
