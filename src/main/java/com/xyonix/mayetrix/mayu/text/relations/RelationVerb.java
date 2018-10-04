package com.xyonix.mayetrix.mayu.text.relations;

public class RelationVerb extends RelationEntity {

	private boolean negated = false;
	
	RelationVerb(String rootPhrase, boolean negated) {
		super(rootPhrase);
		this.negated=negated;
	}
	
	public boolean isNegated() {
		return this.negated;
	}
}
