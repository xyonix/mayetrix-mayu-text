package com.xyonix.mayetrix.mayu.text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;

public class SentenceSplitter {

	private final static String GRAMMATICAL_CLAUSE_ENDINGS = "/',;:.!?";
	private final static String[] SENTENCE_DELIMITERS = {".", "!", "...", "?"};

	public List<TaggedText> split(String body) {
		DocumentPreprocessor docPreprocessor = new DocumentPreprocessor(new StringReader(body));
		docPreprocessor.setTokenizerFactory(PTBTokenizerFactory.newWordTokenizerFactory("ptb3Escaping=false, " +
			"normalizeParentheses=false, normalizeOtherBrackets=false, asciiQuotes=true, ptb3Ellipsis=true, escapeForwardSlashAsterisk=false, americanize=false"));
		docPreprocessor.setSentenceFinalPuncWords(SENTENCE_DELIMITERS);
		List<TaggedText> sentences = new ArrayList<TaggedText>();
		Iterator<List<HasWord>> wordsIterator = docPreprocessor.iterator();
		while(wordsIterator.hasNext()) {
			StringBuilder sb = new StringBuilder();
			List<HasWord> sentence = wordsIterator.next();
			for (HasWord w : sentence) {
				if(!isEnding(w.word())&&!isBlacklisted(w.word()))
					sb.append(" ");

				sb.append(w.word());
			}
			sentences.add(new TaggedText(sb.toString().trim()));
		}
		return sentences;
	}

	private static boolean isBlacklisted(String term) {
		if(term.contains("'"))
			return true;

		return false;
	}

	private static boolean isEnding(String word) {
		if(word==null||word.length()!=1)
			return false;
		return GRAMMATICAL_CLAUSE_ENDINGS.indexOf(word)!=-1;
	}
}
