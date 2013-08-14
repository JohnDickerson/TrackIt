package edu.cmu.cs.eyetrack.analysis.struct.tobii;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TobiiEventMap {

	public static enum TobiiEventType { 
		SCREEN_REC_STARTED("ScreenRecStarted"),
		IMAGE_START("ImageStart"), 
		IMAGE_END("ImageEnd"), 
		MOVIE_START("MovieStart"), 
		MOVIE_END("MovieEnd"), 
		LEFT_MOUSE_CLICK("LeftMouseClick"),
		RIGHT_MOUSE_CLICK("RightMouseClick"),
		KEYPRESS("KeyPress"),
		SCREEN_REC_STOPPED("ScreenRecStopped");
	
		private final String tobiiString;
		
		TobiiEventType(String tobiiString) {
			this.tobiiString = tobiiString;
		}
		
		private String getTobiiString() { return tobiiString; }
		
		public static TobiiEventType parseTobii(String eventStr) {
			for(TobiiEventType type : TobiiEventType.values()) {
				if(eventStr.equalsIgnoreCase(type.getTobiiString())) {
					return type;
				}
			}
			return null;
		}
	};
	
	private TobiiHeader header;
	private Map<TobiiEventType, List<Event>> eventMap;
	
	public TobiiEventMap(TobiiHeader header) {
		this.header = header;
		eventMap = new HashMap<TobiiEventType, List<Event>>();
		for(TobiiEventType type : TobiiEventType.values()) {
			eventMap.put(type, new ArrayList<Event>());
		}
	}
	
	public TobiiEventMap() {
		this(null);
	}
	
	public List<Event> getEvents(TobiiEventType type) {
		return eventMap.get(type);
	}
	
	public TobiiHeader getHeader() {
		return header;
	}

	public void setHeader(TobiiHeader header) {
		this.header = header;
	}

	public void addEvent(TobiiEventType type, long timestamp, int trialId) {
		addEvent(type, timestamp, trialId, "");
	}
	
	public void addEvent(TobiiEventType type, long timestamp, int trialId, String descriptor) {
		eventMap.get(type).add(new Event(type, timestamp, trialId, descriptor));
	}
	
	public class Event {
		
		private TobiiEventType type;
		private long timestamp;
		private int trialId;
		private String descriptor;
		
		public Event(TobiiEventType type, long timestamp, int trialId, String descriptor) {
			this.type = type;
			this.timestamp = timestamp;
			this.trialId = trialId;
			this.descriptor = descriptor;
		}

		public Event(TobiiEventType type, long timestamp, int trialId) {
			this(type, timestamp, trialId, "");
		}
		
		public TobiiEventType getType() {
			return type;
		}

		public void setType(TobiiEventType type) {
			this.type = type;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public int getTrialId() {
			return trialId;
		}

		public void setTrialId(int trialId) {
			this.trialId = trialId;
		}

		public String getDescriptor() {
			return descriptor;
		}

		public void setDescriptor(String descriptor) {
			this.descriptor = descriptor;
		}
		
		
	}
}
