package com.xyonix.mayetrix.mayu.text;

/**
 * Parameters involving token parameter extraction.
 */
public class GramParams {

	private boolean isCaseSensitive = false;
	private int maxWordGrams = -1;
	private boolean leaveStopWords = false;
	private boolean includeOntologyPaths = false;
	private boolean includeOntologyPhrases = false;
	private boolean includePunctuationAsWords = false;
	private boolean applyWordStemming = false;
	
	public GramParams(boolean isCaseSensitive, int maxWordGrams, boolean leaveStopWords, boolean includeOntologyPaths, boolean includeOntologyPhrases, boolean applyWordStemming, boolean includePunctuationAsWords) {
		this.isCaseSensitive=isCaseSensitive;
		this.maxWordGrams=maxWordGrams;
		this.leaveStopWords=leaveStopWords;
		this.includeOntologyPaths=includeOntologyPaths;
		this.includeOntologyPhrases=includeOntologyPhrases;
		this.applyWordStemming=applyWordStemming;
		this.includePunctuationAsWords=includePunctuationAsWords;
	}

	public boolean getIncludeOntologyPaths() {
		return includeOntologyPaths;
	}
	
	public boolean getIncludeOntologyPhrases() {
		return includeOntologyPhrases;
	}

	public boolean getApplyStemming() {
		return applyWordStemming;
	}
	
	public boolean getIncludePunctuationAsWords() {
		return this.includePunctuationAsWords;
	}
	
	public void setIncludePunctuationAsWords(boolean p) {
		this.includePunctuationAsWords=p;
	}
	
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public int getMaxWordGrams() {
		return maxWordGrams;
	}

	public boolean getLeaveStopWords() {
		return leaveStopWords;
	}
}
