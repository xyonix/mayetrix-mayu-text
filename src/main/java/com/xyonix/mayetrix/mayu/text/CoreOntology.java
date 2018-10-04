package com.xyonix.mayetrix.mayu.text;

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
    
}
