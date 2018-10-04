package com.xyonix.mayetrix.mayu.text.relations;


public class RelationEntity {

	private String phrase = null;
	private RelationPhrase root = null;
	private RelationPhrase prefixModifier = null;
	private RelationPhrase suffixModifier = null;

	/**
	 * Constructs from only root phrase, i.e. bob>eat>pie where there is no prefix or suffix modifier info for bob, i.e. big bob of arkansas.
	 * @param rootPhrase
	 * @param st
	 */
	RelationEntity(String rootPhrase) {
		this.phrase=rootPhrase;
		root=new RelationPhrase(rootPhrase);
	}

	RelationEntity(String phrase, RelationPhrase prefixModifier, RelationPhrase root, RelationPhrase suffixModifier) {
		this.phrase=phrase;
		this.prefixModifier=prefixModifier;
		this.root=root;
		this.suffixModifier=suffixModifier;
	}

	public RelationPhrase getRoot() {
		return root;
	}

	public RelationPhrase getPrefixModifier() {
		return prefixModifier;
	}

	public RelationPhrase getSuffixModifier() {
		return suffixModifier;
	}

	public String getPhrase() {
		return phrase;
	}
	
	public String toString() {
		if(prefixModifier==null&&suffixModifier==null)
			return getAsString(root);
		
		return getAsString(prefixModifier)+":" + getAsString(root) + ":"+getAsString(suffixModifier);
	}
	
	public static String getAsString(Object re) {
		String s = "";
		if(re!=null)
			s=re.toString();
		
		return s;
	}
}
