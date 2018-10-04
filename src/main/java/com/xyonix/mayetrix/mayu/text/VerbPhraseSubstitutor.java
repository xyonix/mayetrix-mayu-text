package com.xyonix.mayetrix.mayu.text;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.io.JarReader;

public class VerbPhraseSubstitutor {

    private static VerbPhraseSubstitutor instance = null;
    private static Logger logger = LoggerFactory.getLogger(VerbPhraseSubstitutor.class);
    
    /**
     * Returns an instance. Presumes substitutions are in /data/nlp/verb-substitutions.txt.
     */
    public static VerbPhraseSubstitutor getInstance() {
        if(instance==null) {
            instance = new VerbPhraseSubstitutor();
        }
        return instance;
    }
    
    private VerbPhraseSubstitutor() {
    	final String file = "/data/nlp/verb-substitutions.txt";
        try {
            load(file);
        } catch (Exception e) {
        	final String message = "Problems loading verb substitutions from file: " + file;
            logger.warn(message, e);
            throw new MayuTextException(message, e);
        }
    }
    
    public String substitute(String sentence) {
        for(String complexVerb:complexToSimpleVerbMap.keySet()) {        	
        	if(sentence.toLowerCase().indexOf(complexVerb+" ")!=-1) { //Note: the whitespace at the end is to ensure only full word substitutions, i.e. 'significantly improve' will not end up 'significantly improvesd'
        		sentence = sentence.replaceAll("(?i)"+complexVerb, complexToSimpleVerbMap.get(complexVerb));
                break; //low chance of more than 1 verb subtitution, so we bail for speed.
            }
        }
        return sentence;
    }
    
    private Map<String, String> complexToSimpleVerbMap = new HashMap<String, String>();
    
    void load(String file) throws Exception {

        final BufferedReader inputStream = JarReader.getResourceAsReader(file);
        String line;
        while ((line = inputStream.readLine()) != null) {
            if (line.trim().length() > 0 && !line.startsWith("#")) {
                String[] parts = line.trim().split("\\|");
                complexToSimpleVerbMap.put(parts[0].trim().toLowerCase(), parts[1].trim().toLowerCase()); //TODO Make onto case sensitive lookups
            }
        }
        inputStream.close();
    }
}
