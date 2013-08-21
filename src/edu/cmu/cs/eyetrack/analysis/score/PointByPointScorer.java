package edu.cmu.cs.eyetrack.analysis.score;

import java.util.List;

import edu.cmu.cs.eyetrack.analysis.score.compare.PointComparator;
import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Trial;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.test.SingleRunScore;

public class PointByPointScorer extends Scorer{

	public PointByPointScorer(PointComparator pFunc, DistanceFunction dist) {
		super(pFunc, dist);
	}
	
	public String getName() {
		return "Point-by-Point Distance Scorer";
	}
	
	/**
	 * Calculates the point-by-point scores (based on distances between the points, possibly lagged by some time) and returns
	 * a run score, for a gaze trajectory and a Track-It gold standard file
	 * @param actual
	 * @param subject
	 * @param recordList
	 * @return
	 */
	public SingleRunScore calcScore(Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject, List<Double> recordList) {

		if( subject.getLengthMS() < actual.getLengthMS()) {
			Util.dPrint("Couldn't parse Spacebar-delimited Tobii file correctly; skipping trial here.\nPlease ignore any child with this message!");
			SingleRunScore record = new SingleRunScore();
			record.setFrameCountOnTarget(0);
			record.setFrameCountOverall(-1);
			record.setScore(Double.NaN);
			return record;
		}


		long validFrameCount = 0;
		long totalFrameCount = 0;
		double totalScore = 0.0;

		long wallclockOffset =  subject.getTimestampRecStart();
		for(TobiiFrame subjFrame : subject.getFrames()) {

			totalFrameCount++;
			// If neither of the subject's eyes were found in this frame, skip it
			if(subjFrame.getValidityLeft() == 4 || subjFrame.getValidityRight() == 4) {
				if(recordList != null) {
					recordList.add(null);
				}
				continue;
			}

			long timestamp = subjFrame.getTimestamp();

			double frameScore = pFunc.score(dist, timestamp, wallclockOffset, actual, subject);

			// If the Tobii data goes beyond when the objects were on screen and moving, cut
			// off the scoring function
			if( timestamp - wallclockOffset > actual.getLengthMS()) {
				break;
			}

			validFrameCount++;
			totalScore += frameScore;

			if(recordList != null) {
				recordList.add(frameScore);
			}
		}

		// If normalizing only on valid frames . . .
		double score =  totalScore / validFrameCount;

		System.err.println("Score: " + score);
		// If normalizing on all frames . . .
		//double score = totalScore / subject.getFrames().size();

		SingleRunScore record = new SingleRunScore();
		record.setFrameCountOnTarget(validFrameCount);
		record.setFrameCountOverall(totalFrameCount);
		record.setScore(score);

		return record;
	}

	@Override
	public boolean calcFixationStats(SingleRunScore record, Trial actual, Trajectory<TobiiFrame> subject) {
		// TODO implement this?  Is it okay to just return nothing here? Probably
		throw new UnsupportedOperationException();
	}
}
