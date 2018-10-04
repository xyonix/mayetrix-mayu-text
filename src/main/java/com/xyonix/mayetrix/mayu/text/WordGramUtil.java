package com.xyonix.mayetrix.mayu.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.xyonix.mayetrix.mayu.core.WordGramExtractor;
import com.xyonix.mayetrix.mayu.data.TaxPath;

public class WordGramUtil extends WordGramExtractor {

	public static List<String> getWordsFromString(TaggedText taggedText, GramParams gramParams, Set<String> typeBlacklist) {
		String contents = taggedText.getText();
		if(!gramParams.isCaseSensitive())
			contents=contents.toLowerCase();

		List<String> grams = new ArrayList<String>();		
		if(gramParams.getMaxWordGrams()>1) {
			for(String g:WordGramExtractor.getGramsInRange(gramParams.getMaxWordGrams(), contents)) {
				if(gramParams.getApplyStemming())
					g=CachingStemmer.getInstance().stem(g);

				g = g.replaceAll(" ", "_");
				grams.add(g);
			}
		} else {
			for(String gram:get1Gram(contents, gramParams.getLeaveStopWords())) {
				if(gramParams.getApplyStemming())
					gram=CachingStemmer.getInstance().stem(gram);
				grams.add(gram);
			}
		}

		if(gramParams.getIncludeOntologyPaths()) {
			for(FoundEntity te:taggedText.getEntities()) {
				for(TaxPath tp:te.getPaths()) {
					boolean add=true;
					for (String s:typeBlacklist) {
						if(tp.hasNode(s)) {
							add=false;
							break;
						}
					}
					if(add) {
						grams.add(tp.getName().replaceAll("/", "_"));
					}
				}
			}
		}

		if(gramParams.getIncludeOntologyPhrases()) {
			for(FoundEntity te:taggedText.getEntities()) {
				if(te.getName().split(" ").length>1) {
					grams.add(te.getName().replaceAll(" ", "_"));
				}
			}
		}

		if(gramParams.getIncludePunctuationAsWords()) {
			if(taggedText.getText().endsWith("?"))
				grams.add("ENDS_WITH_QUESTION_MARK");
			else if(taggedText.getText().endsWith("!"))
				grams.add("ENDS_WITH_EXCLAMATION_POINT");
			else if(taggedText.getText().endsWith("..."))
				grams.add("ENDS_WITH_ELLIPSES");
		}
		return grams;		
	}	
}
