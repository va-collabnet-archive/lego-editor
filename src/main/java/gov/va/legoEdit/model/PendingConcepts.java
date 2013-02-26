package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PendingConcepts
{
	public static File pendingConceptsFile = new File("pendingConcepts.tsv");
	private static Logger logger = LoggerFactory.getLogger(PendingConcepts.class);
	private HashMap<Long, Concept> pendingConcepts = new HashMap<>();
	private HashMap<Long, Concept> parentConcepts = new HashMap<>();
	private long highestInUseId = 0;
	private static volatile PendingConcepts instance_;

	public static PendingConcepts getInstance()
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
						c.setUuid(UUID.nameUUIDFromBytes(parts[0].getBytes()).toString());
						
						Concept parent = null;

						if (parts.length > 2)
						{
							long parentSCTID = Long.parseLong(parts[2]);
							//Use this lookup, since it doesn't loop back to pending.
							ConceptVersionBI wbParentConcept =  WBUtility.lookupSnomedIdentifierAsCV(parentSCTID + "");
							if (wbParentConcept != null)
							{
								parent = WBUtility.convertConcept(wbParentConcept);
							}
							else
							{
								logger.error("The specified parent concept for " + c.getSctid() + " doesn't exist and will be ignored");
							}
						}
						
						if (!areIdentifiersUnique(c))
						{
							logger.error("Pending concepts contains a value which is a duplicate, or already exists in snomed '" + c.getSctid() + "'.  Ignoring.");
							continue;
						}
						
						pendingConcepts.put(c.getSctid(), c);
						if (c.getSctid() > highestInUseId)
						{
							highestInUseId = c.getSctid();
						}
						if (parent != null)
						{
							parentConcepts.put(c.getSctid(), parent);
						}
					}
					else
					{
						logger.error("Pending concepts need an ID and a description");
					}
				}
			}
			logger.info("Loaded " + pendingConcepts.size() + " pending concepts");
		}
		catch (IOException e)
		{
			logger.error("Unexpected error loading pending concepts file", e);
		}
	}
	
	public boolean areIdentifiersUnique(Concept potentialPendingConcept)
	{
		if (pendingConcepts.containsKey(potentialPendingConcept.getSctid()))
		{
			return false;
		}

		if (null != WBUtility.lookupSnomedIdentifierAsCV(potentialPendingConcept.getSctid().toString()))
		{
			return false;
		}
		if (potentialPendingConcept.getUuid() != null && null != WBUtility.lookupSnomedIdentifierAsCV(potentialPendingConcept.getUuid().toString()))
		{
			return false;
		}
		return true;
	}
	
	public void addConcept(long id, String description, Long parent) throws IllegalArgumentException
	{
		Concept c = new Concept();
		c.setSctid(id);
		c.setDesc(description);
		c.setUuid(UUID.nameUUIDFromBytes((c.getSctid() + "").getBytes()).toString());
		if (areIdentifiersUnique(c))
		{
			pendingConcepts.put(id, c);
			if (c.getSctid() > highestInUseId)
			{
				highestInUseId = c.getSctid();
			}
			if (parent != null)
			{
				//Use this lookup, since it doesn't loop back to pending.
				ConceptVersionBI wbParentConcept =  WBUtility.lookupSnomedIdentifierAsCV(parent + "");
				if (wbParentConcept != null)
				{
					Concept parentConcept = WBUtility.convertConcept(wbParentConcept);
					parentConcepts.put(id, parentConcept);
				}
				else
				{
					throw new IllegalArgumentException("The specified parent SCTID isn't a valid snomed concept");
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("The provided concept is not unique");
		}
		try
		{
			rewritePendingConceptsFile();
		}
		catch (IOException e)
		{
			logger.error("Pending concepts Store failed", e);
			pendingConcepts.remove(id);
			parentConcepts.remove(id);
			throw new IllegalArgumentException("Sorry, store failed");
		}
	}
	
	public void deleteConcept(long id) throws IllegalArgumentException
	{
		Concept pending = pendingConcepts.remove(id);
		Concept parent = parentConcepts.remove(id);
		try
		{
			rewritePendingConceptsFile();
		}
		catch (IOException e)
		{
			logger.error("Pending concepts Store failed", e);
			pendingConcepts.put(id,  pending);
			if(parent != null)
			{
				parentConcepts.put(id, parent);
			}
			throw new IllegalArgumentException("Sorry, store failed");
		}
	}
	
	public static long getUnusedId()
	{
		while (true)
		{
			long temp = ++getInstance().highestInUseId;
			Concept possible = new Concept();
			possible.setSctid(temp);
			if (getInstance().areIdentifiersUnique(possible))
			{
				return temp;
			}
		}
	}
	
	private void rewritePendingConceptsFile() throws IOException
	{
		//Read through the existing file, keeping the comments, and any lines we don't understand.  
		//only keep the concepts if they our in our current list.  Finally, add any concepts that are missing.
		
		HashSet<Long> unstoredConcepts = new HashSet<>();
		unstoredConcepts.addAll(pendingConcepts.keySet());
		
		List<String> lines = Files.readAllLines(pendingConceptsFile.toPath(), StandardCharsets.US_ASCII);
		StringBuilder replacement = new StringBuilder();
		String eol = System.getProperty("line.separator");
		for (String line : lines)
		{
			if (line.startsWith("#") || line.length() == 0)
			{
				replacement.append(line);
				replacement.append(eol);
			}
			else
			{
				String[] parts = line.split("\t");
				try
				{
					long id = Long.parseLong(parts[0]);
					if (pendingConcepts.containsKey(id))
					{
						replacement.append(buildLine(pendingConcepts.get(id)));
						replacement.append(eol);
						unstoredConcepts.remove(id);
					}
					
				}
				catch (Exception e) 
				{
					replacement.append(line);
					replacement.append(eol);
				}
			}
		}
		for (Long l : unstoredConcepts)
		{
			replacement.append(buildLine(pendingConcepts.get(l)));
			replacement.append(eol);
		}
		
		Files.write(pendingConceptsFile.toPath(), replacement.toString().getBytes(), 
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}
	
	private String buildLine(Concept c)
	{
		Concept parent = parentConcepts.get(c.getSctid());
		return c.getSctid() + "\t" + c.getDesc() + (parent == null ? "" : "\t" + parent.getSctid() + "\t" + parent.getDesc());
	}

	public static Concept getConcept(long id)
	{
		return getInstance().pendingConcepts.get(id);
	}
	
	public static Concept getParentConcept(long pendingConceptId)
	{
		return getInstance().pendingConcepts.get(pendingConceptId);
	}
}
