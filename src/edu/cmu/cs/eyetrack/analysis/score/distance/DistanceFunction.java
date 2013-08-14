package edu.cmu.cs.eyetrack.analysis.score.distance;

import edu.cmu.cs.eyetrack.analysis.struct.Frame;

public abstract class DistanceFunction {

	public abstract Double distance(Frame a, Frame b);
}
