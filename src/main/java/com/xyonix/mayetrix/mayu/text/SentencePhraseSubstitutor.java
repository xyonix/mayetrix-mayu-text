package com.xyonix.mayetrix.mayu.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.core.WordGramExtractor;
import com.xyonix.mayetrix.mayu.misc.CollectionUtil;
import com.xyonix.mayetrix.mayu.text.misc.RootConf;

import edu.stanford.nlp.trees.TypedDependency;

public class SentencePhraseSubstitutor {

	static String SUBSTITUTION_PREFIX = "S9890SUB";
	static String SUBSTITUTION_WHITESPACE = "F89WS";
	
	private static Logger logger = LoggerFactory.getLogger(SentencePhraseSubstitutor.class);
	
	/**
	 * Substitutes phrases found in a sentence for those found in an ontology.
	 * 
	 * @param originalSentence The original sentence prior to subs.
	 * @param ontology An ontology.
	 * @param substitutionTypeWhitelist A whitelist of entity types for which the substitution is to be made, i.e. tp=entity/rockets means
	 * the substitution will only be made for entities of type rockets.
	 * @return The substituted string.
	 */
	public static String substitute(String originalSentence, CoreOntology ontology, String[] substitutionTypeWhitelist) {
		
		/**
		 * Using ordered hash map to preserve phrase lengths
		 */
		Map<String, String> substitutions = new LinkedHashMap<String, String>();
		
		String substituted=removeQuotes(originalSentence);
		substituted=VerbPhraseSubstitutor.getInstance().substitute(substituted);
        generateSubstitutions(1, substituted, substitutions, ontology, substitutionTypeWhitelist);
        
        String returnString=postFilterSpecialChars(substitutions, substituted);
        return cropSentenceLength(returnString, RootConf.MAX_SENTENCE_LENGTH);
	}

	private static List<FoundEntity> generateSubstitutions(int minimumWordTokens, String text, Map<String, String> substitutions, CoreOntology ontology, String[] substitutionTypeWhitelist) {
		//Iterate from longest to smallest so longer phrase matches take precendence.
        List<FoundEntity> foundEntities = new ArrayList<FoundEntity>();
        for(int i=6; i>=minimumWordTokens; i--) {
            for(String orig:WordGramExtractor.getGrams(i, text)) {
                addSubstitution(orig, substitutions, foundEntities, ontology, substitutionTypeWhitelist);
            }
        }
        return foundEntities;
	}
	
    private static String postFilterSpecialChars(Map<String, String> substitutions, String substitutedText) {
        List<String> keys = new ArrayList<String>(substitutions.keySet());
        CollectionUtil.sortByLength(keys);
        for(String orig:keys) {
            String replacement = substitutions.get(orig);
            String regex  = "(?i)( |^)"+orig.toLowerCase().replace("+", "\\+").replace("(", "\\(").replace(")", "\\)");
            try {
            	substitutedText=substitutedText.replaceAll(regex, " " +replacement);
            } catch (Exception e) {
                logger.warn("Problems substituting for: '" + orig + "' with '"+substitutions.get(orig)+"' for: " + substitutedText);
                continue;
            }
        }
        return substitutedText;
    }
    
    public static TypedDependency restoreSubstitute(TypedDependency typedDependency) {
    	typedDependency.gov().setValue(restoreSubstitute(typedDependency.gov().value()));
    	typedDependency.dep().setValue(restoreSubstitute(typedDependency.dep().value()));
    	return typedDependency;
    }
    
	private static void addSubstitution(String original, Map<String, String> substitutions, List<FoundEntity> entities, CoreOntology ontology, String[] substitutionTypeWhitelist) {
        if(recordEntity(original, entities, ontology, substitutionTypeWhitelist)) {
            String sub = generateSubstitute(original);
            substitutions.put(original, sub);
        }
    }

    private static boolean recordEntity(String orig, List<FoundEntity> entities, CoreOntology ontology, String[] substitutionTypeWhitelist) {
    	FoundEntity e = ontology.search(orig);
        boolean hasEntity = (e!=null && e.hasAnUnblacklistedType(CollectionUtil.convert(substitutionTypeWhitelist)));
    	if(e!=null) {
            entities.add(e);
        }
        return hasEntity;
    }
    
	/**
	 * Crops long sentences at nearest word boundary otherwise returns the original string.
	 */
	private static String cropSentenceLength(String substitutedText, int desiredLength) {
		if(desiredLength>substitutedText.length())
			return substitutedText;

		BreakIterator bi = BreakIterator.getWordInstance();
		bi.setText(substitutedText);
		int first_after = bi.following(desiredLength);
		if(first_after==-1 || first_after>=substitutedText.length())
			return substitutedText;

		String cropped = substitutedText.substring(0, first_after).trim()+".";
		return cropped;
	}
	
	public static String restoreSubstitute(String phrase) {
		if(phrase==null)
			return null;

		String restored=phrase;
		if(phrase.startsWith(SUBSTITUTION_PREFIX)) {
			restored = phrase.substring(SUBSTITUTION_PREFIX.length()).replaceAll(SUBSTITUTION_WHITESPACE, " ");
			restored = restored.replaceAll("S9890OPAR", "(");
			restored = restored.replaceAll("S9890CPAR", ")");
			restored = restored.replaceAll("S9890SLASH", "/");
			restored = restored.replaceAll("S9890PLUS", "+");
			restored = restored.replaceAll("S9890APOS", "'");
		}
		return restored;
	}
	
	static String restoreSubstituteWordByWord(String phrase) {
		StringBuilder sb = new StringBuilder();
		String[] ws = phrase.split("\\s+");
		if(ws==null) return phrase;
		for(String w:ws) {
			sb.append(restoreSubstitute(w));
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	public static String removeQuotes(String sent) {
		if(sent.startsWith("\""))
			sent=sent.substring(1);
		if(sent.endsWith("\""))
			sent=sent.substring(0,sent.length()-1);
		return sent;
	}

	public static String generateSubstitute(String phrase) {
		return SUBSTITUTION_PREFIX+phrase.replaceAll(" ", SUBSTITUTION_WHITESPACE).replaceAll("/", "S9890SLASH").replaceAll("\\+", "S9890PLUS").replaceAll("'", "S9890APOS").replaceAll("\\(", "S9890OPAR").replaceAll("\\)", "S9890CPAR");
	}
}
