package com.xyonix.mayetrix.mayu.text;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory.ParserType;

import edu.stanford.nlp.trees.TypedDependency;

public class TestDependencyParser extends TestCase {

	public void testIdenticalParse() {
		assertEqualParserOutput("I eat cake.");
		assertEqualParserOutput("My dog is terrible");
		assertEqualParserOutput("I eat carrots, salad and fries.");
	}

	public void testParse() throws IOException {
		File d = new File("src/test/resources/data/sentences/1/random");
		for(File f:d.listFiles()) {
			if(f.isFile()) {
				for(String sentence:FileUtils.readLines(f)) {
					parseAndPrint(sentence, false);
				}
			}
		}
	}

	void assertEqualParserOutput(String sentence) {
		parseAndPrint(sentence, true);
		assertTrue("Testing parsers have some output on sentence: " + sentence, 
				DependencyParserFactory.generate(ParserType.MALT_PARSER).parseSentence(sentence).toString().equalsIgnoreCase(
						DependencyParserFactory.generate(ParserType.STANFORD_PARSER).parseSentence(sentence).toString())
				);
	}

	private void parseAndPrint(String sentence, boolean includeStanford) {
		System.out.println("\n"+sentence);
		for(TypedDependency td:DependencyParserFactory.generate(ParserType.MALT_PARSER).parseSentence(sentence)) {
			System.out.println("malt parser: " + td.toString());
		}

		if(includeStanford) {
			for(TypedDependency td:DependencyParserFactory.generate(ParserType.STANFORD_PARSER).parseSentence(sentence)) {
				System.out.println("stanford: " + td.toString());
			}
		}
	}

}
