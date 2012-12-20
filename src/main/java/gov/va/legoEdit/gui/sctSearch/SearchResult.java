package gov.va.legoEdit.gui.sctSearch;

import gov.va.legoEdit.storage.wb.WBDataStore;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchResult
{
    Logger logger = LoggerFactory.getLogger(SearchResult.class);
    
    private static AtomicInteger sortHelper = new AtomicInteger();
    private int conceptNid;
    private HashSet<String> matchingStrings = new HashSet<>();
    private int sortOrder;
    private ConceptChronicleBI concept;
    
    public SearchResult(int conceptNid)
    {
        this.conceptNid = conceptNid;
        this.sortOrder = sortHelper.getAndIncrement();
        try
        {
            //I tried using the FXConcept API here, but the performance was dreadful
//            concept = WBDataStore.Ts().getFxConcept(WBDataStore.Ts().getUuidPrimordialForNid(conceptNid), StandardViewCoordinates.getSnomedLatest());
            concept = WBDataStore.Ts().getConcept(conceptNid);
        }
        catch (Exception e)
        {
            logger.error("Unexpected error getting FXConcept", e);
        }
    }
    
    public void addMatchingString(String matchingString)
    {
        matchingStrings.add(matchingString);
    }
    
    protected int getSortOrder()
    {
        return sortOrder;
    }
    
    public int getConceptNid()
    {
        return conceptNid;
    }
    
    public Set<String> getMatchStrings()
    {
        return matchingStrings;
    }
    
    public ConceptChronicleBI getConcept()
    {
        return concept;
    }
}
