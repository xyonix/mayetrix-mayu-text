package com.xyonix.mayetrix.mayu.text.dtrees;

import java.util.List;

import edu.stanford.nlp.trees.TypedDependency;

public interface DependencyParser {

	public List<TypedDependency> parseSentence(String sentence);

}
