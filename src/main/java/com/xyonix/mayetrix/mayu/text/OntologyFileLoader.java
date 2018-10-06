package com.xyonix.mayetrix.mayu.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.io.JarReader;
import com.xyonix.mayetrix.mayu.wordnet.BasicWordNet;

public class OntologyFileLoader {

	private static Logger logger = LoggerFactory.getLogger(OntologyFileLoader.class);

	public static List<FoundEntity> load(String[] ontoFiles, boolean applyStemming) throws IOException {

		List<FoundEntity> rawEntities = new ArrayList<FoundEntity>();

		Map<String, List<FoundEntity>> typeToEntitiesMap = new HashMap<String, List<FoundEntity>>();

		Set<String> entityTypesInNames = new HashSet<String>();
		for(FoundEntity t:loadWithNoReplacements(ontoFiles, applyStemming)) {
			rawEntities.add(t);
			for(String gram:t.getName().split("\\s+")) {
				if(gram.startsWith("[")) {
					if(gram.length()>0 && !entityTypesInNames.contains(gram))
						entityTypesInNames.add(gram);
				}
			}
		}

		for(String t:entityTypesInNames) {
			typeToEntitiesMap.put(t, new ArrayList<FoundEntity>());
		}

		/**
		 * Add found entities for nodes in entity names
		 */
		for(FoundEntity foundEntity:rawEntities) {
			
			for(String t:entityTypesInNames) {
				/**
				 * Separates terms from brackets, i.e. dog from [dog]
				 */
				if(foundEntity.hasPathFragmentThatEndsWith((t.substring(1, t.length()-1)))) { 
					if(StringUtils.countMatches(foundEntity.getName(), "[")<2) {
						typeToEntitiesMap.get(t).add(foundEntity);
						//System.out.println("DEBUG Adding FE: " + foundEntity.toReadableString() + " for " + t);
					}
				}
			}
		}

		Map<String, FoundEntity> expandedEntities = new HashMap<String, FoundEntity>();
		for(FoundEntity te:rawEntities) {

			/**
			 * Expands names and typed names into word bins. For example [he_is] sometimes [feeling] has bins: 1, 2, 3 w/ he_is and instances
			 * of feeling in bins 1 and 3. 
			 */
			List<List<FoundEntity>> bins = new ArrayList<List<FoundEntity>>();
			for(NamedWordToken n:NamedWordToken.parseRawName(te)) {
				List<FoundEntity> perms = new ArrayList<FoundEntity>();
				perms.addAll((n.getNamePermutations(typeToEntitiesMap)));
				bins.add(perms);
			}

			/**
			 * Simple case no permutations are required.
			 */
			if(!te.getName().contains("[")) { 
				FoundEntity foundEntity = new FoundEntity("");
				for(List<FoundEntity> bin:bins) {
					for(FoundEntity t:bin) {
						foundEntity.setName(foundEntity.getName()+" "+t.getName());
						foundEntity.mergePaths(t);
						foundEntity.mergeMetadata(t);
					}
				}
				foundEntity.setName(foundEntity.getName().trim().toLowerCase());

				addExpandedEntity(expandedEntities, foundEntity);
			/**
			 * More challenging scenario w/ multiple permutations.
			 */
			} else {
				List<FoundEntity> perm = new ArrayList<FoundEntity>();
				perm.add(new FoundEntity(""));
				for(List<FoundEntity> bin:bins) {
					List<FoundEntity> tempPerm = new ArrayList<FoundEntity>();
					for(FoundEntity fE:bin) {
						for (FoundEntity fT:perm) {
							FoundEntity tempE = new FoundEntity(fT.getName()+" "+fE.getName());
							tempE.mergePaths(fE);
							tempE.mergeMetadata(fE);
							tempE.mergePaths(fT);
							tempE.mergeMetadata(fT);
							tempPerm.add(tempE);
						}
					}

					perm=tempPerm;
				}
				for(FoundEntity t:perm) {
					t.setName(t.getName().trim().toLowerCase());
					t.mergePaths(te);
					addExpandedEntity(expandedEntities, t);
				}
			}
		}
		return new ArrayList<FoundEntity>(expandedEntities.values());
	}

	private static void addExpandedEntity(Map<String, FoundEntity> expandedEntities, FoundEntity foundEntity) {
		if(expandedEntities.containsKey(foundEntity.getName())) {
			foundEntity.mergePaths(expandedEntities.get(foundEntity.getName()));
			foundEntity.mergeMetadata(expandedEntities.get(foundEntity.getName()));
		}
		expandedEntities.put(foundEntity.getName(), foundEntity);
	}
	
	private static List<FoundEntity> loadWithNoReplacements(String[] files, boolean applyStemming) throws IOException {

		Map<String, FoundEntity> entities = new HashMap<String, FoundEntity>();
		for(String f:files) {
			load(f, entities, applyStemming);
		}

		return new ArrayList<FoundEntity>(entities.values());
	}

	private static void load(String fileName, Map<String, FoundEntity> localEntityMap, boolean stemLookups) throws IOException {
		final String filePath = fileName;
		logger.info("Loading ontology file: " + filePath);

		final BufferedReader inputStream = JarReader.getResourceAsReader(filePath);
		String line;
		FoundEntity foundEntity = null;
		while ((line = inputStream.readLine()) != null) {
			if (line.trim().length() > 0 && !line.startsWith("#")) {
				if(!line.trim().startsWith("types:") && !line.trim().startsWith("metadata:") ) {
					foundEntity = new FoundEntity(line.trim());
				} else if (line.trim().startsWith("metadata:")) {
					String[] parts = line.substring(9).trim().split(", ");
					for(String p:parts) {
						String[] kv = p.trim().split(":");
						foundEntity.addMetadata(kv[0], kv[1]);
					}
				} else {
					String[] parts = line.substring(6).trim().split(", ");
					for(String p:parts) {
						foundEntity.addPath(p);
					}

					if(localEntityMap.containsKey(foundEntity.getName().toLowerCase())) {
						logger.info("Dup found, merging taxpaths for: " + foundEntity.getName());
						FoundEntity originalFoundEntity = localEntityMap.get(foundEntity.getName().toLowerCase());
						logger.info("Orig="+originalFoundEntity.toReadableString());
						logger.info("New="+foundEntity.toReadableString());
						originalFoundEntity.mergePaths(foundEntity);
						originalFoundEntity.mergeMetadata(foundEntity);
						logger.info("Merged="+originalFoundEntity.toReadableString());
						foundEntity=originalFoundEntity;
					}
					localEntityMap.put(foundEntity.getName().toLowerCase(), foundEntity);
					if(stemLookups) {
						String stemmedName = CachingStemmer.getInstance().stem(foundEntity.getName()).toLowerCase();
						if(stemmedName!=null  && stemmedName.length()>3 && !stemmedName.equalsIgnoreCase(foundEntity.getName()) && !BasicWordNet.hasVerb(stemmedName) && !localEntityMap.containsKey(stemmedName)) {
							FoundEntity clonedEntity = foundEntity.clone();
							clonedEntity.setName(stemmedName);
							logger.info("Adding from stem: " + clonedEntity.toReadableString());
							localEntityMap.put(stemmedName, clonedEntity);
						}
					}
				}
			}
		}
		inputStream.close();
	}
}

class NamedWordToken {

	String rawName;
	boolean isType=false;
	FoundEntity sourceFoundEntity;

	/**
	 * Parses raw entity names.
	 * @param raw FoundEntity w/ name like '[he_is] smiling'
	 */
	static List<NamedWordToken> parseRawName(FoundEntity raw) {
		List<NamedWordToken> tokens = new ArrayList<NamedWordToken>();

		for(String token:raw.getName().split("\\s+")) {
			NamedWordToken n = new NamedWordToken();
			n.rawName=token;
			n.sourceFoundEntity=raw;
			if(token.startsWith("[")) {
				n.isType=true;
			}
			tokens.add(n);
		}
		return tokens;
	}

	List<FoundEntity> getNamePermutations(Map<String, List<FoundEntity>> typeToEntitiesMap) {
		List<FoundEntity> permutations = new ArrayList<FoundEntity>();
		if(isType) {
			permutations.addAll(typeToEntitiesMap.get(rawName));
		} else {
			FoundEntity te = new FoundEntity(rawName);
			te.mergePaths(sourceFoundEntity);
			te.mergeMetadata(sourceFoundEntity);
			permutations.add(te);
		}
		return permutations;
	}
}
