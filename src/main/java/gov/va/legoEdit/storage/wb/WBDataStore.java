package gov.va.legoEdit.storage.wb;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.search.SearchHandler;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.legoEdit.storage.DataStoreException;
import java.io.File;
import org.ihtsdo.oft.tcc.rest.client.TccRestClient;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.ddo.store.FxTerminologyStoreDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * WBDataStore
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class WBDataStore
{

	private static volatile WBDataStore instance_;
	private static Logger logger = LoggerFactory.getLogger(WBDataStore.class);
	private boolean useLocal = true;
	private TerminologyStoreDI dataStore_ = null;

	public static WBDataStore getInstance() throws DataStoreException
	{
		if (instance_ == null)
		{
			synchronized (WBDataStore.class)
			{
				if (instance_ == null)
				{
					instance_ = new WBDataStore();
				}
			}
		}
		if (instance_.dataStore_ == null)
		{
			throw new DataStoreException("Already shutdown.  No further operations are allowed.");
		}
		return instance_;
	}

	private WBDataStore()
	{
		if (useLocal)
		{
			logger.info("Opening a connection to a local WB Data Store");
			try
			{
				boolean configured = false;
				String path = System.getProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY);
				if (path != null && path.length() > 0)
				{
					File temp = new File(path);
					if (temp.isDirectory())
					{
						dataStore_ = ExtendedAppContext.getDataStore();
						configured = true;
					}
				}

				if (!configured)
				{
					throw new DataStoreException("Couldn't find the database - had: " + path);
				}
			}
			catch (DataStoreException e)
			{
				throw e;
			}
			catch (Exception ex)
			{
				logger.error("Unexpected error opening the DB", ex);
				throw new DataStoreException("Couldn't open the local db");
			}
		}
		else
		{
			try
			{
				logger.info("Opening a connection to a REST WB Data Store");
				TccRestClient.setup("http://localhost:8080/terminology/rest/");
				dataStore_ = TccRestClient.getRestClient();
			}
			catch (Exception ex)
			{
				logger.error("Unexpected error opening the remote connection", ex);
				throw new DataStoreException("Couldn't open the remote connection");
			}
		}
		logger.info("Data store ready");
	}

	public static void shutdown()
	{
		logger.info("WB Datastore Shutdown called");
		if (instance_ != null)
		{
			if (instance_.dataStore_ != null && instance_.dataStore_ instanceof BdbTerminologyStore)
			{
				try
				{
					ExtendedAppContext.getDataStore().shutdown();
				}
				catch (Exception e)
				{
					instance_.dataStore_ = null;
					logger.error("Error shutting down the DB", e);
					throw new DataStoreException("Unexpected error shutting down the DB");
				}
			}
			instance_.dataStore_ = null;
		}
		logger.info("WB Datastore Shutdown completed");
	}

	public static TerminologyStoreDI Ts()
	{
		return WBDataStore.getInstance().dataStore_;
	}
	
	public static FxTerminologyStoreDI FxTs()
	{
		//make sure data store has been inited first
		getInstance();
		return AppContext.getService(FxTerminologyStoreDI.class);
	}

	/**
	 * Logs an error and returns no results if a local database is not available.  Otherwise, returns results sorted by score.
	 */
	public static SearchHandle prefixSearch(String query, int sizeLimit, TaskCompleteCallback callback, Integer taskId)
	{
		return SearchHandler.descriptionSearch(query, sizeLimit, true, callback, taskId, null, null, true, null);
	}

	/**
	 * Logs an error and returns no results if a local database is not available.  Otherwise, returns results sorted by score.
	 */
	public static SearchHandle descriptionSearch(String query, TaskCompleteCallback callback)
	{
		return SearchHandler.descriptionSearch(query, Integer.MAX_VALUE, false, callback, null, null, null, true, null);
	}
}
