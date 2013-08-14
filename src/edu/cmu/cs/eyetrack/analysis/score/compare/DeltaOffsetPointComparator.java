package edu.cmu.cs.eyetrack.analysis.score.compare;

import java.util.Map;

import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;

public class DeltaOffsetPointComparator extends PointComparator {

	private long offset;
	
	public DeltaOffsetPointComparator(long offset) {
		this.offset = offset;
	}
	
	@Override
	public double score(DistanceFunction dist, long timestamp, long wallclockOffset, Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject) {
		
		long delta = offset;
		if( timestamp-wallclockOffset < offset ) {
			delta = 0;
		}

		Map.Entry<Long, TrackItFrame> closestActual = actual.getClosestEqual(timestamp - delta);
		double distance = dist.distance(closestActual.getValue(), subject.getClosestEqual(timestamp).getValue());
		
		return distance;
	}

	public double getOffset() { return offset; }
	public void setOffset(long offset) { this.offset = offset; }
}
