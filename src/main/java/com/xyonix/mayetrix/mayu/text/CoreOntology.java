package com.xyonix.mayetrix.mayu.text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Base ontologies derive from
 */
public abstract class CoreOntology {

    private boolean stemWordsOnSearch=true;
    
    public abstract FoundEntity search(String phrase);
    
    public void setStemWordsOnSearch(boolean stemWords) {
        this.stemWordsOnSearch=stemWords;
    }
    
    public boolean getStemWordsOnSearch() {
        return this.stemWordsOnSearch;
    }
    
	public static void save(String fileName, List<FoundEntity> entities) throws FileNotFoundException {
	    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
	    for (FoundEntity fe : entities) {
	        pw.println(fe.toOntologyForm());
	    	pw.print("\n");
	    }
	    pw.close();
	}
    
}
