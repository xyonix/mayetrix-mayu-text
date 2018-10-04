package com.xyonix.mayetrix.mayu.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xyonix.mayetrix.mayu.core.FrequencyCounter;
import com.xyonix.mayetrix.mayu.core.Term;

public class TaggedText implements Serializable {

	private static final long serialVersionUID = 1L;
	private String text = null;
	private List<FoundEntity> entities = new ArrayList<FoundEntity>();
	private List<FoundEntity> popularEntities = null;

	public TaggedText(String text) {
		this.text=text;
	}

	/**
	 * Returns popular entities occurring in this block of text based on frequency of occurrence.
	 * @param max The maximum number of entities to return.
	 */
	public List<FoundEntity> getPopularEntities(int max) {
		if(max<1) {
			throw new IllegalArgumentException("max must be > 0");
		}
		if(popularEntities==null) {
			popularEntities = entities;
		}
		if(popularEntities.size()>max) {
			return popularEntities.subList(0, max);
		}
		return popularEntities;
	}

	protected List<FoundEntity> getTopEntities(int max, List<TaggedText> taggedText) {
		Map<String, FoundEntity> nameEntityMap = new HashMap<String, FoundEntity>();
		popularEntities = new ArrayList<FoundEntity>();
		FrequencyCounter fc = new FrequencyCounter();
		for(TaggedText tt:taggedText) {
			for(FoundEntity te:tt.getEntities()) {
				nameEntityMap.put(te.getDisplayName(), te);
				fc.update(te.getDisplayName());
			}
		}
		for(Term t:fc.getMostFrequent(3*max)) {
			FoundEntity te = nameEntityMap.get(t.getValue());
			te.setCount((int)t.getCount());
			popularEntities.add(te);
		}
		if(popularEntities.size()>max) {
			return popularEntities.subList(0, max);
		}
		return popularEntities;
	}

	public String getText() {
		return this.text;
	}

	void setEntities(List<FoundEntity> entities) {
		this.entities=entities;
	}

	public List<FoundEntity> getEntities() {
		return this.entities;
	}  
}
