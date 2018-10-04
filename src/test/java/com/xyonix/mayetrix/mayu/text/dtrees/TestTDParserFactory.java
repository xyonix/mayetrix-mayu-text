package com.xyonix.mayetrix.mayu.text.dtrees;

import java.util.List;

import junit.framework.TestCase;

import com.xyonix.mayetrix.mayu.text.Ontology;
import com.xyonix.mayetrix.mayu.text.SentencePhraseSubstitutor;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyTreeNavigator;
import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory.ParserType;
import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate.TDRole;

import edu.stanford.nlp.trees.TypedDependency;

public class TestTDParserFactory extends TestCase {
	
	public void testEntitySubstitutionInParseTrees() {
		assertEntityInTree("I eat pudding.", ParserType.MALT_PARSER);
		assertEntityInTree("I eat pudding.", ParserType.STANFORD_PARSER);
	}
	
	private void assertEntityInTree(String sent, ParserType type) {
		String[] substitutionTypeWhitelist = { "umls" };
		String modified = SentencePhraseSubstitutor.substitute(sent, Ontology.getInstance(), substitutionTypeWhitelist);
		List<TypedDependency> tds = DependencyParserFactory.generate(type).parseSentence(modified);
		DependencyTreeNavigator tdn = new DependencyTreeNavigator(tds);
		assertTrue(type.toString(), tdn.getCoordinate(new TDCoordinate("dobj", TDRole.DEPENDENT)).get(0).nodeString().equalsIgnoreCase("pudding"));
		for(TypedDependency td:tds) {
			System.out.println(td);
		}
	}

}
