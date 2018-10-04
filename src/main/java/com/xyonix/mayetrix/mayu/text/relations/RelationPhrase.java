package com.xyonix.mayetrix.mayu.text.relations;

import java.util.List;

import com.xyonix.mayetrix.mayu.text.FoundEntity;
import com.xyonix.mayetrix.mayu.text.misc.RootConf;

public class RelationPhrase {

	private String phrase = null;
	private List<FoundEntity> entities = null;
	
	public RelationPhrase(String phrase) {
		this.phrase=phrase;
		this.entities= RootConf.ENTITY_TAGGER.tag(phrase); //Uses Conf set tagger to generate entities.
	}
	
	public String getPhrase() {
		return phrase;
	}
	
	public List<FoundEntity> getEntities() {
		return entities;
	}
	
	public String toString() {
		if(phrase==null)
			return "";
		return phrase;
	}
}
