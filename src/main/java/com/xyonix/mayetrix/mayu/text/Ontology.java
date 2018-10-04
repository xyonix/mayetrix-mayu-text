package com.xyonix.mayetrix.mayu.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.io.JarReader;

public class Ontology extends CoreOntology {

    private static Logger logger = LoggerFactory.getLogger(Ontology.class);
    private static Ontology instance = null;
    private Map<String, FoundEntity> entityMap = new HashMap<String, FoundEntity>();

    public static Ontology getInstance() {
        if(instance==null) {
            instance=new Ontology();
        }
        return instance;
    }
    
    public void printEntities() {
        for(String e:entityMap.keySet()) {
            System.out.println(e);
        }
    }
    
    public boolean hasEntities() {
        return entityMap.size()>0;
    }

    public void clearEntities() {
        this.entityMap.clear();
    }
    
    void addEntity(FoundEntity te) {
    	entityMap.put(te.getName(), te);
    }
    
    public void addEntitiesFromFile(String[] files) {
        try {
            for(FoundEntity te:OntologyFileLoader.load(files, getStemWordsOnSearch())) {
                entityMap.put(te.getName(), te);
            }
        } catch (Exception e) {
            final String message = "Error loading files: " + files;
            logger.error(message, e);
            throw new MayuTextException(message, e);
        }
    	for(String f:files) {
    		logger.info("Loaded entities from ontology files: " + f);
    	}
    }

    protected Ontology() { }

    /**
     * Searches for entity names in ontology. If an exact match exists, it is returned. If stemming is specified, 
     * the phrase is stemmed and a search on the stemmed form is made. Case is dropped in all cases.
     */
    public FoundEntity search(String term) {
        if(term==null)
            throw new IllegalArgumentException();

        String key = term.toLowerCase().trim();
        if(entityMap.containsKey(key))
            return entityMap.get(key);
        
        if(getStemWordsOnSearch()) {
            key = CachingStemmer.getInstance().stem(key);
            return entityMap.get(key);
        }
        return null;
    }
    
    /**
     * Returns all entities in the ontology.
     */
    public List<FoundEntity> getAllEntities() {
        return new ArrayList<FoundEntity>(entityMap.values());
    }
    
    /**
     * Returns all entities containing the specified taxpath node.
     */
    public List<FoundEntity> getAllEntitiesWithType(String node) {
    	List<FoundEntity> all = new ArrayList<FoundEntity>();
    	for(FoundEntity te:entityMap.values()) {
    		if(te.hasType(node))
    			all.add(te);
    	}
    	return all;
    }
    
    public String[] getOntologyFiles(String pathToFiles) throws IOException {
    	return JarReader.loadFileNamesWithExtensionInJarResourcePath(pathToFiles, "onto");
    }
}
