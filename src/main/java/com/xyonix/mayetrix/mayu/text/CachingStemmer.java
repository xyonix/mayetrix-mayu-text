package com.xyonix.mayetrix.mayu.text;

import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.text.misc.RootConf;

import edu.stanford.nlp.process.Morphology;

/**
 * Stanford Stemmer w/ caching and basic protections for very short words, etc.
 */
public class CachingStemmer {

    private static final Logger logger = LoggerFactory.getLogger(CachingStemmer.class);

    private Morphology stanfordStemmer = new Morphology();
    private static CachingStemmer instance = null;
    private final LRUMap cache = new LRUMap(RootConf.STEMMER_CACHE_SIZE);

    public static CachingStemmer getInstance() {
        if(instance==null) {
            instance=new CachingStemmer();
        }
        return instance;
    }

    private CachingStemmer() {
    }
    
    public String stem(String original) {
    	String stem = (String)cache.get(original);
    	if (null == stem) {
    		stem = stemImpl(original);
    		cache.put(original,  stem);
    	}
    	return stem;
    }
    
    private String stemImpl(String original) {
    	String modifiedWord = original;

        if(modifiedWord.length()<3)
            return modifiedWord;

        try {
            boolean isPhrase = modifiedWord.contains(" ");
            StringBuilder prefix=new StringBuilder();
            if(isPhrase) {
                String[] parts = modifiedWord.split("\\s+");
                if(parts.length>1) {
                    for(int i=0; i<parts.length-1; i++) {
                        if(i>0)
                            prefix.append(" ");
                        if(i==0) //handle case like pig's den
                        	parts[i]=stem(parts[i]);
                        
                        prefix.append(parts[i]);
                    }
                    modifiedWord=parts[parts.length-1];
                }
            }
            String stemmed = stanfordStemmer.stem(modifiedWord);
            if(stemmed==null || stemmed.length()<2)
                return original;

            if(stemmed.endsWith("'")) //handles stemming w/ possessive, i.e. Pig's
                stemmed=stemmed.substring(0, stemmed.length()-1);

            if(isPhrase)
                return prefix.toString()+" " +stemmed;
            
            return stemmed;
        } catch (Throwable e) {
            logger.warn("Problems stemming: " + modifiedWord, e);
            return modifiedWord;
        }
    }
}
