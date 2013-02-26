package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.model.PendingConcepts;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionChronicle;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionVersion;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WBUtility
{
	// TODO WB BUG - fix after https://csfe.aceworkspace.net/sf/go/artf227370
	// private static UUID snomedIdType = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"); //SNOMED integer id
	private static UUID snomedIdType = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"); // user
	private static Integer snomedIdTypeNid = null;
	private static UUID FSN_UUID = UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf");
	private static Integer FSNTypeNid = null;
	private static UUID ACTIVE_VALUE_UUID = UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f");
	private static Integer ActiveValueTypeNid = null;

	private static Logger logger = LoggerFactory.getLogger(Utility.class);

	private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, workQueue, new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			t.setName("Concept-Lookup-" + t.getId());
			return t;
		}
	});

	/**
	 * Looks up the identifier (sctid or UUID).  Checks the pendingConcepts list if not found in Snomed.
	 */
	public static void lookupSnomedIdentifier(final String identifier, final ConceptLookupCallback callback, final long submitTime)
	{
		logger.debug("Threaded Lookup: '" + identifier + "'");
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				Concept c = lookupSnomedIdentifier(identifier);
				callback.lookupComplete(c, submitTime);
			}
		};
		tpe.execute(r);
	}

	/**
	 * Looks up the identifier (sctid or UUID).  Checksthe pendingConcepts list if not found in Snomed.
	 */
	public static Concept lookupSnomedIdentifier(String identifier)
	{
		logger.debug("Lookup: '" + identifier + "'");
		ConceptVersionBI result = lookupSnomedIdentifierAsCV(identifier);
		if (result == null)
		{
			// check the pending concepts file
			logger.debug("Lookup Pending Concepts: '" + identifier + "'");
			return PendingConcepts.getInstance().getConcept(identifier);
		}
		return convertConcept(result);
	}

	public static Concept convertConcept(ConceptVersionBI concept)
	{
		Concept c = null;
		if (concept != null && concept.getUUIDs() != null && concept.getUUIDs().size() > 0)
		{
			c = new Concept();
			c.setDesc(getFSN(concept));
			c.setUuid(concept.getUUIDs().get(0).toString());
			try
			{
				for (IdBI x : concept.getAdditionalIds())
				{
					if (x.getAuthorityNid() == getSnomedIdTypeNid() && Utility.isLong(x.getDenotation().toString()))
					{
						c.setSctid(Long.parseLong(x.getDenotation().toString()));
						break;
					}
				}
			}
			catch (Exception e)
			{
				// noop
			}
		}
		return c;
	}

	/**
	 * Looks up the identifier (sctid or UUID).  Does not check the pendingConcepts list.
	 */
	public static ConceptVersionBI lookupSnomedIdentifierAsCV(String identifier)
	{
		logger.debug("WB DB Lookup '" + identifier + "'");
		if (identifier == null || identifier.trim().length() == 0)
		{
			return null;
		}

		ConceptVersionBI result = null;
		try
		{
			UUID uuid = UUID.fromString(identifier.trim());
			result = WBDataStore.Ts().getConceptVersion(StandardViewCoordinates.getSnomedLatest(), uuid);
			if (result.getUUIDs().size() == 0)
			{
				// This is garbage that the WB API invented. Nothing like an undocumented getter which, rather than returning null when the thing
				// you are asking for doesn't exist - it goes off and returns essentially a new, empty, useless node. Sigh.
				throw new IllegalArgumentException();
			}
		}
		catch (IllegalArgumentException | IOException e)
		{
			// try looking up by ID
			try
			{
				result = WBDataStore.Ts().getConceptVersionFromAlternateId(StandardViewCoordinates.getSnomedLatest(), snomedIdType, identifier.trim());
				if (result.getUUIDs().size() == 0)
				{
					// This is garbage that the WB API invented. Nothing like an undocumented getter which, rather than returning null when the thing
					// you are asking for doesn't exist - it goes off and returns essentially a new, empty, useless node. Sigh.
					result = null;
				}
			}
			catch (IOException e1)
			{
				// noop
			}
		}
		return result;
	}

	private static int getSnomedIdTypeNid()
	{
		if (snomedIdTypeNid == null)
		{
			try
			{
				snomedIdTypeNid = WBDataStore.Ts().getNidForUuids(snomedIdType);
			}
			catch (IOException e)
			{
				logger.error("Couldn't find nid for snomed id UUID", e);
				snomedIdTypeNid = -1;
			}
		}
		return snomedIdTypeNid;
	}

	private static int getFSNTypeNid()
	{
		if (FSNTypeNid == null)
		{
			try
			{
				FSNTypeNid = WBDataStore.Ts().getNidForUuids(FSN_UUID);
			}
			catch (IOException e)
			{
				logger.error("Couldn't find nid for FSN UUID", e);
				FSNTypeNid = -1;
			}
		}
		return FSNTypeNid;
	}

	private static int getActiveValueTypeNid()
	{
		if (ActiveValueTypeNid == null)
		{
			try
			{
				ActiveValueTypeNid = WBDataStore.Ts().getNidForUuids(ACTIVE_VALUE_UUID);
			}
			catch (IOException e)
			{
				logger.error("Couldn't find nid for Active Value UUID", e);
				ActiveValueTypeNid = -1;
			}
		}
		return ActiveValueTypeNid;
	}

	/**
	 * Note, this method isn't smart enough to work with multiple versions properly.... assumes you only pass in a concept with current values
	 */
	public static String getFSN(ConceptVersionBI concept)
	{
		String bestFound = null;
		try
		{
			if (concept.getDescs() != null)
			{
				for (DescriptionChronicleBI desc : concept.getDescs())
				{
					DescriptionVersionBI<?> descVer = desc.getVersions().toArray(new DescriptionVersionBI[desc.getVersions().size()])[desc.getVersions().size() - 1];

					if (descVer.getTypeNid() == getFSNTypeNid())
					{
						if (descVer.getStatusNid() == getActiveValueTypeNid())
						{
							return descVer.getText();
						}
						else
						{
							bestFound = descVer.getText();
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			// noop
		}
		return (bestFound == null ? concept.toUserString() : bestFound);
	}

	public static String getFSN(FxConcept concept)
	{
		// Go hunting for a FSN
		if (concept.getDescriptions() == null)
		{
			return concept.getConceptReference().getText();
		}
		String bestFound = null;
		for (FxDescriptionChronicle d : concept.getDescriptions())
		{
			FxDescriptionVersion dv = d.getVersions().get(d.getVersions().size() - 1);
			if (dv.getTypeReference().getUuid().equals(FSN_UUID))
			{
				if (dv.getStatusReference().getUuid().equals(ACTIVE_VALUE_UUID))
				{
					return dv.getText();
				}
				else
				{
					bestFound = dv.getText();
				}
			}
		}
		return (bestFound == null ? concept.getConceptReference().getText() : bestFound);
	}

	/**
	 * Updates (in place) all of the concepts within the supplied LegoList with the results from a WB lookup. Concepts which fail lookup are returned
	 * in the result list.
	 */
	public static List<Concept> lookupAllConcepts(LegoList ll)
	{
		ArrayList<Concept> failures = new ArrayList<>();

		// walk through the legolist, and to a lookup on each concept, flagging errors on the ones that failed lookup.
		for (Lego l : ll.getLego())
		{
			for (Assertion a : l.getAssertion())
			{
				failures.addAll(lookupAll(a.getDiscernible().getExpression()));
				failures.addAll(lookupAll(a.getQualifier().getExpression()));
				failures.addAll(lookupAll(a.getValue().getExpression()));
				if (a.getValue() != null && a.getValue().getMeasurement() != null)
				{
					failures.addAll(lookupAll(a.getValue().getMeasurement()));
				}
				for (AssertionComponent ac : a.getAssertionComponent())
				{
					failures.addAll(lookupAll(ac.getType()));
				}
			}
		}
		return failures;
	}

	private static List<Concept> lookupAll(Expression e)
	{
		ArrayList<Concept> failures = new ArrayList<>();
		if (e == null)
		{
			return failures;
		}
		if (e.getConcept() != null)
		{
			Concept result = lookupSnomedIdentifier(e.getConcept().getUuid());
			if (result == null)
			{
				result = lookupSnomedIdentifier(e.getConcept().getSctid() + "");
			}
			if (result != null)
			{
				e.setConcept(result);
			}
			else
			{
				failures.add(e.getConcept());
			}
		}
		for (Expression e1 : e.getExpression())
		{
			failures.addAll(lookupAll(e1));
		}
		for (Relation r : e.getRelation())
		{
			failures.addAll(lookupAll(r));
		}
		for (RelationGroup rg : e.getRelationGroup())
		{
			for (Relation r : rg.getRelation())
			{
				failures.addAll(lookupAll(r));
			}
		}
		return failures;
	}

	private static List<Concept> lookupAll(Relation r)
	{
		ArrayList<Concept> failures = new ArrayList<>();
		if (r.getType() != null && r.getType().getConcept() != null)
		{
			failures.addAll(lookupAll(r.getType()));
		}
		if (r.getDestination() != null)
		{
			failures.addAll(lookupAll(r.getDestination().getExpression()));
			failures.addAll(lookupAll(r.getDestination().getMeasurement()));
		}
		return failures;
	}

	private static List<Concept> lookupAll(Type t)
	{
		ArrayList<Concept> failures = new ArrayList<Concept>();
		if (t == null || t.getConcept() == null)
		{
			return failures;
		}
		Concept result = lookupSnomedIdentifier(t.getConcept().getUuid());
		if (result == null)
		{
			result = lookupSnomedIdentifier(t.getConcept().getSctid() + "");
		}
		if (result != null)
		{
			t.setConcept(result);
		}
		else
		{
			failures.add(t.getConcept());
		}
		return failures;
	}

	private static List<Concept> lookupAll(Measurement m)
	{
		ArrayList<Concept> failures = new ArrayList<>();
		if (m == null || m.getUnits() == null || m.getUnits().getConcept() == null)
		{
			return failures;
		}
		Concept result = lookupSnomedIdentifier(m.getUnits().getConcept().getUuid());
		if (result == null)
		{
			result = lookupSnomedIdentifier(m.getUnits().getConcept().getSctid() + "");
		}
		if (result != null)
		{
			m.getUnits().setConcept(result);
		}
		else
		{
			failures.add(m.getUnits().getConcept());
		}
		return failures;
	}
}
