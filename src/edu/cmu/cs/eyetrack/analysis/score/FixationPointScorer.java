package edu.cmu.cs.eyetrack.analysis.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.eyetrack.analysis.score.compare.PointComparator;
import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Trial;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.test.SingleRunScore;

public class FixationPointScorer extends Scorer {

	public FixationPointScorer(PointComparator pFunc, DistanceFunction dist) {
		super(pFunc, dist);
	}

	public String getName() {
		return "Fixation Points-only Scorer";
	}

	private boolean trajectoryLengthsOkay(Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject) {
		if( subject.getLengthMS() < actual.getLengthMS()) {
			Util.dPrint("Couldn't parse Spacebar-delimited Tobii file correctly; skipping trial here.\nPlease ignore any child with this message!");
			return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param actual
	 * @param subject
	 * @param record
	 * @param recordList
	 * @return Mapping of Fixation Points to their individual numeric scores
	 */
	private Map<Integer, Double> calcFixationPtScores(Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject, SingleRunScore record, List<Double> recordList) {
		
		long wallclockOffset =  subject.getTimestampRecStart();
		long validFrameCount = 0;
		long totalFrameCount = 0;
		
		Map<Integer, Double> fixationIdxScores = new HashMap<Integer, Double>();
		for(TobiiFrame subjFrame : subject.getFrames()) {

			totalFrameCount++;
			// If either of the subject's eyes weren't found in this frame, skip it
			if(!subjFrame.bothEyesTracked()) {
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

		record.setFrameCountOnTarget(validFrameCount);
		record.setFrameCountOverall(totalFrameCount);
		return fixationIdxScores;
	}
	
	private Map<Integer, Double> calcFixationPtScores(Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject, SingleRunScore record) {
		return calcFixationPtScores(actual, subject, record, null);
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
	@Override
	public SingleRunScore calcScore(Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject, List<Double> recordList) {

		if(!trajectoryLengthsOkay(actual, subject)) {
			SingleRunScore record = new SingleRunScore();
			record.setFrameCountOnTarget(0);
			record.setFrameCountOverall(-1);
			record.setScore(Double.NaN);
			return record;
		}

		// Calculate a mapping of {Fixation Point Index -> that Fixation Point's score}
		SingleRunScore record = new SingleRunScore();
		Map<Integer, Double> fixationIdxScores = calcFixationPtScores(actual, subject, record, recordList);

		// Normalize based on number of fixation points
		if(fixationIdxScores.size() > 0) {
			
			double score = 0.0;
			for(Integer fixationIdx : fixationIdxScores.keySet()) {
				score += fixationIdxScores.get(fixationIdx);
			}
			score /= (double) fixationIdxScores.size();

			System.err.println("Score: " + score + ", Fixation Pts: " + fixationIdxScores.size());

			record.setFixationPointCount(fixationIdxScores.size());
			record.setScore(score);

			return record;

		} else {

			System.err.println("NO FIXATION POINTS FOUND");
			return new SingleRunScore().fakeTheScore();
		}
	}
	
	
	
	/**
	 * Given the target T, for each fixation point F, if [F is not near T], 
	 * then for each distractor D, if [F is near D], we consider this fixation
	 * point to be on a distractor and not on the target.
	 * 
	 * @param record A base SingleRunScore that has already been filled out by calcScore
	 * @param actual The gold standard data from Track-It (actual positions of objects)
	 * @param subject The subject's Tobii data
	 * @returns true if this method is implemented meaningfully
	 */
	@Override
	public boolean calcFixationStats(SingleRunScore record, Trial actualFull, Trajectory<TobiiFrame> subject) {
		
		long wallclockOffset = subject.getTimestampRecStart();
		
		// Grab the target object's trajectory; we'll ignore fixation points that are close to this
		int targetIdx = actualFull.getTargetIdx();
		Trajectory<TrackItFrame> targetTrajectory = actualFull.getTrajectories().get(targetIdx);
		if(!trajectoryLengthsOkay(targetTrajectory, subject)) { return true; }

		// Calculate the scores for each fixation point relative to the target.  We won't count
		// those fixation points that have a "good enough" score, because they are tracking the target
		Map<Integer, Double> fixationIdxTargetScores = calcFixationPtScores(targetTrajectory, subject, record);

		// Calculate the best scores for each fixation point relative to any distractor
		Map<Integer, Double> fixationIdxDistractorScores = new HashMap<Integer, Double>();
		for(int trajIdx=0; trajIdx < actualFull.getTrajectories().size(); trajIdx++) {
			
			// Only care about distractors now, not the target
			if(trajIdx == targetIdx) { continue; }
			
			// Get the mapping of fixation points to scores for this specific distractor
			Trajectory<TrackItFrame> distractorTrajectory = actualFull.getTrajectories().get(trajIdx);		
			Map<Integer, Double> specificFixationScores = calcFixationPtScores(distractorTrajectory, subject, record);

			// If this distractor beats the other distractors for any fixation point, update
			for(Integer fixationIdx : specificFixationScores.keySet()) {
				
				Double currentBestScore = fixationIdxDistractorScores.get(fixationIdx);
				Double newScore = specificFixationScores.get(fixationIdx);
				if(null == currentBestScore || newScore.compareTo(currentBestScore) < 0) {
					fixationIdxDistractorScores.put(fixationIdx, newScore);
				}
			}
			
		} // End of trajectory (distractors) loop
		
		
		// Now, for each fixation point, if its best distractor score is much-much better than its best target score,
		// count it as a distractor-on-target fixation point
		int fixationNearDistractorNotTargetCt = 0;
		for(Integer fixationPtIDx : fixationIdxTargetScores.keySet()) {
			
			double tScore = fixationIdxTargetScores.get(fixationPtIDx);
			double dScore = fixationIdxDistractorScores.get(fixationPtIDx);
			
			// TODO parameterize these thresholds
			double closenessThreshold = 150;    // For Euclidean Distance metric, this is #pixels; for current Track-It tests, object width=120px
			// High score w.r.t. Target, low score w.r.t. Distractor 
			if(tScore > closenessThreshold && dScore <= closenessThreshold) {
				fixationNearDistractorNotTargetCt++;
			}	
		}
		
		
		
		record.setFixationNearDistractorNotTargetCount(fixationNearDistractorNotTargetCt);
		record.setFixationPointCount(fixationIdxTargetScores.size());
		
		return true;
	}

}
