package com.xyonix.mayetrix.mayu.text.relations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xyonix.mayetrix.mayu.text.dtrees.DependencyTreeNavigator;
import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate.TDRole;

import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class RelationGenerator {

	private static RelationGenerator instance = null;

	public static RelationGenerator getInstance() {
		if (instance == null) {
			instance = new RelationGenerator();
		}
		return instance;
	}

	private RelationGenerator() {
	}

	/**
	 * Generates a list of relations from a sentence deep parse.
	 */
	public List<Relation> generate(List<TypedDependency> dependencies) {
		List<Relation> rels = new ArrayList<Relation>();
		applyRules(dependencies, rels);
		
		//De-dupe - Note: Perform last stage de-duping in case we want to use rule redundant conclusions for confidence scoring.
		Map<String, Relation> deduped = new HashMap<String, Relation>();
		for(Relation r:rels) {
			deduped.put(r.toString(), r);
		}
		return new ArrayList<Relation>(deduped.values());
	}

	private void applyRules(List<TypedDependency> dependencies, List<Relation> rels) {
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>nsubj_gov>dobj_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("xsubj_dep>xsubj_gov>dobj_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>auxpass_dep>auxpass_gov", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubjpass_dep>auxpass_dep>auxpass_gov", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>nsubj_gov>advmod_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>nsubj_gov>xcomp_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("amod_dep>amod_gov>dep_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>cop_dep>cop_gov", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>dobj_gov>dobj_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("root_dep>pobj_gov>pobj_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>prep_gov>prep_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>nsubj_gov>punct_dep", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>aux_dep>aux_gov", TDRole.GOVERNOR), dependencies, rels);
		applyGrammaticalRule(SyntacticRule.fromString("nsubj_dep>nsubj_gov>acomp_dep", TDRole.GOVERNOR), dependencies, rels);
	}

	/**
	 * Maps tree like:
	 * 
	 * nsubj(eat-2, I-1)
	 * root(ROOT-0, eat-2)
	 * dobj(eat-2, pie-3)
	 * punct(eat-2, .-4)
	 * 
	 * to svo: i>eat>pie
	 * 
	 * for input: nsubj_dep>nsubj_gov>dobj_dep
	 */
	private void applyGrammaticalRule(SyntacticRule rule, List<TypedDependency> dependencies, List<Relation> relations) {
		DependencyTreeNavigator fullTreeNavigator = new DependencyTreeNavigator(dependencies);
		for(List<TypedDependency> filteredDeps:filterByCommonLink(dependencies, rule)) {
			DependencyTreeNavigator partialTreeNavigator = new DependencyTreeNavigator(filteredDeps);
			List<TreeGraphNode> sources = partialTreeNavigator.getCoordinate(rule.getSource());
			if(sources.size()>=0) { //Proceed only if a source was found matching the rule
				List<TreeGraphNode> action = partialTreeNavigator.getCoordinate(rule.getAction());
				if(action.size()>0) { //Proceed only if an action was found matching the rule
					List<TreeGraphNode> targets = partialTreeNavigator.getCoordinate(rule.getTarget());
					if(targets.size()>0) { //Proceed only if a target was found matching the rule
						List<TreeGraphNode> sourcesWConjs = fullTreeNavigator.expandConjunctions(sources);
						List<TreeGraphNode> targetsWConjs = fullTreeNavigator.expandConjunctions(targets);							
						addTriples(fullTreeNavigator, sourcesWConjs, action.get(0), targetsWConjs, "applyGrammaticalRule-"+rule.toString(), relations);
						
						List<TreeGraphNode> targetsPrepObjs = fullTreeNavigator.getMatchingDependentsForwardOnPrep(action.get(0), false);
						addPrepTriples(fullTreeNavigator, sources, action.get(0), targetsPrepObjs, "applyGrammaticalRule-"+rule.toString(), relations);
						
						addAdvModTriples(fullTreeNavigator, sourcesWConjs, action.get(0), targetsWConjs, "applyGrammaticalRule-"+rule.toString(), relations);
					}
				}
			}
		}
	}

	/**
	 * Filters by the common link i.e. gov across the nsubj/dobj pair so the pairs are aligned.
	 */
	List<List<TypedDependency>> filterByCommonLink(List<TypedDependency> dependencies, SyntacticRule rule) {
		Map<String, List<TypedDependency>> trees = new HashMap<String, List<TypedDependency>>();
		DependencyTreeNavigator tdn = new DependencyTreeNavigator(dependencies);
		for(TypedDependency td:tdn.getMatchingTypedDependencies(rule.getTDShortNames())) {
			String key = td.gov().nodeString();
			if(rule.getCommonLink()==TDRole.DEPENDENT)
				key=td.dep().nodeString();
			
			if(!trees.containsKey(key))
				trees.put(key, new ArrayList<TypedDependency>());
			
			trees.get(key).add(td);
		}		
		return new ArrayList<List<TypedDependency>>(trees.values());
	}

	private void addTriples(DependencyTreeNavigator tdn, List<TreeGraphNode> sources, TreeGraphNode action, List<TreeGraphNode> targets, String responsibleRule, List<Relation> relations) {
		for(TreeGraphNode source:sources) {
			for(TreeGraphNode target:targets) {
				if(source.label().value().equalsIgnoreCase(target.label().value()))
					continue; //block reflexives

				RelationEntity subject = new RelationEntity(source.value()); //TODO need to set the ontology entities, and look for prefix and suffix entities.
				RelationVerb verb = new RelationVerb(action.value(), tdn.isNegated(action, target));
				RelationEntity object = new RelationEntity(target.value());
				RelationEntity prep = null;

				relations.add(new Relation(subject, verb, object, prep));
			}
		}
		
		if (sources.size() == 0 && targets.size() > 0) {
			for(TreeGraphNode target:targets) {
				RelationEntity subject = new RelationEntity("nsubj"); //TODO need to set the ontology entities, and look for prefix and suffix entities.
				RelationVerb verb = new RelationVerb(action.value(), tdn.isNegated(action, target));
				RelationEntity object = new RelationEntity(target.value());
				RelationEntity prep = null;

				relations.add(new Relation(subject, verb, object, prep));
			}
		}
	}
	
	private void addPrepTriples(DependencyTreeNavigator tdn, List<TreeGraphNode> sources, TreeGraphNode action, List<TreeGraphNode> targets, String responsibleRule, List<Relation> relations) {
		for(TreeGraphNode source:sources) {
			for(TreeGraphNode target:targets) {
				if(source.label().value().equalsIgnoreCase(target.label().value()))
					continue; //block reflexives
				
				// create an artificial verb as verb_prep
				String actionPrep = tdn.getActionPrep(action, target);
				if (actionPrep == null) continue;

				RelationEntity subject = new RelationEntity(source.value());
				RelationVerb verb = new RelationVerb(actionPrep, tdn.isNegated(action, target));
				RelationEntity object = new RelationEntity(target.value());
				RelationEntity prep = null;

				relations.add(new Relation(subject, verb, object, prep));
			}
		}
	}
	
	private void addAdvModTriples(DependencyTreeNavigator tdn, List<TreeGraphNode> sources, TreeGraphNode action, List<TreeGraphNode> targets, String responsibleRule, List<Relation> relations) {
		for(TreeGraphNode source:sources) {
			for(TreeGraphNode target:targets) {
				if(source.label().value().equalsIgnoreCase(target.label().value()))
					continue; //block reflexives
				
				// create an artificial verb as verb_prep
				String actionMod = tdn.getActionAdvMod(action);
				if (actionMod == null) continue;

				RelationEntity subject = new RelationEntity(source.value());
				RelationVerb verb = new RelationVerb(actionMod, tdn.isNegated(action, target));
				RelationEntity object = new RelationEntity(target.value());
				RelationEntity prep = null;

				relations.add(new Relation(subject, verb, object, prep));
			}
		}
	}
}
