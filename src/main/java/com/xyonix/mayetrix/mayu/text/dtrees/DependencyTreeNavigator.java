package com.xyonix.mayetrix.mayu.text.dtrees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xyonix.mayetrix.mayu.text.PrepositionSet;
import com.xyonix.mayetrix.mayu.text.SentencePhraseSubstitutor;

import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class DependencyTreeNavigator {

	private Map<String, List<TypedDependency>> governorDependencyMap = new HashMap<String, List<TypedDependency>>();
	private Map<String, List<TypedDependency>> dependencyGovernorMap = new HashMap<String, List<TypedDependency>>();
	private Collection<TypedDependency> typedDependencies = null;

	/**
	 * Map from shortname 'prep' to full TypedDependency like 'prep(runs-3, in-2)'
	 */
	private Map<String, List<TypedDependency>> shortNameToTypedDependencyMap = new HashMap<String, List<TypedDependency>>();

	public DependencyTreeNavigator(List<TypedDependency> typedDependencies) {
		this.typedDependencies=typedDependencies;
		for(TypedDependency d:typedDependencies) {
			if(d.reln()!=null) {
				if(shortNameToTypedDependencyMap.containsKey(d.reln().getShortName())) {
					shortNameToTypedDependencyMap.get(d.reln().getShortName()).add(d);
				} else {
					List<TypedDependency> dependencies = new ArrayList<TypedDependency>();
					dependencies.add(d);
					shortNameToTypedDependencyMap.put(d.reln().getShortName(), dependencies);
				}

				String key = d.gov().nodeString();
				if(governorDependencyMap.containsKey(key))
					governorDependencyMap.get(key).add(d);
				else {
					List<TypedDependency> deps = new ArrayList<TypedDependency>();
					deps.add(d);
					governorDependencyMap.put(key, deps);
				}

				key = d.dep().nodeString();
				if(dependencyGovernorMap.containsKey(key))
					dependencyGovernorMap.get(key).add(d);
				else {
					List<TypedDependency> deps = new ArrayList<TypedDependency>();
					deps.add(d);
					dependencyGovernorMap.put(key, deps);
				}
			}
		}
	}

	public Collection<TypedDependency> getTypedDependencies() {
		return this.typedDependencies;
	}

	protected List<TypedDependency> getGovernorDependencies(TreeGraphNode tgn) {
		List<TypedDependency> tds = new ArrayList<TypedDependency>(); 
		if(tgn!=null && governorDependencyMap.containsKey(tgn.nodeString()))
			tds=governorDependencyMap.get(tgn.nodeString());
		return tds;
	}

	private List<TypedDependency> getDependencyDependencies(TreeGraphNode tgn) {
		List<TypedDependency> tds = new ArrayList<TypedDependency>(); 
		if(tgn!=null && dependencyGovernorMap.containsKey(tgn.nodeString()))
			tds=dependencyGovernorMap.get(tgn.nodeString());
		return tds;
	}

	/**
	 * Returns location within TypedDependency given a node value.
	 * 
	 * Given TDs like:
	 * 
	 * prep(assists-9, Consolidated-1)
	 * pcomp(Consolidated-1, with-2)
	 * amod(files-5, other-3)
	 * amod_dep(files-5, computational-4)
	 * 
	 * and nodeValue "computational" the returned result would be: amod_dep
	 * @param nodeValue A value like "computational"
	 * @return The location like: nsubj_gov
	 */
	public List<String> getDependencyType(String nodeValue, TypedDependencyRole role, boolean appendTDRole) {
		List<String> locs = new ArrayList<String>();
		for(TreeGraphNode tgn:getTreeGraphNodes(nodeValue)) {
			if(role==TypedDependencyRole.GOVERNOR || role==TypedDependencyRole.EITHER) {
				for(TypedDependency td:getGovernorDependencies(tgn)) {
					if(appendTDRole)
						locs.add(td.reln().getShortName()+"_gov");
					else
						locs.add(td.reln().getShortName());
				}
			}
			if(role==TypedDependencyRole.DEPENDENT || role==TypedDependencyRole.EITHER) {
				for(TypedDependency td:getDependencyDependencies(tgn)) {
					if(appendTDRole)
						locs.add(td.reln().getShortName()+"_dep");
					else
						locs.add(td.reln().getShortName());
				}
			}
		}
		return locs;
	}

	public enum TypedDependencyRole {
		DEPENDENT, GOVERNOR, EITHER;
	}

	/**
	 * Filters TreeGraphNodes that don't come from a TypedDependency in the specified blacklistedTypes list.
	 * 
	 * Example: if original contains Gopal-1 and unicorns-7, and bannedTypes contains "nsubj", then Gopal-1 will get 
	 * filtered out and only unicorns-7 will remain.
	 * 
	 * nsubj(like-6, Gopal-1)
	 * punct(Gopal-1, ,-2)
	 * conj(Gopal-1, Jasmine-3)
	 * cc(Gopal-1, and-4)
	 * conj(Gopal-1, Sarbjit-5)
	 * root(ROOT-0, like-6)
	 * dobj(like-6, unicorns-7)
	 */
	public List<TreeGraphNode> filterTypes(List<TreeGraphNode> original, String[] blacklistedTypes, TypedDependencyRole tdRole) {
		Set<String> blacklistTypes = new HashSet<String>(blacklistedTypes.length);
		for(String bt:blacklistedTypes) {
			blacklistTypes.add(bt);
		}

		Map<String, TreeGraphNode> filtered = new HashMap<String, TreeGraphNode>();
		for(TreeGraphNode tgn:original) {
			for(String mt:getDependencyType(tgn.nodeString(), tdRole, false)) {
				if(!blacklistTypes.contains(mt)) {
					filtered.put(tgn.nodeString(), tgn);
				}
			}
		}
		return new ArrayList<TreeGraphNode>(filtered.values());
	}

	/**
	 * Given 'good-4' and dependencies like
	 * 
	 * nsubj(good-4, Uma-1)
	 * cop(good-4, is-2)
	 * neg(good-4, not-3)
	 * 
	 * returns true if negated, false if not.
	 * 
	 * @param forwardToMatchingGov If True, will forward to a matching governor. So, as in the tree below, the tgn='good-7' will
	 * look for a negation on the governor 'dog-8'
	 * 
	 * neg(dog-8, not-4)
	 * amod(dog-8, good-7)
	 */
	public boolean isNegated(TreeGraphNode tgn, boolean forwardToMatchingGov) {
		for(TypedDependency td:getGovernorDependencies(tgn)) {
			if(td.reln().getShortName().equals("neg"))
				return true;
		}
		if(forwardToMatchingGov) {
			for(TreeGraphNode g:getMatchingGovernors(tgn)) {
				if(isNegated(g,false))
					return true;
			}
		}
		return false;
	}

    public boolean isNegated(TreeGraphNode action, TreeGraphNode target) {
        return (isNegated(target, true) || isNegated(action, false));
    }
    
	public List<TreeGraphNode> getMatchingDependents(TreeGraphNode tgn) {
		return getMatchingDependents(tgn, true);
	}

	/**
	 * Given 'seems-3' returns 'discovery-8' without requiring 'xcomp' to be specified. All matching dependents are returned.
	 * 
	 * nsubj(seems-3, TXT-2)
	 * aux(discovery-8, to-4)
	 * cop(discovery-8, be-5)
	 * det(discovery-8, a-6)
	 * amod(discovery-8, terrible-7)
	 * xcomp(seems-3, discovery-8)
	 * 
	 * @param includePunctuation If true, then punctuation matches are returned, else not.
	 */
	public List<TreeGraphNode> getMatchingDependents(TreeGraphNode tgn, boolean includePunctuation) {
		List<TreeGraphNode> nodes = new ArrayList<TreeGraphNode>();
		List<TypedDependency> deps = getGovernorDependencies(tgn);
		for(TypedDependency td:deps) {
			if(includePunctuation || !td.reln().getShortName().equals("punct")) {
				if(td.gov().label().value().equals(tgn.label().value())) {
					nodes.add(td.dep());
				}
			}
		}
		return nodes;
	}

	public List<TreeGraphNode> getMatchingDependents(String nodeType, TreeGraphNode tgn) {
		String[] nodeTypes = new String[1];
		nodeTypes[0]=nodeType;
		return getMatchingDependents(nodeTypes, tgn);
	}

	public List<TreeGraphNode> getMatchingDependents(String[] nodeTypes, TreeGraphNode tgn) {
		List<TreeGraphNode> matchingDependents = new ArrayList<TreeGraphNode>();
		List<TypedDependency> dependencies = getGovernorDependencies(tgn);
		for(TypedDependency td:dependencies) {
			for(String nodeType:nodeTypes) {
				if(td.reln().getShortName().equals(nodeType)) {
					matchingDependents.add(td.dep());
				}
			}
		}
		return matchingDependents;
	}

	/**
	 * Returns TD coord.
	 */
	public List<TreeGraphNode> getCoordinate(TDCoordinate coordinate) {
		List<TreeGraphNode> tds = new ArrayList<TreeGraphNode>();
		for(TypedDependency td:getMatchingTypedDependencies(coordinate.getTDTypeShortName())) {
			if(td!=null) {
				if(coordinate.getRole()==TDCoordinate.TDRole.GOVERNOR)
					tds.add(td.gov());
				else
					tds.add(td.dep());
			}
		}
		return tds;
	}
	
	/**
	 * Given 'seems-3' returns 'discovery-8' without requiring 'amod' to be specified. All matching dependents are returned.
	 * 
	 * nsubj(seems-3, TXT-2)
	 * aux(discovery-8, to-4)
	 * cop(discovery-8, be-5)
	 * det(discovery-8, a-6)
	 * amod(discovery-8, terrible-7)
	 * xcomp(seems-3, discovery-8)
	 * 
	 * @param includePunctuation If true, then punctuation matches are returned, else not.
	 */
	public List<TreeGraphNode> getMatchingGovernors(TreeGraphNode tgn) {
		List<TreeGraphNode> nodes = new ArrayList<TreeGraphNode>();
		List<TypedDependency> deps = getDependencyDependencies(tgn);
		for(TypedDependency td:deps) {
			if(td.dep().label().value().equals(tgn.label().value())) {
				nodes.add(td.gov());
			}
		}
		return nodes;
	}


	/**
	 * Returns all TypedDependency objects for all matching types.
	 * @param nodeTypes List of types like: nsubj, nsubjpass, aux, etc.
	 */
	public List<TypedDependency> getMatchingTypedDependencies(String[] nodeTypes) {
		List<TypedDependency> tds = new ArrayList<TypedDependency>();
		for(String nt:nodeTypes) {
			for(TypedDependency td:getMatchingTypedDependencies(nt)) {
				if(td!=null)
					tds.add(td);
			}
		}
		return tds;
	}

	/**
	 * Returns the TreeGraphNode corresponding to 'discovery-8' given 'discovery'. Checks both governors and 
	 * dependents. Returns null if no match.
	 */
	List<TreeGraphNode> getTreeGraphNodes(String name) {
		List<TreeGraphNode> nodes = new ArrayList<TreeGraphNode>();
		if(name.contains(" ")) {
			name=SentencePhraseSubstitutor.generateSubstitute(name);
		}
		name=name.toLowerCase();
		for(TypedDependency td:getTypedDependencies()) {
			if(td.gov().nodeString().toLowerCase().startsWith(name))
				nodes.add(td.gov());
			if(td.dep().nodeString().toLowerCase().startsWith(name))
				nodes.add(td.dep());
		}
		return nodes;
	}

	/**
	 * Given an action and pobj node, return the prep node, and return verb-prep
	 */
	public String getActionPrep(TreeGraphNode action, TreeGraphNode pobjNode) {
		List<TypedDependency> dependencies = getGovernorDependencies(action);
		for(TypedDependency td:dependencies) {
				if(td.gov().label().value().equals(action.label().value())) {
					if(PrepositionSet.getInstance().contains(td.dep().label().value())) {
						for(TreeGraphNode pm:getMatchingDependents(td.dep(), false)) {
							if (pm.label().value().equals(pobjNode.label().value())) {
								return (action.label().value() + "-" + td.dep().label().value());
						}
					} 
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the dependency 'prep(SUB-negative_impact-2, of-3)' from a TypedDependency list like:
	 * 
	 * det(SUB-negative_impact-2, The-1)
	 * nsubj(terrible-6, SUB-negative_impact-2)
	 * prep(SUB-negative_impact-2, of-3)
	 * pobj(of-3, Madonna-4)
	 * cop(terrible-6, is-5)
	 * punct(terrible-6, .-7)
	 * 
	 * @param nodeType Like 'prep' or 'nsubj'.
	 */
	public List<TypedDependency> getMatchingTypedDependencies(String nodeType) {
		List<TypedDependency> dependencies = shortNameToTypedDependencyMap.get(nodeType);
		if(dependencies==null)
			return new ArrayList<TypedDependency>();
		return dependencies;
	}

	public List<TreeGraphNode> getMatchingDependentsForwardOnPrepositions(TreeGraphNode tgn) {
		return getMatchingDependentsForwardOnPrep(tgn, true);
	}

	/**
	 * Same as getMatchingDependents but if target is a prep (like: of, with, ...), another lookup is performed on the preposition. So
	 * 
	 * Given 'consolidates-11' returns 'pride-16' not 'with-15' as is the case for getMatchingDependents
	 * 
	 * prep(consolidates-11, with-15)
	 * pobj(with-15, pride-16)
	 */
	public List<TreeGraphNode> getMatchingDependentsForwardOnPrep(TreeGraphNode tgn, boolean includePunct) {
		List<TreeGraphNode> nodes = new ArrayList<TreeGraphNode>();
		List<TypedDependency> dependencies = getGovernorDependencies(tgn);
		for(TypedDependency td:dependencies) {
			if(includePunct || !td.reln().getShortName().equals("punct")) {
				if(td.gov().label().value().equals(tgn.label().value())) {
					if(PrepositionSet.getInstance().contains(td.dep().label().value())) {
						for(TreeGraphNode pm:getMatchingDependents(td.dep(), includePunct)) {
							nodes.add(pm);
						}
					} else {
						nodes.add(td.dep());
					}
				}
			}
		}
		return nodes;
	}
	
	/**
	 * Expands conjunctions from a list of nodes.
	 */
	public List<TreeGraphNode> expandConjunctions(List<TreeGraphNode> originals) {
		Map<String, TreeGraphNode> conjunctions = new HashMap<String, TreeGraphNode>();
		for(TreeGraphNode t:originals) {
			for(TreeGraphNode tc:expandConjunctions(t)) {
				conjunctions.put(tc.nodeString(), tc);
			}
		}
		return new ArrayList<TreeGraphNode>(conjunctions.values());
	}
	
	/**
	 * Given an action, return the advmod node if there is one, and return verb-advmod
	 */
	public String getActionAdvMod(TreeGraphNode action) {
		List<TypedDependency> dependencies = getGovernorDependencies(action);
		for(TypedDependency td:dependencies) {
			if(td.gov().index() == action.index()) {
				if (td.reln() != null && td.reln().getShortName().equals("advmod")) {
					return (action.label().value() + "-" + td.dep().label().value());
				} 
			}
		}
		return null;
	}

	/**
	 * Returns all conjunctions for this tree given a node. 
	 * 
	 * Given 'crows-3'
	 * 
	 * in
	 * 
	 * nsubj(likes-2, Jasmine-1)
	 * root(ROOT-0, likes-2)
	 * dobj(likes-2, crows-3)
	 * punct(crows-3, ,-4)
	 * conj(crows-3, goats-5)
	 * cc(crows-3, and-6)
	 * conj(crows-3, dogs-7)
	 * punct(likes-2, .-8)
	 * nsubj(likes-2, Jasmine-1)
	 * 
	 * returns
	 * 
	 * crows-3, goats-5, dogs-7
	 * 
	 */
	private List<TreeGraphNode> expandConjunctions(TreeGraphNode original) {
		List<TreeGraphNode> conjunctions = new ArrayList<TreeGraphNode>();
		conjunctions.add(original);
		for(TreeGraphNode n:getMatchingDependents( "conj", original)) {
			conjunctions.add(n);
		}
		return conjunctions;
	}
}
