package com.xyonix.mayetrix.mayu.text;

public class TaggedWordList {

	private String[] words;
	private String tag;

	/**
	 * Constructs object.
	 * 
	 * @param tag A tag representing the group of words like: news, entertainment.
	 * @param words represented by the tag as they appear docs.
	 */
	public TaggedWordList(String tag, String[] words) {
		this.tag = tag;
		this.words=words;
	}
	
	public String getLabel() {
		return this.tag;
	}
	
	public String[] getWords() {
		return words;
	}
}

