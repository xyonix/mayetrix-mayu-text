package com.xyonix.mayetrix.mayu.text;

import java.io.File;

import junit.framework.TestCase;

import com.xyonix.mayetrix.mayu.core.LookupSet;
import com.xyonix.mayetrix.mayu.text.CachingStemmer;

public class TestStemmer extends TestCase {

	public TestStemmer(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void testOneWord() throws Exception {
		System.out.println(CachingStemmer.getInstance().stem("pig's"));
		assertTrue(CachingStemmer.getInstance().stem("pig's").equalsIgnoreCase("pig"));
	}
	
	public void testFirstWordInPhraseStem() throws Exception {
		System.out.println(CachingStemmer.getInstance().stem("pig's den"));
		assertTrue(CachingStemmer.getInstance().stem("pig's den").equalsIgnoreCase("pig den"));
		
		System.out.println(CachingStemmer.getInstance().stem("pig's dog's den"));
		assertTrue(CachingStemmer.getInstance().stem("pig's dog's den").equalsIgnoreCase("pig dog's den"));
	}

	public void testOneLiner() throws Exception {
		System.out.println(CachingStemmer.getInstance().stem("'m"));
	}

	public void testStem() throws Exception {
		File f = new File("src/test/resources/data/stress_strings.txt");

		for(String s:LookupSet.getFromFile(f.getAbsolutePath(), false)) {
			String stemmed = CachingStemmer.getInstance().stem((s));
			assertTrue(stemmed!=null);
		}

		assertTrue(CachingStemmer.getInstance().stem("Pig farms on the lake's").equalsIgnoreCase("Pig farms on the lake"));
	}
}
