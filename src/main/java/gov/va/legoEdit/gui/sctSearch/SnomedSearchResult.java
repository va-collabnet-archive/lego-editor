package gov.va.legoEdit.gui.sctSearch;

import gov.va.legoEdit.storage.wb.WBDataStore;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnomedSearchResult
{
	Logger logger = LoggerFactory.getLogger(SnomedSearchResult.class);

	private static AtomicInteger sortHelper = new AtomicInteger();
	private int conceptNid;
	private HashSet<String> matchingStrings = new HashSet<>();
	private int sortOrder;
	private ConceptVersionBI concept;

	public SnomedSearchResult(int conceptNid)
	{
		this.conceptNid = conceptNid;
		this.sortOrder = sortHelper.getAndIncrement();
		try
		{
			// I tried using the FXConcept API here, but the performance was dreadful
			// concept = WBDataStore.Ts().getFxConcept(WBDataStore.Ts().getUuidPrimordialForNid(conceptNid),
			// StandardViewCoordinates.getSnomedLatest());
			concept = WBDataStore.Ts().getConceptVersion(StandardViewCoordinates.getSnomedLatest(), conceptNid);
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

	public ConceptVersionBI getConcept()
	{
		return concept;
	}
}
