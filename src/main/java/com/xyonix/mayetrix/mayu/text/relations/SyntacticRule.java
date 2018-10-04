package com.xyonix.mayetrix.mayu.text.relations;

import java.util.HashSet;
import java.util.Set;

import com.xyonix.mayetrix.mayu.misc.CollectionUtil;
import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate;

public class SyntacticRule {
	
	private TDCoordinate.TDRole commonLink;
	private TDCoordinate source;
	private TDCoordinate action;
	private TDCoordinate target;
	private String stringRepresentation = null;
	
	/**
	 * Returns syntactic rule from String form.
	 * 
	 * nsubj_dep>nsubj_gov>dobj_dep + a common link. The link states that which roles of the type must match, i.e. gov in the rule above.
	 * 
	 * Example:
	 * 
	 * Joseph loves water.
	 * 
	 * nsubj(loves-2,JosephI-1)
	 * root(ROOT-0, loves-2)
	 * dobj(loves-2, water-3)
	 * punct(loves-2, .-4)
	 * 
	 * the common link would be gov since nsubj_gov and dobj_gov must both be loves-2 and not another value.
	 */
	public static SyntacticRule fromString(String rule, TDCoordinate.TDRole link) {
		String[] parts = rule.split(">");
		return new SyntacticRule(parseCoordinate(parts[0]), parseCoordinate(parts[1]), parseCoordinate(parts[2]), rule, link);
	}
	
	private static TDCoordinate parseCoordinate(String coordinate) {
		String[] c = coordinate.split("_");
		TDCoordinate.TDRole r = TDCoordinate.TDRole.GOVERNOR;
		if(c[1].trim().equalsIgnoreCase("dep"))
			r=TDCoordinate.TDRole.DEPENDENT;
		return new TDCoordinate(c[0].trim(), r);
	}
	
	public TDCoordinate.TDRole getCommonLink() {
		return commonLink;
	}
	
	private SyntacticRule(TDCoordinate source, TDCoordinate action, TDCoordinate target, String form, TDCoordinate.TDRole link) {
		this.source=source;
		this.action=action;
		this.target=target;
		this.stringRepresentation=form;
	}
	
	public String[] getTDShortNames() {
		Set<String> sns = new HashSet<String>();
		addShortName(sns, source);
		addShortName(sns, action);
		addShortName(sns, target);
		return CollectionUtil.convert(sns);
	}
	
	public TDCoordinate getSource() {
		return source;
	}

	public TDCoordinate getAction() {
		return action;
	}

	public TDCoordinate getTarget() {
		return target;
	}
	
	public String toString() {
		return this.stringRepresentation;
	}
	
	private void addShortName(Set<String> sns, TDCoordinate c) {
		String sn=c.getTDTypeShortName();
		if(!sns.contains(sn))
			sns.add(sn);
	}
}