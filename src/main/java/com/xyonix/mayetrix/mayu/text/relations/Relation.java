package com.xyonix.mayetrix.mayu.text.relations;

public class Relation {

	private RelationEntity subject;
	private RelationVerb verb;
	private RelationEntity object;
	private RelationEntity prepositionalComplement;

	public Relation(RelationEntity subject, RelationVerb verb, RelationEntity object, RelationEntity prep) {
		this.subject=subject;
		this.verb=verb;
		this.object=object;
		this.prepositionalComplement=prep;
	}

	public RelationEntity getSubject() {
		return subject;
	}

	public RelationVerb getVerb() {
		return verb;
	}

	public RelationEntity getObject() {
		return object;
	}

	public RelationEntity getPrepositionalComplement() {
		return prepositionalComplement;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String neg = "";
		if(verb.isNegated())
			neg="-";
		sb.append("["+RelationEntity.getAsString(subject)+"] > " + neg + "["+RelationEntity.getAsString(verb)+"] > ["+RelationEntity.getAsString(object)+"]");
		if(prepositionalComplement!=null)
			sb.append(" ^ ["+ RelationEntity.getAsString(prepositionalComplement)+"]");
		
		return sb.toString();
	}
}
