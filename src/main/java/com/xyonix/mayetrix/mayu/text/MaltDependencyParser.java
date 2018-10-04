package com.xyonix.mayetrix.mayu.text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.io.JarReader;
import com.xyonix.mayetrix.mayu.misc.CollectionUtil;
import com.xyonix.mayetrix.mayu.text.dtrees.DependencyParser;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class MaltDependencyParser implements DependencyParser {

	private static MaltDependencyParser instance = null;
	MaltParserService maltParserService =  null;
	private static Logger logger = LoggerFactory.getLogger(MaltDependencyParser.class);

	public static MaltDependencyParser getInstance() {
		if (instance == null) {
			try {
				instance = new MaltDependencyParser();
			} catch (Exception e) {
				logger.error("Failed to create MaltParser", e);
				throw new MayuTextException("Failed to create MaltParser", e);
			}
		}
		return instance;
	}

	/**
	 * Takes the encoded form like: '5 [I:0][O:1,2,3,5,10]ID:3 FORM:ate LEMMA:_ CPOSTAG:VBN POSTAG:VBN' and returns a 
	 * TreeGraphNode corresponding to: ate-3
	 */
	private TreeGraphNode parseTreeGraphNodeFromEncodedForm(String encodedForm) {
		int id = Integer.parseInt(encodedForm.split(" ")[0]);
		if(!encodedForm.contains("FORM:") || !encodedForm.contains(" LEMMA:"))
			return generateTreeGraphNode("ROOT", id);

		return generateTreeGraphNode(encodedForm.substring(encodedForm.indexOf("FORM:")+5, encodedForm.indexOf(" LEMMA:")), id);
	}

	private String getShortRelName(Edge e) throws MaltChainedException {
		for (SymbolTable t:e.getLabelSet().keySet()) {
			Integer code = e.getLabelSet().get(t);
			return t.getSymbolCodeToString(code);
		}
		return null;
	}

	public List<TypedDependency> parseSentence(String sentence) {
		List<TypedDependency> typedDependencies = new ArrayList<TypedDependency>();
		try {
			DependencyStructure depStructure = maltParserService.parse(generateTokensFromSentence(sentence));
			for(Edge e:depStructure.getEdges()) {
				String shortRelName = getShortRelName(e);
				GrammaticalRelation grammaticalRelation = null;
				if(shortRelName.equalsIgnoreCase("null")) {
					grammaticalRelation=GrammaticalRelation.ROOT;
				} else {
					grammaticalRelation = EnglishGrammaticalRelations.shortNameToGRel.get(shortRelName);
				}					
				typedDependencies.add(new TypedDependency(grammaticalRelation, parseTreeGraphNodeFromEncodedForm(e.getSource().toString()), parseTreeGraphNodeFromEncodedForm(e.getTarget().toString())));
			}
		} catch (Exception e) {
			logger.error("MaltParser: " + e.getMessage());
		}
		return typedDependencies;
	}
	
	private static TreeGraphNode generateTreeGraphNode(String name, int index) {
		TreeGraphNode tgn = new TreeGraphNode();
		CoreLabel ccl = new CoreLabel();
		ccl.setValue(SentencePhraseSubstitutor.restoreSubstitute(name));
		ccl.setIndex(index);
		tgn.setLabel(ccl);
		return tgn;
	}

	private MaltDependencyParser() throws MaltChainedException, IOException, ClassNotFoundException {
		maltParserService = new MaltParserService();
		
		/**
		 * Need to copy this file from the jar to a temp file due to how MP reads 
		 */
		File modelFile = JarReader.addResourceAsFileToTempDir("data/external/maltParser/engmalt.linear-1.7.mco", new File("temp"));
		maltParserService.initializeParserModel("-c engmalt.linear-1.7.mco -m parse -w " + modelFile.getParent() + " -lfi parser.log");		
	}

	/**
	 * Generates tokens as described at: http://nextens.uvt.nl/depparse-wiki/DataFormat - TODO look into ways to improve this.
	 */
	private String[] generateTokensFromSentence(String sentence) throws IOException, ClassNotFoundException {
		List<String> tokens = new ArrayList<String>();
		String tagged = POSTagger.getInstance().tag(sentence);
		int i=1;
		for(String t:tagged.split(" ")) {
			String[] parts = t.split("_");
			tokens.add((i++)+"\t"+parts[0]+"\t_\t"+parts[1]+"\t"+parts[1]);
		}
		return CollectionUtil.convert(tokens);
	}
}
