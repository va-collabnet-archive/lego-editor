package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import java.io.File;
import org.ihtsdo.bdb.BdbTerminologyStore;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.tk.rest.client.TtkRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
			synchronized (BDBDataStoreImpl.class)
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
				String directory = "wb-berkeley-db";

				if (new File(directory).exists())
				{
					Ts.setup(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS, directory);
				}
				else
				{
					throw new DataStoreException("Couldn't find the database");
					// Ts.setup(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS, directory);
					// System.out.println("Start load of eConcept.jbin");
					//
					// File[] econFiles = new File[] { new File("/Users/kec/NetBeansProjects/eConcept.jbin") };
					//
					// Ts.get().loadEconFiles(econFiles);
					// System.out.println("Finished load of eConcept.jbin");
				}
				Ts.get().setGlobalSnapshot(Ts.get().getSnapshot(StandardViewCoordinates.getSnomedLatest()));
				dataStore_ = Ts.get();
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
				TtkRestClient.setup("http://localhost:8080/terminology/rest/");
				dataStore_ = TtkRestClient.getRestClient();
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
    				Ts.close();
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
}
