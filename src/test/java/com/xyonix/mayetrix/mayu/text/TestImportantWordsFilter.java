package com.xyonix.mayetrix.mayu.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.xyonix.mayetrix.mayu.text.ImportantWordsFilter;
import com.xyonix.mayetrix.mayu.text.TaggedWordList;

import junit.framework.TestCase;

public class TestImportantWordsFilter extends TestCase {
		
	public void testCalcImportantWords() throws Exception {

		String[] bow0={"pigs", "you", "whooo", "pigs",  "uh", "uh", "it", "i", "the", "bazow"};
		String[] bow1={"chickens", "cazow", "red", "crater", "uh", "it", "i", "the", "shmawow"};
		String[] bow2={"pluto", "world", "water", "crater", "uh", "it", "i", "the", "blahblah"};

		List<TaggedWordList> id = new ArrayList<TaggedWordList>();
		id.add(new TaggedWordList("pigs", bow0));
		id.add(new TaggedWordList("chickens", bow1));
		id.add(new TaggedWordList("pluto", bow2));
		
		Set<String> hi = ImportantWordsFilter.compute(id, 2);
		for(String h:hi) {
			System.out.println("Important word: " + h);
		}
		assertTrue(ImportantWordsFilter.compute(id, 1).size()>ImportantWordsFilter.compute(id, 3).size());
	}
}
