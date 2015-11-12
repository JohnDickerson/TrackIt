package edu.cmu.cs.eyetrack.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory.StimulusType;

/**
 * 	Order1	Order2
	trial1	1	2
	trial2	2	4
	trial3	4	1
	trial4	3	5
	trial5	5	3
	trial6	2	1
	trial7	4	5
	trial8	3	3
	trial9	5	4
	trial10	1	2
	trial11	3	5
	trial12	5	3
	trial13	1	4
	trial14	4	2
	trial15	2	1
	
	with targets
	1 --> SabineNewStimulus4
	2 --> SabineNewStimulus8
	3 --> SabineNewStimulus5
	4 --> SabineNewStimulus3
	5 --> SabineNewStimulus1
	
 */
public class ColoradoTypedTrial {

	public static enum TRIAL_TYPE { TYPE1, TYPE2 };
	
	private List<Stimulus> orderedTargetList;
	private TRIAL_TYPE myTrialType;
	
	public ColoradoTypedTrial(TRIAL_TYPE ttype) {
		myTrialType = ttype;
		init(ttype);
	}
	
	@SuppressWarnings("serial")
	private void init(TRIAL_TYPE ttype) {
		
		// Build the 15 trials of (Target, Distractor) pairs based on the type I or II selected
		final Map<String, Stimulus> targetStimMap = StimulusFactory.getInstance().getAllOfType(StimulusType.TARGET);
		switch(ttype) {
		case TYPE1:
			orderedTargetList = new ArrayList<Stimulus>() {{
				add(targetStimMap.get("Target 1"));
				add(targetStimMap.get("Target 2"));
				add(targetStimMap.get("Target 4"));
				add(targetStimMap.get("Target 3"));
				add(targetStimMap.get("Target 5"));
				add(targetStimMap.get("Target 2"));
				add(targetStimMap.get("Target 4"));
				add(targetStimMap.get("Target 3"));
				add(targetStimMap.get("Target 5"));
				add(targetStimMap.get("Target 1"));
				//add(targetStimMap.get("Target 3"));
				//add(targetStimMap.get("Target 5"));
				//add(targetStimMap.get("Target 1"));
				//add(targetStimMap.get("Target 4"));
				//add(targetStimMap.get("Target 2"));
			}};
			break;
		case TYPE2:
			orderedTargetList = new ArrayList<Stimulus>() {{
				add(targetStimMap.get("Target 2"));
				add(targetStimMap.get("Target 4"));
				add(targetStimMap.get("Target 1"));
				add(targetStimMap.get("Target 5"));
				add(targetStimMap.get("Target 3"));
				add(targetStimMap.get("Target 1"));
				add(targetStimMap.get("Target 5"));
				add(targetStimMap.get("Target 3"));
				add(targetStimMap.get("Target 4"));
				add(targetStimMap.get("Target 2"));
				//add(targetStimMap.get("Target 5"));
				//add(targetStimMap.get("Target 3"));
				//add(targetStimMap.get("Target 4"));
				//add(targetStimMap.get("Target 2"));
				//add(targetStimMap.get("Target 1"));
			}};
			break;
		}
	}
	
	public Stimulus getStimulus(int trialNumber) {
		// Trial count is 1-indexed in the main code
		int index = trialNumber - 1;
		if(index < 0 || index >= orderedTargetList.size()) {
			throw new IllegalArgumentException("Tried to index " + index + " into orderedTargetList of size " + orderedTargetList.size());
		}
		return orderedTargetList.get(index);
	}

	public TRIAL_TYPE getMyTrialType() {
		return myTrialType;
	}
	
	public int getNumTrials() {
		return orderedTargetList.size();
	}
}
