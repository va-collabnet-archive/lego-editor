package gov.va.legoEdit.model.bdbModel;

import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.ConceptAndRel;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Rel;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.WriteException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * This class handles the storage of the LEGO object into the BerkeleyDB.
 *
 * @author darmbrust
 */
@Entity
public class LegoBDB
{
    @PrimaryKey
    private String uniqueId;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String legoUUID;
    protected String stampId;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String pncsId;
    protected List<Assertion> assertions;
    @SecondaryKey(relate = Relationship.MANY_TO_MANY)
    protected Set<String> usedSCTIdentifiers;
    //This is the list of assertions defined within this lego.  
    @SecondaryKey(relate = Relationship.MANY_TO_MANY)
    protected Set<String> usedAssertionUUIDs;
    //This is the list of assertions linked to by this lego in the form of composite assertions.
    @SecondaryKey(relate = Relationship.MANY_TO_MANY)
    protected Set<String> compositeAssertionUUIDs;
    
    //not stored
    private transient PncsBDB pncsBDBRef;
    private transient StampBDB stampBDBRef;

    @SuppressWarnings("unused")
	private LegoBDB()
    {
        //required by BDB
    }

    public LegoBDB(Lego lego) throws WriteException
    {
        legoUUID = lego.getLegoUUID();
        pncsBDBRef = new PncsBDB(lego.getPncs());
        pncsId = pncsBDBRef.getUniqueId();
        stampBDBRef = new StampBDB(lego.getStamp());
        stampId = stampBDBRef.getStampId();
        assertions = new ArrayList<>();
        usedAssertionUUIDs = new HashSet<>();
        compositeAssertionUUIDs = new HashSet<>();
        usedSCTIdentifiers = new HashSet<>();
        for (Assertion a : lego.getAssertion())
        {
            assertions.add(a);
            checkAndUpdateAssertionList(a);
            
            //Keith requested this schema change, which results in this ugly API... sigh.
            Concept discernibleConcept = a.getDiscernible().getConcept();
            if (discernibleConcept == null)
            {
            	discernibleConcept = a.getDiscernible().getConceptAndRel();
            }
            indexConcept(discernibleConcept);
            if (discernibleConcept instanceof ConceptAndRel)
            {
	            for (Rel r : ((ConceptAndRel)discernibleConcept).getRel())
	            {
	                indexConcept(r.getConcept());
	                indexConcept(r.getTypeConcept());
	            }
            }
            indexConcept(a.getQualifier().getConcept());
            if (a.getTiming() != null)
            {
                Measurement m = a.getTiming().getMeasurement();
                if (m != null && m.getUnits() != null)
                {
                    indexConcept(m.getUnits().getConcept());
                }
            }
            indexConcept(a.getValue().getConcept());
            Measurement m = a.getValue().getMeasurement();
            if (m != null && m.getUnits() != null)
            {
                indexConcept(m.getUnits().getConcept());
            }
            
            if (a.getAssertionComponents() != null)
            {
	            for (AssertionComponent ac : a.getAssertionComponents().getAssertionComponent())
	            {
	            	compositeAssertionUUIDs.add(ac.getAssertionUUID());
	            }
            }
        }

        this.uniqueId = legoUUID + ":" + stampId;
    }

    private void indexConcept(Concept c)
    {
        if (c != null)
        {
            if (c.getSctid() != null)
            {
                usedSCTIdentifiers.add(c.getSctid() + "");
            }
            if (c.getUuid() != null && c.getUuid().length() > 0)
            {
                usedSCTIdentifiers.add(c.getUuid());
            }
        }
    }

    public String getLegoUUID()
    {
        return legoUUID;
    }

    public String getStampId()
    {
        return stampId;
    }
    
    public StampBDB getStampBDB()
    {
        if (stampBDBRef == null)
        {
            stampBDBRef = ((BDBDataStoreImpl)BDBDataStoreImpl.getInstance()).getStampByUniqueId(stampId);
        }
        return stampBDBRef;
    }
    
    public PncsBDB getPncsBDB()
    {
        return pncsBDBRef;
    }

    public String getPncsId()
    {
        return pncsId;
    }

    public void addAssertion(Assertion assertion) throws WriteException
    {
        if (assertions == null)
        {
            assertions = new ArrayList<>();
        }
        assertions.add(assertion);
        checkAndUpdateAssertionList(assertion);
        
        for (AssertionComponent ac : assertion.getAssertionComponents().getAssertionComponent())
        {
        	compositeAssertionUUIDs.add(ac.getAssertionUUID());
        }
        
        Concept discernibleConcept = assertion.getDiscernible().getConcept();
        if (discernibleConcept == null)
        {
        	discernibleConcept = assertion.getDiscernible().getConceptAndRel();
        }
        indexConcept(discernibleConcept);
        if (discernibleConcept instanceof ConceptAndRel)
        {
            for (Rel r : ((ConceptAndRel)discernibleConcept).getRel())
            {
                indexConcept(r.getConcept());
                indexConcept(r.getTypeConcept());
            }
        }

        indexConcept(assertion.getQualifier().getConcept());
        if (assertion.getTiming() != null)
        {
            Measurement m = assertion.getTiming().getMeasurement();
            if (m != null && m.getUnits() != null)
            {
                indexConcept(m.getUnits().getConcept());
            }
            
        }
        indexConcept(assertion.getValue().getConcept());
        Measurement m = assertion.getValue().getMeasurement();
        if (m != null && m.getUnits() != null)
        {
            indexConcept(m.getUnits().getConcept());
        }
    }

    /**
     * Note, this returns a copy (you can't add to this list)
     */
    public List<Assertion> getAssertions()
    {
        ArrayList<Assertion> result = new ArrayList<>();
        if (assertions == null)
        {
            return result;
        }
        for (Assertion a : assertions)
        {
            result.add(a);
        }
        return result;
    }

    public String getUniqueId()
    {
        return uniqueId;
    }

    public Lego toSchemaLego()
    {
        Lego l = new Lego();
        l.setLegoUUID(legoUUID);
        l.setStamp(getStampBDB().toSchemaStamp());
        l.setPncs(((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(pncsId));
        if (l.getPncs() == null)
        {
            throw new DataStoreException("Pncs should be present!");
        }
        l.getAssertion().addAll(assertions);
        return l;
    }
    
    private void checkAndUpdateAssertionList(Assertion a) throws WriteException
    {
        if (!usedAssertionUUIDs.add(a.getAssertionUUID()))
        {
            throw new WriteException("Each assertion within a Lego must have a unique UUID");
        }
        Set<String> legosUsingAssertionUUID = ((BDBDataStoreImpl)BDBDataStoreImpl.getInstance()).getLegoUUIDsContainingAssertion(a.getAssertionUUID());
        legosUsingAssertionUUID.remove(legoUUID);
        if (legosUsingAssertionUUID.size() > 0)
        {
            throw new WriteException("The assertion UUID '" + a.getAssertionUUID() + "' is already in use by the lego '" 
                    + legosUsingAssertionUUID.iterator().next() + "'.  Assertion UUIDs should be unique");
        }
    }
}
