package com.xyonix.mayetrix.mayu.text;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Simplifies access to Stanford MaxEnt POS tagger.
 */
public class POSTagger {
	
	private static POSTagger instance = null;
	private MaxentTagger maxEntTagger = null;
    private static Logger logger = LoggerFactory.getLogger(Ontology.class);

	public static POSTagger getInstance() {
		if (instance == null) {
			try {
				instance = new POSTagger();
			} catch (Exception e) {
				logger.error("Failed to initialize POS tagger", e);
				throw new MayuTextException("Failed to initialize POS tagger", e);
			}
		}
		return instance;
	}
	
	public String tag(String sentence) {
		return maxEntTagger.tagString(sentence);
	}

	private POSTagger() throws IOException, ClassNotFoundException {
		maxEntTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");		
	}
}
