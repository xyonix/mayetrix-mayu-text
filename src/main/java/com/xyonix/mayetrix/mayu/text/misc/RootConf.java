package com.xyonix.mayetrix.mayu.text.misc;

import java.io.File;

import com.xyonix.mayetrix.mayu.text.BasicTagger;

public abstract class RootConf {
		
	/**
	 * Change to proj specific entity tagger when needed.
	 */
	public static final BasicTagger ENTITY_TAGGER = BasicTagger.getInstance();
	
    public static int MAX_SENTENCE_LENGTH = 280; //280

	public static final int STEMMER_CACHE_SIZE = 15000;
    private static String DATA_DIRECTORY = "src/main/resources/data";
    private static String TEMP_DATA_DIRECTORY = DATA_DIRECTORY +"/temp";
    
    public static File getTempDataDirectory() {
        File d = new File(TEMP_DATA_DIRECTORY);
        if(!d.exists())
            d.mkdir();
        return d;
    }
    
    public static String HOME_DIRECTORY = ".";
   
}
