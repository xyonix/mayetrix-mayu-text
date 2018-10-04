package com.xyonix.mayetrix.mayu.text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParser;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Filters;

/**
 * Wrapper w/ default initializations of StanfordParser.
 */
public class StanfordDependencyParser implements DependencyParser {

	private GrammaticalStructureFactory grammaticalStructureFactory = null;
	private static StanfordDependencyParser instance = null;
	private LexicalizedParser parser;
	private static Logger logger = LoggerFactory.getLogger(StanfordDependencyParser.class);
	private TokenizerFactory<Word> tokenizerFactory = PTBTokenizer.factory();

	public static StanfordDependencyParser getInstance() {
		if (instance == null) {
			try {
				instance = new StanfordDependencyParser();
			} catch (Exception e) {
				logger.error("Failed to init StanfordDependencyParser", e);
				throw new MayuTextException("Failed to init StanfordDependencyParser", e);
			}
		}
		return instance;
	}

	public List<TypedDependency> parseSentence(String sentence) {
		List<Word> tokens = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize(); 
        Tree tree = parser.apply(tokens);
        GrammaticalStructure gs = grammaticalStructureFactory.newGrammaticalStructure(tree);
        List<TypedDependency> dependencies = new ArrayList<TypedDependency>();
        for(TypedDependency td:gs.typedDependencies()) {
        	td=SentencePhraseSubstitutor.restoreSubstitute(td);
        	dependencies.add(td);
        }
        return new ArrayList<TypedDependency>(gs.typedDependencies());
	}
	
	private StanfordDependencyParser() throws Exception {
		parser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		String tlpName = "edu.stanford.nlp.trees.PennTreebankLanguagePack";
		TreebankLanguagePack treebankLanguagePack = (TreebankLanguagePack) Class.forName(tlpName).newInstance();
		Filter<String> puncWordFilter = Filters.acceptFilter();
		grammaticalStructureFactory = treebankLanguagePack.grammaticalStructureFactory(puncWordFilter);
	}

}
