package com.xyonix.mayetrix.mayu.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.data.TaxPath;

public class BasicFeatureExtractor {

	private static Logger logger = LoggerFactory.getLogger(BasicFeatureExtractor.class);

	/**
	 * Returns whitespace separated word tokens consisting of multiword entity names (single word tokens presumed extracted
	 * elsewhere) + entity types "wordified"
	 * @param entities
	 */
	protected String generateWordTokens(List<FoundEntity> entities, Set<String> type_blacklist) {
		StringBuilder sb = new StringBuilder();
		for (FoundEntity fE:entities) {
			if(fE.getName().indexOf(" ")!=-1) {
				sb.append(fE.getName().toLowerCase().replace(" ", "_"));
				sb.append(" ");
			}
			for (TaxPath t:fE.getPaths()) {
				if (type_blacklist.contains(t.getName()))
					continue;
				
				sb.append(t.getName().replaceAll("/", "_"));
				sb.append(" ");
			}
	        logger.info(fE.toReadableString());
	    }
		logger.info(sb.toString().trim());
		return sb.toString().trim();
	}
	
	protected List<String> generateSentencePermutations(String sentence, BasicTagger tagger, Ontology ontology) throws IOException {
		sentence=sentence.toLowerCase();
		Set<String> triggerSentences = new HashSet<String>();
		List<String> outSentences = new ArrayList<String>();
		triggerSentences.add(sentence);
		//outSentences.add(sentence);

		Map<String, List<String>> entityToSynMap = new HashMap<String, List<String>>();
		for (FoundEntity fe:tagger.tag(sentence)) {
			entityToSynMap.put(fe.getName().toLowerCase(), new ArrayList<String>());
			if (fe!=null) {
				if (fe.hasPathFragment("linguistic_permutation")) {
					for (TaxPath t:fe.getPathsWithType("linguistic_permutation")) {
						for (FoundEntity candidateFEs:ontology.getAllEntitiesWithType(t.getLeaf())) {
							if (candidateFEs.hasPathFragment("linguistic_permutation/"+t.getLeaf()) && !candidateFEs.getName().contains("[")) {
								entityToSynMap.get(fe.getName().toLowerCase()).add(candidateFEs.getName());
							}
						}
					}
				}
			}
		}
		String[] punct = {" ", ",","."}; //needed so don't crazy substituting things like diagnostic for short terms like no., so we look as a word or punct boundary.
		for (String entity:entityToSynMap.keySet()) {
			for (String synonym:entityToSynMap.get(entity)) {
				for (String triggerSentence:triggerSentences) {
					for (String p:punct) {
						String entityWPunctOrSpace = entity+p;
						String synonymWPunctOrSpace = synonym+p;
						if (triggerSentence.contains(entityWPunctOrSpace)) {
							outSentences.add(triggerSentence.replace(entityWPunctOrSpace, synonymWPunctOrSpace));
						}
					}
				}
			}
			for (String s:outSentences) {
				triggerSentences.add(s);
			}

		}
		return outSentences;
	}
}
