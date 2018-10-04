package com.xyonix.mayetrix.mayu.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xyonix.mayetrix.mayu.data.BasicEntity;
import com.xyonix.mayetrix.mayu.data.TaxPath;

/**
 * Entity found in text.
 */
public class FoundEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = null;
	private String displayName = null; 
	private int count = -1;
	private float confidence = -1f;

	protected Map<String, TaxPath> paths = new HashMap<String, TaxPath>();

	private Map<String, FoundEntity> resolvedAnaphorMap = new HashMap<String, FoundEntity>();

	public void setCount(int count) {
		this.count=count;
	}

	public int getCount() {
		return count;
	}

	public void setConfidence(float confidence) {
		this.confidence=confidence;
	}

	/**
	 * Returns a confidence value between 0 and 1 if set. Used for resolved anaphor confidence. Returns -1 if never set.
	 */
	public float getConfidence() {
		return confidence;
	}

	public String getDisplayNameWithAnaphora() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		if(getResolvedAnaphor()!=null && getResolvedAnaphor().size() > 0) {
			for (FoundEntity anaphora : getResolvedAnaphor())
			{
				sb.append(" [[");
				sb.append(anaphora.getDisplayName());
				sb.append("]]");
			}
		}
		return sb.toString();
	}

	public void addResolvedAnaphor(FoundEntity se) {
		if(resolvedAnaphorMap.containsKey(se.getName()))
			return;

		this.resolvedAnaphorMap.put(se.getName(), se);
	}

	public List<FoundEntity> getResolvedAnaphor() {
		return new ArrayList<FoundEntity>(this.resolvedAnaphorMap.values());	
	}

	/**
	 * Returns name of the entity. If found within text, this is the form found in the text.
	 */
	public String getName() {
		return this.name;
	}

	public FoundEntity(String name) {
		setName(name);
	}

	/**
	 * Returns the display name of the entity. If no display name (such as a preferred consumer term) was set, this will be the same as the name.
	 */
	public String getDisplayName() {
		if(displayName==null)
			return cleanCase(name);

		return displayName;
	}

	public void setDisplayName(String dn) {
		this.displayName=dn;
	}

	public void setName(String n) {
		this.name=n;
	}

	public void addPath(String path) {
		TaxPath t = new TaxPath(path);
		paths.put(t.getName().toLowerCase(), t);
	}

	public List<TaxPath> getPaths() {
		return new ArrayList<TaxPath>(this.paths.values());
	}

	public void resetPaths() {
		this.paths.clear();
	}

	public void setPaths(List<TaxPath> paths) {
		resetPaths();
		for(TaxPath tp:paths) {
			this.paths.put(tp.getName(), tp);
		}
	}

	public static String cleanCase(String name) {
		if(name!=null && name.length()>0) {
			if(name.length()==3) { //3 letter acronym presumption
				return name.toUpperCase();
			}
			StringBuilder sb = new StringBuilder();
			String[] parts = name.split("\\s+");
			for(String w:parts) {
				sb.append(w.substring(0,1).toUpperCase());
				sb.append(w.substring(1).trim());
				sb.append(" ");
			}
			return sb.toString().trim();
		}
		return name;
	}

	public void deepMerge(FoundEntity e) {
		if(e==null)
			return;

		mergePaths(e);
		setDisplayName(e.getDisplayName());
	}

	public void mergePaths(FoundEntity e) {
		for(TaxPath tp:e.getPaths()) {
			addPath(tp.getName());
		}
	}

	public FoundEntity clone() {
		FoundEntity e = new FoundEntity(getName());
		for(TaxPath tp:getPaths()) {
			e.addPath(tp.getName());
		}
		e.setDisplayName(getDisplayName());
		for(FoundEntity ant:getResolvedAnaphor()) {
			e.addResolvedAnaphor(ant.clone());
		}
		return e;
	}

	public boolean hasType(String type) {
		for(TaxPath p:paths.values()) {
			if(p.hasNode(type))
				return true;
		}
		return false;
	}

	public boolean hasUnblacklistedType(String type) {
		return hasType(type) && !isBlacklistedForType(type);
	}

	public boolean hasAnUnblacklistedType(List<String> types) {
		for(String t:types) {
			if(hasUnblacklistedType(t))
				return true;
		}
		return false;
	}

	public List<TaxPath> getPathsWithType(String type) {
		List<TaxPath> rpaths = new ArrayList<TaxPath>();
		for(TaxPath p:paths.values()) {
			if(p.hasNode(type))
				rpaths.add(p);
		}
		return rpaths;
	}

	public boolean hasPath(TaxPath path) {
		return paths.containsKey(path.getName().toLowerCase());
	}

	public boolean hasPathFragment(TaxPath fragment) { 	
		for(TaxPath p:paths.values()) {
			if(hasNodeInString(p.getName().toLowerCase(), fragment.getName().toLowerCase()))
				return true;
		}
		return false;
	}

	public boolean hasPathFragmentThatEndsWith(TaxPath fragment) {
		if(hasPathFragment(fragment)) { //needed for performance.
			for(TaxPath p:paths.values()) {
				if(p.getName().toLowerCase().endsWith(fragment.getName().toLowerCase()))
					return true;
			}
			return false;
		}
		return false;
	}

	private static boolean hasNodeInString(String path, String node) {
		if(path.contains(node)) {
			String r=path.replace(node, "");
			if(r.length()==0 || (r.startsWith("/") && path.startsWith(node)) || r.endsWith("/") || r.contains("//"))
				return true;
		}
		return false;
	}

	public boolean hasPathFragment(String fragment) {
		return hasPathFragment(new TaxPath(fragment));
	}

	public boolean hasPathFragmentThatEndsWith(String fragment) {
		return hasPathFragmentThatEndsWith(new TaxPath(fragment));
	}

	public static void sortByName( List<? extends FoundEntity> entities ) {
		EntityNameComparator comparator = EntityNameComparator.getInstance();
		Collections.sort(entities, comparator);
	}

	public static boolean hasType(List<FoundEntity> es, String type) {
		for(FoundEntity e:es) {
			if(e.hasType(type))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if somewhere in the list of entities, an entity exists containing the path fragment, i.e. an entity w/ path x/y/z exists matching the fragment y or y/z
	 */
	public static boolean hasPathFragment(List<FoundEntity> es, TaxPath fragment) {
		for(FoundEntity e:es) {
			if(e.hasPathFragment(fragment))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if somewhere in the list of entities, an entity exists containing the path fragment, i.e. an entity w/ path x/y/z exists matching the fragment y or y/z
	 * and the same entity does NOT contains a blacklisted path fragment of the leaf, i.e. the match does not contain z_blacklisted.
	 */
	public static boolean hasUnblacklistedPathFragment(List<FoundEntity> es, TaxPath fragment) {
		for(FoundEntity e:es) {
			if(e.hasPathFragment(fragment)) {
				String btype = fragment.getLeaf()+"_blacklist";
				if(!e.hasType(btype))
					return true;
			}
		}
		return false;
	}

	public String toReadableString() {
		StringBuilder sb = new StringBuilder();
		if(count!=-1)
			sb.append(count + " - ");

		sb.append(getName());
		sb.append(" ");

		if(getDisplayName()!=null) {
			sb.append("- ");
			sb.append(getDisplayName());
			sb.append(" ");
		}
		if(getPaths().size()>0) {
			sb.append("[");
			boolean first=true;
			for(TaxPath tp:getPaths()) {
				if(!first)
					sb.append(", ");
				sb.append(tp.getName());
				first=false;
			}
			sb.append("] ");
		}
		return sb.toString();
	}

	public String toString() {
		return toReadableString();
	}

	/**
	 * Returns true if entity contains a blacklist type, i.e. treatment_blacklist
	 * @param type I.e. treatment
	 */
	public boolean isBlacklistedForType(String type) {
		return hasType(type+"_blacklist");
	}

	public void appendChildPathToParent(String parentPath, String childPath) {
		if(!hasType(childPath)) { //Do only if not already there.
			boolean appended = false;
			for(TaxPath p:paths.values()) {
				if(p.getName().equals(parentPath)) {
					p.append(childPath);
					appended=true;
					break;
				}
			}
			if(!appended)
				addPath(parentPath+"/"+childPath); //if wasn't already there, just add the parent/child combo
		}
	}

	public String toOntologyForm() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append("\n");
		if(this.paths.size()>0) {
			sb.append("types:");
			boolean first=true;
			for(TaxPath tp:getPaths()) {
				if(!first)
					sb.append(", ");
				sb.append(tp.getName());
				first=false;
			}
		}
		return sb.toString();
	}

	public BasicEntity getAsSimpleEntity() {
		List<String> paths = new ArrayList<String>(getPaths().size());
		for(TaxPath tp:getPaths()) {
			paths.add(tp.getName());
		}

		return new BasicEntity(getName(), getDisplayName(), getCount(), paths);
	}

	private static void addTypeIfPresent(Set<String> types, FoundEntity te, String type) {
		if(te.hasType(type) && !types.contains(type) && !te.hasType(type+"_blacklist"))
			types.add(type);
	}
}

class EntityNameComparator implements Comparator<FoundEntity> {

	EntityNameComparator(){
		super();
	}

	public static EntityNameComparator getInstance(){
		return new EntityNameComparator();
	}

	public int compare( FoundEntity r1, FoundEntity r2 ) {
		return r1.getName().toLowerCase().compareTo(r2.getName().toLowerCase());
	}
}

