package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StimulusFactory {

	public static enum StimulusType { BOTH, TARGET, DISTRACTOR };
	
	// This should be the same global random that we use everywhere
	private Random random;
	
	// Keeps track of which Stimulus are which type (distractor, target, ..)
	private HashMap<StimulusType, List<Stimulus>> stimuli;
	
	private static StimulusFactory factory = null;
	public static StimulusFactory getInstance() {
		if(factory == null) {
			factory = new StimulusFactory();
		}
		return factory;
	}
	
	private StimulusFactory() {
		reset();
	}
	
	public void reset() {
		this.random = new Random();
		this.stimuli = new HashMap<StimulusType, List<Stimulus>>();
		
		// If we've never seen this type of stimulus before, initialize
		for(StimulusType type : StimulusType.values()) {
			this.stimuli.put(type, new ArrayList<Stimulus>());
		}
	}
	
	public void setRandom(Random random) {
		this.random = random;
	}
	
	public String registerStimulus(Stimulus stimulus, StimulusType type) {
		
		// BOTH keeps track of every stimulus we have ever seen
		if(type == StimulusType.BOTH) {
			for(StimulusType t : StimulusType.values()) {
				stimuli.get(t).add(stimulus);
			}
		} else {
			stimuli.get(type).add(stimulus);
		}
		
		return stimulus.getName();
	}
	
	public Stimulus create(StimulusType type, Color color) {
		List<Stimulus> typeList = stimuli.get(type);
		if(typeList == null) return null;
		
		// Pick a random stimulus of the appropriate type
		int idx = random.nextInt(typeList.size());
		
		if(color!=null) {
			return typeList.get(idx).factoryClone(color);
		} else {
			return typeList.get(idx).factoryClone( typeList.get(idx).getColor() );
		}
	}
	
	public Stimulus create(StimulusType type) {
		return create(type,null);
	}
	
	public List<Stimulus> createOneOfEach(StimulusType type) {
		List<Stimulus> typeList = stimuli.get(type);
		if(typeList == null) return null;
		
		List<Stimulus> allOfType = new ArrayList<Stimulus>(typeList);
		Collections.shuffle(allOfType, random);
		return allOfType;
	}
	
	public Map<String, Stimulus> getAllOfType(StimulusType type) {
		List<Stimulus> typeList = stimuli.get(type);
		if(typeList == null) return null;
		
		Map<String, Stimulus> nameMap = new HashMap<String, Stimulus>();
		for(Stimulus s : typeList) {
			nameMap.put( s.getName(), s );
		}
		return nameMap;
	}
	
	public String[] getColorNames(StimulusType type) {
		List<Stimulus> typeList = stimuli.get(type);
		if(typeList == null) return null;
		
		String[] colors = new String[typeList.size()];
		int idx=0;
		for(Stimulus s : typeList) {
			colors[idx++] = s.getColor().toString();
		}
		return colors;
	}
	
	public Stimulus getRegisteredExample(StimulusType type, Stimulus request) {
		List<Stimulus> typeList = stimuli.get(type);
		if(typeList == null) return null;
		
		for(Stimulus s : typeList) {
			if(s.getClass().getName().equals(request.getClass().getName())) {
				return s.factoryClone(request.getColor());
			}
		}
		
		return null;
	}
}
