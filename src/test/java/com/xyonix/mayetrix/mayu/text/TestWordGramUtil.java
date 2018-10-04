package com.xyonix.mayetrix.mayu.text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.text.BasicTagger;
import com.xyonix.mayetrix.mayu.text.GramParams;
import com.xyonix.mayetrix.mayu.text.Ontology;
import com.xyonix.mayetrix.mayu.text.TaggedText;
import com.xyonix.mayetrix.mayu.text.WordGramUtil;

public class TestWordGramUtil extends TestCase {
	
	private static Logger logger = LoggerFactory
			.getLogger(TestWordGramUtil.class);
	
	public void testGetWordsFromString() {
		Ontology.getInstance().addEntitiesFromFile(new String[]{
                "/data/ontology/ot-4.onto"
        });
		BasicTagger.getInstance().setOntology(Ontology.getInstance());
		TaggedText t = new TaggedText("I'm feeling Big pigs, small cows, and green dogs are good animals!");
		BasicTagger.getInstance().tag(t);
		
		Set<String> typeBlacklist =new HashSet<String>();
		typeBlacklist.add("pronoun");
		typeBlacklist.add("sip");
		typeBlacklist.add("preferred_consumer_term");
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 2, true, true, false, true, true), typeBlacklist), "ENDS_WITH_EXCLAMATION_POINT", true);
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 2, true, true, false, true, false), typeBlacklist), "green_dog", true);
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 2, true, true, false, false, false), typeBlacklist), "green_dogs", true);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(true, 2, true, true, false, false, false), typeBlacklist), "Big_pigs", true);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, true, true, false, false, false), typeBlacklist), "big_pigs", false);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, true, true, false, false, false), typeBlacklist), "cows", true);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, true, true, false, false, false), typeBlacklist), "and", true);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, false, true, false, false, false), typeBlacklist), "and", false);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, false, true, false, false, false), typeBlacklist), "entity_lang_pos_adjective_positive", true);		
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, false, false, false, false, false), typeBlacklist), "i'm_feeling", false);
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(false, 1, false, false, true, true, false), typeBlacklist), "i'm_feeling", true);
		assertContains(WordGramUtil.getWordsFromString(t, new GramParams(true, 1, false, false, true, true, false), typeBlacklist), "i'm_feeling", true);		
	}
	
	private void assertContains(List<String> grams, String member, boolean contains) {
		String in = "in";
		if(!contains)
			in = "not in";
		
		logger.info("looking for: " + member + " "+in+": ");
		for(String g:grams) {
			logger.info("gram: " + g);
		}
		Set<String> set = new HashSet<String>(grams);
		if(contains)
			assertTrue(set.contains(member));
		else
			assertTrue(!set.contains(member));
	}

}
