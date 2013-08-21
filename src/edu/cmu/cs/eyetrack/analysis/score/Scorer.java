package edu.cmu.cs.eyetrack.analysis.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.eyetrack.analysis.score.compare.PointComparator;
import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiData;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Experiment;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Trial;
import edu.cmu.cs.eyetrack.test.SingleRunScore;

public abstract class Scorer {

	protected PointComparator pFunc;
	protected DistanceFunction dist;

	public Scorer(PointComparator pFunc, DistanceFunction dist) {
		this.pFunc = pFunc;
		this.dist = dist;
	}

	public abstract SingleRunScore calcScore(Trajectory<TrackItFrame> actual, Trajectory<TobiiFrame> subject, List<Double> recordList);
	
	public abstract boolean calcFixationStats(SingleRunScore record, Trial actual, Trajectory<TobiiFrame> subject);
	
	
	
	public Map<Integer, SingleRunScore> scoreSubject(Experiment goldStandard, TobiiData subjectData, Map<Integer, List<Double>> recordMap, double lookThreshold) {

		Map<Integer, SingleRunScore> scoreMap = new HashMap<Integer, SingleRunScore>();
		for(Integer trialID : goldStandard.getTrials().keySet()) {

			Trial trial = goldStandard.getTrials().get(trialID);
			
			// Old version: exactly as many Tobii trials as there are Track-It trials
			//Trajectory<TobiiFrame> subjectTrajectory = subjectData.getTrajectories().get(trialID);

			// Newer version: must infer Tobii trials by Spacebar press + closest timestamp match

			Trajectory<TobiiFrame> subjectTrajectory = subjectData.getClosestTrajectoryTo(goldStandard.getTrials().get(trialID).getStartTime());

			if(null == subjectTrajectory) {
				// Some error occurred while parsing the file (we've had some students with blank Tobii files); skip this
				continue;
			}

			int targetIdx = trial.getTargetIdx();
			Trajectory<TrackItFrame> goldTrajectory = trial.getTrajectories().get(targetIdx);

			if(recordMap != null) {
				recordMap.put(trialID, new ArrayList<Double>());
			}

			SingleRunScore record = calcScore(goldTrajectory,
					subjectTrajectory, 
					recordMap == null ? null : recordMap.get(trialID));

			// If the subject's eyes weren't tracked by Tobii enough, then nil the score
			if( ((double)record.getFrameCountOnTarget() / (double)record.getFrameCountOverall() ) < lookThreshold) {
				record.setScore(Double.NaN);
			}

			// Some scoring function implement secondary features, like thinking about non-target objectsp; put that stuff here
			calcFixationStats(record, trial, subjectTrajectory);
			
			scoreMap.put(trialID, record);


		}
		return scoreMap;
	}



	// For use with old version of Track-It + Tobii that ran on movies, stopped in December 2011
	public Map<Integer, SingleRunScore> scoreSubjectDec2011(Experiment goldStandard, TobiiData subjectData, Map<Integer, List<Double>> recordMap) {

		Map<Integer, SingleRunScore> scoreMap = new HashMap<Integer, SingleRunScore>();
		for(Integer trialID : subjectData.getTrajectories().keySet()) {

			// Old version: exactly as many Tobii trials as there are Track-It trials
			Trajectory<TobiiFrame> subjectTrajectory = subjectData.getTrajectories().get(trialID);

			if( goldStandard.getTrials().containsKey(trialID)) {

				int targetIdx = goldStandard.getTrials().get(trialID).getTargetIdx();
				Trajectory<TrackItFrame> goldTrajectory = goldStandard.getTrials().get(trialID).getTrajectories().get(targetIdx);

				if(recordMap != null) {
					recordMap.put(trialID, new ArrayList<Double>());
				}

				SingleRunScore record = calcScore(goldTrajectory, 
						subjectTrajectory, 
						recordMap == null ? null : recordMap.get(trialID));

				scoreMap.put(trialID, record);

			} else {
				System.err.println("No matching trial found in gold standard for Trial ID: " + trialID);
			}
		}
		return scoreMap;
	}
	
	public abstract String getName();
}
