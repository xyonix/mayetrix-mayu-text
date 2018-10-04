package com.xyonix.mayetrix.mayu.text.dtrees;

/**
 * Represents a location in a Stanford TypedDependency structure.
 */
public class TDCoordinate {

	private String typedDependencyTypeShortName = null;
	private TDRole role = null;
	
	public TDCoordinate(String n, TDRole r) {
		this.typedDependencyTypeShortName=n;
		this.role=r;
	}
	
	public enum TDRole {
		GOVERNOR, DEPENDENT;
	}
	
	public String getTDTypeShortName() {
		return typedDependencyTypeShortName;
	}

	public TDRole getRole() {
		return role;
	}
}
