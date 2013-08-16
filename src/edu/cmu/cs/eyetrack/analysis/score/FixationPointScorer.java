package edu.cmu.cs.eyetrack.analysis.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.eyetrack.analysis.score.compare.PointComparator;
import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.test.SingleRunScore;

public class FixationPointScorer extends Scorer {

	public FixationPointScorer(PointComparator pFunc, DistanceFunction dist) {
		super(pFunc, dist);
	}

	public String getName() {
		return "Fixation Points-only Scorer";
	}

	/**
	 * For each fixation point in the Tobii gaze trajectory (typically 20-120, based on the length of the
	 * run and the accuracy of the eye tracking), calculate the minimum distance between that fixation point
	 * and the target during the same time period, then add it to the final score.  Normalize by the number
	 * of fixation points used.
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
		long wallclockOffset =  subject.getTimestampRecStart();
		
		Map<Integer, Double> fixationIdxScores = new HashMap<Integer, Double>();
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

			Integer fixationPtIdx = subjFrame.getFixationIndex();
			if(null != fixationPtIdx) {
				// If this gaze point is part of a fixation point, think about it
				Double bestScore = fixationIdxScores.get(fixationPtIdx);
				if(null == bestScore || frameScore < bestScore) {
					// We've seen this fixation pt before; is this time's score better than the last best score?
					// Or we've never seen this fixation pt before, so its current score is the best score
					fixationIdxScores.put(fixationPtIdx, frameScore);
				}
			}
			validFrameCount++;	
		}

		// Normalize based on number of fixation points
		if(fixationIdxScores.size() > 0) {
			
			double score = 0.0;
			for(Integer fixationIdx : fixationIdxScores.keySet()) {
				score += fixationIdxScores.get(fixationIdx);
			}
			score /= (double) fixationIdxScores.size();

			System.err.println("Score: " + score + ", Fixation Pts: " + fixationIdxScores.size());

			SingleRunScore record = new SingleRunScore();
			record.setFrameCountOnTarget(validFrameCount);
			record.setFrameCountOverall(totalFrameCount);
			record.setFixationPointCount(fixationIdxScores.size());
			record.setScore(score);

			return record;

		} else {

			System.err.println("NO FIXATION POINTS FOUND");
			return new SingleRunScore().fakeTheScore();
		}
	}

}
