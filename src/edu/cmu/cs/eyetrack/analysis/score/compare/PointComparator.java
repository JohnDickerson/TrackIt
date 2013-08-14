package edu.cmu.cs.eyetrack.analysis.score.compare;

import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;

public abstract class PointComparator {

	public abstract double score(DistanceFunction dist, long timestamp, long wallclockOffset, Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject);
}
