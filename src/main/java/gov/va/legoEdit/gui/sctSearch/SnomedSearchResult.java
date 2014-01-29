package gov.va.legoEdit.gui.sctSearch;

import gov.va.legoEdit.storage.wb.WBDataStore;
import java.util.HashSet;
import java.util.Set;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * SnomedSearchResult
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class SnomedSearchResult
{
	Logger logger = LoggerFactory.getLogger(SnomedSearchResult.class);

	private int conceptNid;
	private HashSet<String> matchingStrings = new HashSet<>();
	private float bestScore;  //best score, rather than score, as multiple matches may go into a snomedSearchResult
	private ConceptVersionBI concept;

	public SnomedSearchResult(int conceptNid, float score)
	{
		this.conceptNid = conceptNid;
		this.bestScore = score;
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
	
	public void adjustScore(float newScore)
	{
		bestScore = newScore;
	}

	public float getBestScore()
	{
		return bestScore;
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
