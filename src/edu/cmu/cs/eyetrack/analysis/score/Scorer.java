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
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.test.SingleRunScore;

public class Scorer {
	
	private PointComparator pFunc;
	private DistanceFunction dist;
	
	public Scorer(PointComparator pFunc, DistanceFunction dist) {
		this.pFunc = pFunc;
		this.dist = dist;
	}

	//public Double calcScore(Trajectory<?> actual, Trajectory<?> subject) {
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
	
	public Map<Integer, SingleRunScore> scoreSubject(Experiment goldStandard, TobiiData subjectData, Map<Integer, List<Double>> recordMap, double lookThreshold) {

		Map<Integer, SingleRunScore> scoreMap = new HashMap<Integer, SingleRunScore>();
		for(Integer trialID : goldStandard.getTrials().keySet()) {

			// Old version: exactly as many Tobii trials as there are Track-It trials
			//Trajectory<TobiiFrame> subjectTrajectory = subjectData.getTrajectories().get(trialID);

			// Newer version: must infer Tobii trials by Spacebar press + closest timestamp match

			Trajectory<TobiiFrame> subjectTrajectory = subjectData.getClosestTrajectoryTo(goldStandard.getTrials().get(trialID).getStartTime());
			
			if(null == subjectTrajectory) {
				// Some error occurred while parsing the file (we've had some students with blank Tobii files); skip this
				continue;
			}
			
			int targetIdx = goldStandard.getTrials().get(trialID).getTargetIdx();
			Trajectory<TrackItFrame> goldTrajectory = goldStandard.getTrials().get(trialID).getTrajectories().get(targetIdx);
			
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
}
