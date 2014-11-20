/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * {@link PendingConceptsRemapped}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class PendingConceptsRemapped
{	
	private static Logger logger = LoggerFactory.getLogger(PendingConceptsRemapped.class);

	private HashMap<Long, Pair<Long, String>> remappedIDs = new HashMap<>();  //Maps new ID -> Old ID, Old Description
	
	private transient HashMap<String, Long> reverseHash = new HashMap<>();  //Maps "Old Desc|Old ID" -> new ID
	private transient HashMap<Long, ArrayList<Pair<Long, String>>> oldToNew = new HashMap<>();  //Maps old ID -> new ID, old description

	protected PendingConceptsRemapped()
	{
		
	}
	
	protected void remap(Long oldID, String oldDescription, Long newID)
	{
		Pair<Long, String> oldValues = new Pair<>(oldID, oldDescription);
		Pair<Long, String> existing = remappedIDs.put(newID, oldValues);
		if (existing != null)
		{
			throw new RuntimeException("oops");
		}
		
		reverseHash.put(hash(oldID, oldDescription), newID);
		
		ArrayList<Pair<Long, String>> newIDs = oldToNew.get(oldID);
		if (newIDs == null)
		{
			newIDs = new ArrayList<Pair<Long, String>>();
			oldToNew.put(oldID, newIDs);
		}
		newIDs.add(new Pair<>(newID, oldDescription));
	}
	
	public boolean hasRemap(Long id)
	{
		return oldToNew.containsKey(id);
	}
	
	public ArrayList<Pair<Long, String>> getRemap(Long id)
	{
		return oldToNew.get(id);
	}
	
	public Long getNewID(Long oldID, String oldDescription)
	{
		return reverseHash.get(hash(oldID, oldDescription));
	}
	
	protected String hash(Long oldId, String oldDescription)
	{
		return oldId + "|" + oldDescription;
	}
	
	private void buildTransients()
	{
		for (Entry<Long, Pair<Long, String>> x : remappedIDs.entrySet())
		{
			reverseHash.put(hash(x.getValue().getKey(), x.getValue().getValue()), x.getKey());
			
			ArrayList<Pair<Long, String>> newIDs = oldToNew.get(x.getValue().getKey());
			if (newIDs == null)
			{
				newIDs = new ArrayList<Pair<Long, String>>();
				oldToNew.put(x.getValue().getKey(), newIDs);
			}
			newIDs.add(new Pair<>(x.getKey(), x.getValue().getValue()));
		}
	}

	protected void store(File fileToWrite) throws IOException
	{
		try
		{
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			FileWriter fw = new FileWriter(fileToWrite);
			gson.toJson(remappedIDs, fw);
			fw.close();
		}
		catch (Exception e)
		{
			throw new IOException("Problem storings PendingConceptsRemapped to " + fileToWrite.getAbsolutePath(), e);
		}
	}

	protected static PendingConceptsRemapped read(File path) throws IOException
	{
		try
		{
			Type readType = new TypeToken<HashMap<Long, Pair<Long, String>>>() {}.getType();
			PendingConceptsRemapped pcr = new PendingConceptsRemapped();
			Gson gson = new Gson();
			FileReader fr = new FileReader(path);
			pcr.remappedIDs = gson.fromJson(fr, readType);
			fr.close();
			
			pcr.buildTransients();
			return pcr;
		}
		catch (Exception e)
		{
			logger.error("Problem reading PendingConceptsRemapped from " + path.getAbsolutePath(), e);
			throw new IOException("Problem reading PendingConceptsRemapped", e);
		}
	}
}
