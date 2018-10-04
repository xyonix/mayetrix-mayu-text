package com.xyonix.mayetrix.mayu.text.relations;

import java.util.List;

import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyTreeNavigator;
import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory.ParserType;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyTreeNavigator.TypedDependencyRole;

import junit.framework.TestCase;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class TestTDNavigator extends TestCase {

	public void testGetCoordinate() {
		List<TypedDependency> tds = DependencyParserFactory.generate(ParserType.MALT_PARSER).parseSentence("I eat pie.");
		for(int i=0; i<tds.size(); i++) {
			System.out.println(i+"="+tds.get(i).toString());
		}
		
		DependencyTreeNavigator tdn = new DependencyTreeNavigator(tds);
		for(TreeGraphNode t:tdn.getCoordinate(new TDCoordinate("nsubj", com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate.TDRole.GOVERNOR))) {
			assertTrue(t.nodeString().equalsIgnoreCase("eat"));
		}
	}
	
	public void testFilterTypes() {
		List<TypedDependency> tds = TestRelationGenerator.genTD("I eat pie");
		for (TypedDependency t:tds) {
			System.out.println(t);
		}
		
		DependencyTreeNavigator tdn = new DependencyTreeNavigator(tds);
		List<TreeGraphNode> original = tdn.getMatchingDependents(tds.get(0).gov());
		
		print(original, "Original:");
		
		String[] bannedTypes = {"nsubj"};
		List<TreeGraphNode> f = tdn.filterTypes(original, bannedTypes, TypedDependencyRole.DEPENDENT);
		
		print(f, "Filtered:");
		assertTrue(original.size()==2);
		assertTrue(f.size()==1);
	}
	
	private void print(List<TreeGraphNode> fs, String header) {
		System.out.println("*** " + header);
		for(TreeGraphNode t:fs) {
			System.out.println(t.toString());
		}
		System.out.println("***");
	}
	
	public TestTDNavigator(String name) {
		super(name);
	}

	public void testGetDependencyType() {
		assertDependencyType("Docetaxel is effective", "effective", "nsubj_gov");
		assertDependencyType("Docetaxel is very effective", "effective", "nsubj_gov");
		assertDependencyType("Docetaxel is on occassion very effective", "effective", "prep_gov");
	}

	private void assertDependencyType(String sentence, String nodeName, String nodeLocation) {
		List<TypedDependency> tds = DependencyParserFactory.generate(ParserType.STANFORD_PARSER).parseSentence(sentence);
		System.out.println(tds);
		System.out.println("BEGIN ASSERT LOCATION");
		DependencyTreeNavigator dn = new DependencyTreeNavigator(tds);
		List<String> locs = dn.getDependencyType(nodeName, TypedDependencyRole.EITHER, true); 
		for(String l:locs) {
			System.out.println(l);
		}
		System.out.println("END ASSERT LOCATION");
		assertTrue(locs.contains(nodeLocation));
	}

	public void testGetMatchingGovernors() {
		List<TypedDependency> tds = DependencyParserFactory.generate(ParserType.MALT_PARSER).parseSentence("I eat pie.");
		for(int i=0; i<tds.size(); i++) {
			System.out.println(i+"="+tds.get(i).toString());
		}
		System.out.println(tds);
		DependencyTreeNavigator dn = new DependencyTreeNavigator(tds);
		for(TreeGraphNode tgn:dn.getMatchingGovernors(tds.get(0).gov())) {
			System.out.println(tgn.nodeString());
			assertTrue(tgn.nodeString().equalsIgnoreCase("root"));
		}
	}
}
