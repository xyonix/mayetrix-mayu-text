package com.xyonix.mayetrix.mayu.text.relations;

import java.util.List;

import junit.framework.TestCase;

import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParserFactory.ParserType;
import com.xyonix.mayetrix.mayu.text.relations.Relation;
import com.xyonix.mayetrix.mayu.text.relations.RelationGenerator;

import edu.stanford.nlp.trees.TypedDependency;

public class TestRelationGenerator extends TestCase {

	public void testGenerate() {
		assertMinNumRels("I'm not a dog on crack.", 5, true);
		assertMinNumRels("I'm a dog on crack.", 5, false);
		assertMinNumRels("I am not a crackhead", 1, true);
		assertMinNumRels("I am a crackhead", 1, false);
		assertMinNumRels("I am not crackhead and anemic", 2, true);
		assertMinNumRels("I am crackhead and anemic", 2, false);
		assertMinNumRels("Lemon is not eaten by Yoyo.", 2, true);
		assertMinNumRels("Lemon is eaten by Yoyo.", 2, false);
		assertMinNumRels("I do not eat lemon.", 2, true);
		assertMinNumRels("I eat lemon.", 1, false);
		assertMinNumRels("I do not eat lemon, cake or cookies.", 4, true);
		assertMinNumRels("I eat lemon, cake and cookies.", 3, false);
		assertMinNumRels("Hariom, Ramesh and Yoyo do not eat lemon.", 3, true);
		assertMinNumRels("Hariom, Ramesh and Yoyo eat lemon.", 3, false);
		assertMinNumRels("Hariom and Ramesh are not morons or idiots.", 4, true);
		assertMinNumRels("Hariom, Ramesh and Yoyo eat lemon, cake and cookies.", 9, false);
	}

	private void assertMinNumRels(String sent, int numRels, boolean isNegated) {
		System.out.println("\n" + sent);
		List<Relation> rels = RelationGenerator.getInstance().generate(genTD(sent));
		for(Relation r:rels) {
			if (r.toString().contains("[nsubj]")) continue;
			
			if(isNegated)
				assertTrue(r.getVerb().isNegated());
			else
				assertTrue(!r.getVerb().isNegated());

			System.out.println(r.toString());
		}
		System.out.println("Has " + rels.size() + " relations");
		assertTrue(rels.size()>=numRels);
	}

	static List<TypedDependency> genTD(String sent) {
		List<TypedDependency> deps = DependencyParserFactory.generate(ParserType.MALT_PARSER).parseSentence(sent);
		for(TypedDependency t:deps) {
			System.out.println(t);
		}
		return deps;
	}

}
