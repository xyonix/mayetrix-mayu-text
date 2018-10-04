package com.xyonix.mayetrix.mayu.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reduces dimensionality for improved accuracy on some classifiers. Filters common words and excessively unique words
 * (e.g. in only 1 doc and hence prone to model over fitting). Enables higher accuracy in text classification tasks.
 */
public class ImportantWordsFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<String, Set<String>> labelFilterMap = null;	

	public Set<String> getLabels() {
		return labelFilterMap.keySet();
	}

	public Set<String> getFilter(String label) {
		return labelFilterMap.get(label);
	}

	/**
	 * Computes important words.
	 * 
	 * @param threshold Value between 1 and 10. Lower the number, more words you get back. Mid range is good to start.
	 * @param instances Instances of training document data.
	 */
	static Set<String> compute(List<TaggedWordList> instances, double threshold) throws MayuTextException {

		HashMap<String, ArrayList<String[]>> wordListsAndLabels = new HashMap<String, ArrayList<String[]>>();

		for (TaggedWordList id : instances) {
			String target = id.getLabel();
			if(wordListsAndLabels.containsKey(target.toString()))
				wordListsAndLabels.get(target.toString()).add(id.getWords());
			else {
				ArrayList<String[]> al = new ArrayList<String[]>();
				al.add(id.getWords());
				wordListsAndLabels.put(target.toString(), al);
			}				
		}

		BigramCalculator bc = new BigramCalculator();
		try {
			return bc.calcImportantWords(wordListsAndLabels, threshold);
		} catch (Exception e) {
			throw new MayuTextException("Probs computing important words", e);
		}
	}
}

class BigramCalculator {

	protected double[] calcBigramContingencyValues(double n_ii, double n_ix, double n_xi, double n_xx) {

		double n_oi = n_xi - n_ii;
		double n_io = n_ix - n_ii; 
		double[] rA = new double[4];

		rA[0] = n_ii;
		rA[1] = n_oi;
		rA[2] = n_io;
		rA[3] = n_xx - n_ii - n_oi - n_io;

		return rA;
	}

	protected double calcChiSquareBigramScore(double n_ii, double n_ix, double n_xi, double n_xx) {
		return n_xx * calcPhiSquareBigramScore(n_ii, n_ix, n_xi, n_xx);
	}

	protected double calcPhiSquareBigramScore(double n_ii, double n_ix, double n_xi, double n_xx) {
		
		double[] contingency = calcBigramContingencyValues(n_ii,n_ix,n_xi,n_xx);

		n_ii = contingency[0];
		n_ix = contingency[1];
		n_xi = contingency[2];
		n_xx = contingency[3];

		return java.lang.Math.pow(n_ii*n_xx - n_ix*n_xi, 2) / ((n_ii + n_ix) * (n_ii + n_xi) * (n_ix + n_xx) * (n_xi + n_xx));
	}

	HashSet<String> calcImportantWords(HashMap<String, ArrayList<String[]>> hm, double min_score) throws Exception {
		
		FrequencyDistribution wordFreqDist = new FrequencyDistribution();
		ConditionalFrequencyDistribution labelWordFreqDist = new ConditionalFrequencyDistribution();

		Set<String> labels = hm.keySet();
		for(String label : labels) {
			ArrayList<String[]> wordListsArray = hm.get(label);
			for (String[] wordLists : wordListsArray) {
				for (String word : wordLists) {
					wordFreqDist.increment(word);
					labelWordFreqDist.increment(label, word);
				}
			}
		}

		int n_xx = labelWordFreqDist.calcMassSizeSum();
		HashSet<String> importantWords = new HashSet<String>();
		for (String condition : labelWordFreqDist.getConditions()) {
			int n_xi = labelWordFreqDist.getFrequencyDistribution(condition).getMass();

			HashMap<String, Double> word_scores = new HashMap<String, Double>();
			FrequencyDistribution fd = labelWordFreqDist.getFrequencyDistribution(condition);
			for(String word : fd.getWords()) {
				int n_ii = fd.getFreqValue(word);
				int n_ix = wordFreqDist.getFreqValue(word);
				double score = calcChiSquareBigramScore(n_ii, n_ix, n_xi, n_xx);
				word_scores.put(word,score);
			}

			ArrayList<String> bestWords = new ArrayList<String>();
			for(String word : word_scores.keySet()) {
				if(word_scores.get(word) >= min_score) {
					bestWords.add(word);
				}
			}
			importantWords.addAll(bestWords);
		}

		return importantWords;
	}
}

class ConditionalFrequencyDistribution {
	
	private HashMap<String, FrequencyDistribution>	wordFreqMap = new HashMap<String, FrequencyDistribution>();

	void printFrequencyDistribution() {
		
		HashSet<String> keys = new HashSet<String>();
		for (String condition : wordFreqMap.keySet()) {
			FrequencyDistribution fd = wordFreqMap.get(condition);
			keys.addAll(fd.getWords());
		}

		String[][] m = new String[wordFreqMap.keySet().size()][keys.size()];

		int keyCounter = 0;
		for (String type : keys) {
			int condCounter = 0;
			for(String condition : wordFreqMap.keySet()) {
				System.out.println(condition);
				m[condCounter++][keyCounter] = type + ":" + wordFreqMap.get(condition).getFreqValue(type);
			}
			keyCounter++;
		}

		for (int i = 0; i < wordFreqMap.keySet().size(); i++) {
			System.out.print(i + "\t");
			for (int j = 0; j < keys.size(); j++) {
				System.out.print(m[i][j] + "\t");
			}
			System.out.println();
		}
	}

	void increment(String condition, String word) {
		if(wordFreqMap.containsKey(condition)) {
			FrequencyDistribution fd = wordFreqMap.get(condition);
			fd.increment(word);
		} else {
			FrequencyDistribution fd = new FrequencyDistribution();
			fd.increment(word);
			wordFreqMap.put(condition, fd);
		}
	}

	int calcHeadSizeSum() {
		int sum = 0;
		for (String key : wordFreqMap.keySet()) {
			FrequencyDistribution fd = wordFreqMap.get(key);
			sum += fd.getSize();
		}
		return sum;
	}

	int calcMassSizeSum() {
		int sum = 0;
		for (String key : wordFreqMap.keySet())
		{
			FrequencyDistribution fd = wordFreqMap.get(key);
			sum += fd.getMass();
		}
		return sum;
	}

	Set<String> getConditions() {
		return wordFreqMap.keySet();
	}

	FrequencyDistribution getFrequencyDistribution(String condition) throws Exception {
		FrequencyDistribution fd = wordFreqMap.get(condition);
		return fd;
	}
}

class FrequencyDistribution {
	
	private HashMap<String, Integer>	wordFreqMap = new HashMap<String, Integer>();

	void increment(String word) {
		if(wordFreqMap.containsKey(word))
			wordFreqMap.put(word, wordFreqMap.get(word)+1);
		else
			wordFreqMap.put(word, 1);
	}

	int getSize() {
		return wordFreqMap.size();
	}

	int getMass() {
		int sum = 0;
		for(String word : getWords())
		{
			sum += wordFreqMap.get(word);
		}
		return sum;
	}

	Set<String> getWords() {
		return wordFreqMap.keySet();
	}

	int getFreqValue(String key) {
		if (wordFreqMap.containsKey(key))
			return wordFreqMap.get(key);
		else
			return 0;
	}

	String printFreqValues() {
		StringBuilder sb = new StringBuilder();

		for (String type : getWords()) {
			sb.append(type+":"+getFreqValue(type) + "\t");
		}

		return sb.toString();
	}
}