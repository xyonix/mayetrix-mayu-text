package com.xyonix.mayetrix.mayu.text.dtrees;

import com.xyonix.mayetrix.mayu.text.MaltDependencyParser;
import com.xyonix.mayetrix.mayu.text.StanfordDependencyParser;

public class DependencyParserFactory {

	public enum ParserType {
		STANFORD_PARSER, MALT_PARSER;
	}

	public static DependencyParser generate(ParserType type) {
		if(type==ParserType.MALT_PARSER)
			return MaltDependencyParser.getInstance();

		return StanfordDependencyParser.getInstance();
	}
}
