package com.xyonix.mayetrix.mayu.text;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyonix.mayetrix.mayu.text.FoundEntity;
import com.xyonix.mayetrix.mayu.text.OntologyFileLoader;

public class TestOntologyLoader extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(TestOntologyLoader.class);
 
    public void testSubstitutionsProb() throws IOException {
        String[] files = new String[]{"/data/ontology/ot-6.onto"};
        List<FoundEntity> tes = OntologyFileLoader.load(files, true);
        boolean hasSug = false;
        for(FoundEntity fE:tes) {
            logger.info(fE.toReadableString());
            if(fE.hasType("suggestion"))
            	hasSug=true;
        }
        assertTrue(hasSug);
    }
    
    public void testLoadOt1() throws IOException {
        String[] files = new String[]{"/data/ontology/ot-1.onto"};
        List<FoundEntity> tes = OntologyFileLoader.load(files, true); 
        
        for(FoundEntity te:tes) {
        	assertTrue(te.getName()+" has an illegal bracket!", !te.getName().contains("["));
            logger.info(te.toReadableString());
        }
    }
    
    private void assertNumWithTypes(List<FoundEntity> tes, String type, int num) {
        int count=0;
        for(FoundEntity fE:tes) {
            if(fE.hasType(type))
                count++;
        }
        assertTrue(count==num);
    }
    
    public void testLoadWithSyns() throws IOException {
        String[] files = new String[]{"/data/ontology/ot-10.onto"};
        List<FoundEntity> tes = OntologyFileLoader.load(files, true); 
        
        for(FoundEntity te:tes) {
            logger.info(te.toReadableString());
        }
    }
    
    public void testLoadWithTypeReplacements() throws IOException {
        String[] files = new String[]{"/data/ontology/ot-2.onto"};
        List<FoundEntity> tes = OntologyFileLoader.load(files, true); 
        
        for(FoundEntity fe:tes) {
            if(fe.getName().contains("["))
                fail("contained types leaking");
            
            logger.info(fe.toReadableString());
        }
        assertTrue(tes.size()==10);
        assertNumWithTypes(tes, "i_am", 6);
    }

}
