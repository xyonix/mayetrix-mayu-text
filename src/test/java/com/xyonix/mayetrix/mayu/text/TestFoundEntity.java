package com.xyonix.mayetrix.mayu.text;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.xyonix.mayetrix.mayu.data.BasicEntity;
import com.xyonix.mayetrix.mayu.data.TaxPath;
import com.xyonix.mayetrix.mayu.text.FoundEntity;
import com.xyonix.mayetrix.mayu.text.Ontology;

public class TestFoundEntity extends TestCase {
	
	public void testEndsWithPath() {
		FoundEntity te = new FoundEntity("banana");
		te.addPath("stuff/cloud/BAg");
		System.out.println(te.toString());
		assertTrue(te.hasPathFragmentThatEndsWith("BAg"));
		assertTrue(te.hasPathFragmentThatEndsWith("cloud/BAg"));
		assertTrue(!te.hasPathFragmentThatEndsWith("SHAt"));
		assertTrue(!te.hasPathFragmentThatEndsWith("veggies/BAg"));
	}
	
	public void testPathCase() {
		FoundEntity te = new FoundEntity("rock");
		te.addPath("/cloud/BAg");
		assertTrue(te.toString().trim().equals("rock - Rock [/cloud/BAg]"));
	}
	
	public void testCleanCase() {
		assertTrue(FoundEntity.cleanCase("lo").equals("Lo"));
		assertTrue(FoundEntity.cleanCase("fbi").equals("FBI"));
		assertTrue(FoundEntity.cleanCase("dog pound").equals("Dog Pound"));
		assertTrue(FoundEntity.cleanCase("dog  pound").equals("Dog Pound"));
	}
	
	public void testGetAsBasicEntity() {
		FoundEntity te = new FoundEntity("pigs");
		te.addPath("entity/oinkers");
		te.setDisplayName("piggies");
		BasicEntity se = te.getAsSimpleEntity();
		se.getTextName().equals("pigs");
		se.getDisplayName().equals("piggies");
		se.getPaths().contains("oinkers");
		se.getPaths().contains("entity/oinkers");
	}
	
	public void testDisplayNamePerms() {
		FoundEntity fe2 = new FoundEntity("Piggies");
		assertTrue(fe2.getName().equals("Piggies"));
		assertTrue(fe2.getDisplayName().equals("Piggies"));
		fe2.setName("Froggies");
		assertTrue(fe2.getName().equals("Froggies"));
		assertTrue(fe2.getDisplayName().equals("Froggies"));
		fe2.setDisplayName("Chickies");
		assertTrue(fe2.getDisplayName().equals("Chickies"));
		FoundEntity fe1 = new FoundEntity("lambies");
		assertTrue(fe1.getDisplayName().equals("Lambies"));
		fe1.setDisplayName("LAMBies");
		assertTrue(fe1.getDisplayName().equals("LAMBies"));
	}

	public void testHasUnblacklistedPathFragment() {
        Ontology.getInstance().addEntitiesFromFile(new String[]{
                "/data/ontology/ot-8.onto"
        });	
        FoundEntity te = Ontology.getInstance().search("dog");
        FoundEntity te2 = new FoundEntity("banana");
        List<FoundEntity> tes = new ArrayList<FoundEntity>();
        tes.add(te);
        te2.addPath("entity/clouds");
        
        assertTrue(!FoundEntity.hasUnblacklistedPathFragment(tes, new TaxPath("clouds")));
        assertTrue(!FoundEntity.hasUnblacklistedPathFragment(tes, new TaxPath("entity/clouds")));

        tes.add(te2);
        assertTrue(FoundEntity.hasUnblacklistedPathFragment(tes, new TaxPath("clouds")));
        assertTrue(FoundEntity.hasUnblacklistedPathFragment(tes, new TaxPath("entity/clouds")));
	}
		
	public void testIsBlacklistedForType() {
        Ontology.getInstance().addEntitiesFromFile(new String[]{
                "/data/ontology/ot-8.onto"
        });	
        FoundEntity te = Ontology.getInstance().search("dog");
        assertTrue(te.hasType("clouds"));
        assertTrue(te.hasType("clouds_blacklist"));
        assertTrue(te.isBlacklistedForType("clouds"));
        assertTrue(!te.isBlacklistedForType("waters"));
	}
	
	public void testHasPathFragment() {
		assertPathFragment(true, "/entity/treatment", "treatment");
		assertPathFragment(true, "entity/treatment", "treatment");
		assertPathFragment(true, "entity/treatment", "entity");
		assertPathFragment(true, "entity/treatment", "entity/treatment");
		assertPathFragment(true, "entity/treatment/hypnotique", "treatment/hypnotique");

	}
	
	private void assertPathFragment(boolean shouldHaveIt, String container, String fragment) {
		FoundEntity te = new FoundEntity("la la");
		te.addPath(container);
		if(shouldHaveIt)
			assertTrue(container+" " + fragment, te.hasPathFragment(fragment));
		else
			assertTrue(container+" " + fragment, !te.hasPathFragment(fragment));
	}
}
