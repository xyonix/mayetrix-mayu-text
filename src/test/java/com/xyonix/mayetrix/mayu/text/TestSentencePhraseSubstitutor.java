package com.xyonix.mayetrix.mayu.text;

import com.xyonix.mayetrix.mayu.text.FoundEntity;
import com.xyonix.mayetrix.mayu.text.Ontology;
import com.xyonix.mayetrix.mayu.text.SentencePhraseSubstitutor;

import junit.framework.TestCase;

public class TestSentencePhraseSubstitutor extends TestCase {

	private void prepOnto() {
		FoundEntity te = new FoundEntity("purple worms");
		te.addPath("popsicles");
		Ontology.getInstance().addEntity(te);
	}
	
	public void testSubstituteRestore() {
		prepOnto();
		String t = "purple worms";
		assertTrue(t.equals(SentencePhraseSubstitutor.restoreSubstitute(SentencePhraseSubstitutor.generateSubstitute(t))));
	}
	
	public void testSubstitute() {
		prepOnto();
		assertMod("Happily eat purple grapes", "Eat purple grapes");
		assertMod("I happily eat purple grapes", "I eat purple grapes");
		assertMod("I happily eat grapes", "I eat grapes");
		assertMod("I take labradors.", null);
		assertMod("\"I eat purple worms on Wednesdays\"", "I eat " + SentencePhraseSubstitutor.SUBSTITUTION_PREFIX+"purple" 
		+ SentencePhraseSubstitutor.SUBSTITUTION_WHITESPACE 
				+ "worms on Wednesdays");
	}

	public void testRestoreSubstitute() {
		String p = SentencePhraseSubstitutor.restoreSubstituteWordByWord("'m");
		assertTrue(p.equalsIgnoreCase("'m"));
	}
	
	public void testRestoreSubstituteWordByWord() {
		String sp = SentencePhraseSubstitutor.generateSubstitute("Jimbo")+" eats "+SentencePhraseSubstitutor.generateSubstitute("grapes");
		String p = SentencePhraseSubstitutor.restoreSubstituteWordByWord(sp);
		assertTrue(p.equalsIgnoreCase("Jimbo eats grapes"));
	}
	
	private void assertMod(String original, String match) {
		String[] substitutionTypeWhitelist = { "popsicles" };
		System.out.println(original);
		String mod = SentencePhraseSubstitutor.substitute(original, Ontology.getInstance(), substitutionTypeWhitelist);
		System.out.println(mod+"\n");
		if(match!=null)
			assertTrue(mod.trim().equalsIgnoreCase(match.trim()));
	}
}
