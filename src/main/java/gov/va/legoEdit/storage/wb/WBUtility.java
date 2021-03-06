package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.LegoGUIModel;
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
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompVersionDdo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * WBUtility
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class WBUtility
{
	private static UUID snomedIdType = TermAux.SNOMED_IDENTIFIER.getUuids()[0]; //SNOMED integer id
	public static Integer snomedIdTypeNid = null;  //This is public for JUnit test purposes in the sim-api conversions.
	private static UUID FSN_UUID = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
	private static Integer FSNTypeNid = null;
	private static UUID PREFERRED_UUID = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
	private static Integer preferredNid = null;
	private static UUID SYNONYM_UUID = SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0];
	private static Integer synonymNid = null;
	
	public static UserPreferences up = LegoGUIModel.getInstance().getUserPreferences();

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
	 * @param callId is just an optional field that will be returned to the caller in the callback.
	 */
	public static void lookupSnomedIdentifier(final String identifier, final String description, final ConceptLookupCallback callback, final Integer callId)
	{
		logger.debug("Threaded Lookup: '" + identifier + "'");
		final long submitTime = System.currentTimeMillis();
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				Concept c = lookupSnomedIdentifier(identifier, description);
				callback.lookupComplete(c, submitTime, callId);
			}
		};
		tpe.execute(r);
	}

	/**
	 * Looks up the identifier (sctid or UUID).  Checks the pendingConcepts list if not found in Snomed.
	 */
	public static Concept lookupSnomedIdentifier(String identifier)
	{
		logger.debug("Lookup: '" + identifier + "'");
		ConceptVersionBI result = lookupSnomedIdentifierAsCV(identifier);
		if (result == null)
		{
			// check the pending concepts file
			logger.debug("Lookup Pending Concepts: '" + identifier + "'");
			return PendingConcepts.getInstance().getConcept(identifier, null);
		}
		return convertConcept(result);
	}
	
	/**
	 * Looks up the identifier (sctid or UUID).  Checks the pendingConcepts list if not found in Snomed.
	 * Description (optional) is used if we are looking up against the pending Concepts, and find multiple 
	 * matches due to ID management issues - the description will match us to the correct new ID.
	 */
	public static Concept lookupSnomedIdentifier(String identifier, String description)
	{
		logger.debug("Lookup: '" + identifier + "'");
		ConceptVersionBI result = lookupSnomedIdentifierAsCV(identifier);
		if (result == null)
		{
			// check the pending concepts file
			logger.debug("Lookup Pending Concepts: '" + identifier + "' '" + description +"'");
			return PendingConcepts.getInstance().getConcept(identifier, description);
		}
		return convertConcept(result);
	}

	public static Concept convertConcept(ConceptChronicleBI concept)
	{
		Concept c = null;
		if (concept != null && concept.getUUIDs() != null && concept.getUUIDs().size() > 0)
		{
			c = new Concept();
			c.setDesc(getDescription(concept));
			c.setUuid(concept.getUUIDs().get(0).toString());
			try
			{
				for (RefexChronicleBI<?> annotation : concept.getAnnotations())
				{
					if (annotation.getAssemblageNid() == getSnomedIdTypeNid())
					{
						RefexLongVersionBI<?> sctid = (RefexLongVersionBI<?>) annotation.getPrimordialVersion();
						c.setSctid(sctid.getLong1());
						break;
					}
				}
				
				if (c.getSctid() == null)
				{
					//old way for old econ files
					for (IdBI x : concept.getAdditionalIds())
					{
						if (x.getAuthorityNid() == getSnomedIdTypeNid() && Utility.isLong(x.getDenotation().toString()))
						{
							c.setSctid(Long.parseLong(x.getDenotation().toString()));
							break;
						}
					}
				}
			}
			catch (Exception e)
			{
				// noop
			}
			if (c.getSctid() == null)
			{
				logger.debug("Couldn't find SCTID for concept " + c.getDesc() + " " + c.getUuid());
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
			result = WBDataStore.Ts().getConceptVersion(StandardViewCoordinates.getSnomedStatedLatest(), uuid);
			if (result.getUUIDs().size() == 0)
			{
				// This is sillyness of the WB API. Nothing like an undocumented getter which, rather than returning null when the thing
				// you are asking for doesn't exist - it goes off and returns essentially a new, empty, useless node. Sigh.
				throw new IllegalArgumentException();
			}
		}
		catch (IllegalArgumentException | IOException e)
		{
			// try looking up by ID
			try
			{
				//getConceptVersionFromAlternateId seems broke after the DB update, make the UUID myself instead.
				result = WBDataStore.Ts().getConceptVersion(StandardViewCoordinates.getSnomedStatedLatest(), UuidFactory.getUuidFromAlternateId(snomedIdType, identifier.trim()));
				//result = WBDataStore.Ts().getConceptVersionFromAlternateId(StandardViewCoordinates.getSnomedLatest(), snomedIdType, identifier.trim());
				if (result.getUUIDs().size() == 0)
				{
					// This is sillyness of the WB API. Nothing like an undocumented getter which, rather than returning null when the thing
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
	
	private static int getPreferredTypeNid()
	{
		if (preferredNid == null)
		{
			try
			{
				preferredNid = WBDataStore.Ts().getNidForUuids(PREFERRED_UUID);
			}
			catch (IOException e)
			{
				logger.error("Couldn't find nid for Preferred UUID", e);
				preferredNid = -1;
			}
		}
		return preferredNid;
	}
	
	private static int getSynonymTypeNid()
	{
		if (synonymNid == null)
		{
			try
			{
				synonymNid = WBDataStore.Ts().getNidForUuids(SYNONYM_UUID);
			}
			catch (IOException e)
			{
				logger.error("Couldn't find nid for synonymNid UUID", e);
				synonymNid = -1;
			}
		}
		return synonymNid;
	}

	public static Concept getConceptForNid(Integer nid)
	{
		if (nid == null)
		{
			return null;
		}
		try
		{
			ConceptChronicleBI cc = WBDataStore.Ts().getConcept(nid);
			return convertConcept(cc);
		}
		catch (Exception e)
		{
			logger.error("Error during nid lookup", e);
		}
		return null;
	}

	/**
	 * Note, this method isn't smart enough to work with multiple versions properly.... assumes you only pass in a concept with current values
	 */
	public static String getDescription(ConceptChronicleBI concept)
	{
		String fsn = null;
		String preferred = null;
		String bestFound = null;
		try
		{
			if (concept.getDescriptions() != null)
			{
				for (DescriptionChronicleBI desc : concept.getDescriptions())
				{
					DescriptionVersionBI<?> descVer = desc.getVersions().toArray(new DescriptionVersionBI[desc.getVersions().size()])[desc.getVersions().size() - 1];

					if (descVer.getTypeNid() == getFSNTypeNid())
					{
						if (descVer.getStatus() == Status.ACTIVE)
						{
							if (up.getUseFSN())
							{
								return descVer.getText();
							}
							else
							{
								fsn = descVer.getText();
							}
							
						}
						else
						{
							bestFound = descVer.getText();
						}
					}
					else if (descVer.getTypeNid() == getSynonymTypeNid() && isPreferred(descVer.getAnnotations()))
					{
						if (descVer.getStatus() == Status.ACTIVE)
						{
							if (!up.getUseFSN())
							{
								return descVer.getText();
							}
							else
							{
								preferred = descVer.getText();
							}
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
		//If we get here, we didn't find what they were looking for. Pick something....
		return (fsn != null ? fsn : (preferred != null ? preferred : (bestFound != null ? bestFound : concept.toUserString())));
	}
	
	public static String getDescription(UUID uuid)
	{
		try
		{
			return getDescription(WBDataStore.Ts().getConceptVersion(StandardViewCoordinates.getSnomedStatedLatest(), uuid));
		}
		catch (Exception e)
		{
			logger.warn("Unexpected error looking up description", e);
			return null;
		}
	}

	public static String getDescription(ConceptChronicleDdo concept)
	{
		// Go hunting for a FSN
		if (concept == null)
		{
			return null;
		}
		if (concept.getDescriptions() == null)
		{
			if (concept.getConceptReference() == null)
			{
				return null;
			}
			return concept.getConceptReference().getText();
		}
		
		String fsn = null;
		String preferred = null;
		String bestFound = null;
		for (DescriptionChronicleDdo d : concept.getDescriptions())
		{
			DescriptionVersionDdo dv = d.getVersions().get(d.getVersions().size() - 1);
			if (dv.getTypeReference().getUuid().equals(FSN_UUID))
			{
				if (dv.getStatus() == Status.ACTIVE)
				{
					if (up.getUseFSN())
					{
						return dv.getText();
					}
					else
					{
						fsn = dv.getText();
					}
				}
				else
				{
					bestFound = dv.getText();
				}
			}
			else if (dv.getTypeReference().getUuid().equals(SYNONYM_UUID))
			{
				if (dv.getStatus() == Status.ACTIVE && isPreferred(dv.getAnnotations()))
				{
					if (!up.getUseFSN())
					{
						return dv.getText();
					}
					else
					{
						preferred = dv.getText();
					}
				}
				else
				{
					bestFound = dv.getText();
				}
			}
		}
		//If we get here, we didn't find what they were looking for. Pick something....
		return (fsn != null ? fsn : (preferred != null ? preferred : (bestFound != null ? bestFound : concept.getConceptReference().getText())));
	}
	
	private static boolean isPreferred(List<RefexChronicleDdo<?, ?>> annotations)
	{
		for (RefexChronicleDdo<?, ?> frc : annotations)
		{
			for (Object version : frc.getVersions())
			{
				if (version instanceof RefexCompVersionDdo && ((RefexCompVersionDdo<?,?>)version).getComp1Ref().getUuid().equals(PREFERRED_UUID))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean isPreferred(Collection<? extends RefexChronicleBI<?>> collection)
	{
		for (RefexChronicleBI<?> rc : collection)
		{
			if (rc.getAssemblageNid() == getPreferredTypeNid());
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates (in place) all of the concepts within the supplied LegoList with the results from a WB lookup. Concepts which fail lookup are returned
	 * in the result list.
	 */
	public static List<Concept> lookupAllConcepts(LegoList ll)
	{
		ArrayList<Concept> failures = new ArrayList<>();

		// walk through the legolist, and do a lookup on each concept, flagging errors on the ones that failed lookup.
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
			Concept result = lookupSnomedIdentifier(e.getConcept().getUuid(), e.getConcept().getDesc());
			if (result == null)
			{
				result = lookupSnomedIdentifier(e.getConcept().getSctid() + "", e.getConcept().getDesc());
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
