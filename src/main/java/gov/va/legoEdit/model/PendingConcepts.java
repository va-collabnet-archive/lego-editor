package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PendingConcepts
{
	public static File pendingConceptsFile = new File("pendingConcepts.tsv");
	private static Logger logger = LoggerFactory.getLogger(PendingConcepts.class);
	private HashMap<String, Concept> pendingConceptsById = new HashMap<>();
	private HashMap<String, Concept> pendingConceptsByUuid = new HashMap<>();
	private static volatile PendingConcepts instance_;

	private static PendingConcepts getInstance()
	{
		if (instance_ == null)
		{
			synchronized (PendingConcepts.class)
			{
				if (instance_ == null)
				{
					instance_ = new PendingConcepts();
				}
			}
		}
		return instance_;
	}

	private PendingConcepts()
	{
		logger.info("Loading pending concepts from: " + pendingConceptsFile.getAbsolutePath());
		try
		{
			if (pendingConceptsFile.exists())
			{
				List<String> lines = Files.readAllLines(pendingConceptsFile.toPath(), StandardCharsets.US_ASCII);
				for (String s : lines)
				{
					if (s.startsWith("#") || s.length() == 0)
					{
						continue;
					}
					String[] parts = s.split("\t");
					if (parts.length > 1)
					{
						Concept c = new Concept();
						try
						{
							c.setSctid(Long.parseLong(parts[0]));
						}
						catch (NumberFormatException e)
						{
							logger.error("Invalid ID in pending concepts file - line '" + s + "'");
							continue;
						}
						c.setDesc(parts[1]);

						if (parts.length > 2)
						{
							c.setUuid(parts[2]);
						}
						else
						{
							c.setUuid(UUID.nameUUIDFromBytes(parts[0].getBytes()).toString());
						}
						if (pendingConceptsById.containsKey(c.getSctid().toString()))
						{
							logger.error("Pending concepts contains a duplicate entry for '" + c.getSctid() + "'.  Ignoring duplicate SCTID.");
							continue;
						}

						if (pendingConceptsByUuid.containsKey(c.getUuid()))
						{
							logger.error("Pending concepts contains a duplicate entry for '" + c.getUuid() + "'.  Ignoring duplicate UUID.");
							c.setUuid(null);
						}
						
						if (null != WBUtility.lookupSnomedIdentifierAsCV(c.getSctid().toString()))
						{
							logger.error("Pending concepts contains a SCTID which already exists in snomed (" + c.getSctid() + ").  Ignoring.");
							continue;
						}
						if (c.getUuid() != null && null != WBUtility.lookupSnomedIdentifierAsCV(c.getUuid().toString()))
						{
							logger.error("Pending concepts contains a UUID which already exists in snomed (" + c.getUuid() + ").  Ignoring.");
							continue;
						}
						pendingConceptsById.put(c.getSctid().toString(), c);
						if (c.getUuid() != null)
						{
							pendingConceptsByUuid.put(c.getUuid(), c);
						}
					}
					else
					{
						logger.error("Pending concepts need an ID and a description");
					}
				}
			}
			logger.info("Loaded " + pendingConceptsById.size() + " pending concepts");
		}
		catch (IOException e)
		{
			logger.error("Unexpected error loading pending concepts file", e);
		}
	}

	public static Concept getConcept(String id)
	{
		Concept c = getInstance().pendingConceptsById.get(id);
		if (c == null)
		{
			return getInstance().pendingConceptsByUuid.get(id);
		}
		else
		{
			return c;
		}
	}
}
