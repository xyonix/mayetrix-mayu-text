package com.xyonix.mayetrix.mayu.text.relations;

import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate;
import com.xyonix.mayetrix.mayu.text.dtrees.TDCoordinate.TDRole;
import com.xyonix.mayetrix.mayu.text.relations.SyntacticRule;

import junit.framework.TestCase;

public class TestSyntacticRule extends TestCase {
	
	public void testFromString() {
		SyntacticRule lr = SyntacticRule.fromString("nsubj_dep>nsubj_gov>dobj_dep", TDRole.GOVERNOR);
		assertTrue(lr.getSource().getTDTypeShortName().equalsIgnoreCase("nsubj"));
		assertTrue(lr.getAction().getRole()==TDCoordinate.TDRole.GOVERNOR);
	}
	
	public void testGetTDShortNames() {
		SyntacticRule lr = SyntacticRule.fromString("nsubj_dep>nsubj_gov>dobj_dep", TDRole.GOVERNOR);
		for(String s:lr.getTDShortNames()) {
			System.out.println(s);
		}
		assertTrue(lr.getTDShortNames().length==2);
	}
}
