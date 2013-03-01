package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.ihtsdo.bdb.BdbTerminologyStore;
import org.ihtsdo.cc.lucene.LuceneManager;
import org.ihtsdo.cc.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.tk.rest.client.TtkRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WBDataStore
{
	private static volatile WBDataStore instance_;
	private static Logger logger = LoggerFactory.getLogger(WBDataStore.class);
	public static File bdbFolderPath = new File("wb-berkeley-db");

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

				if (bdbFolderPath.exists())
				{
					Ts.setup(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS, bdbFolderPath.getAbsolutePath());
				}
				else
				{
					if (LegoGUI.getInstance() != null)
					{
						LegoGUI.getInstance().showErrorDialog("No Snomed Database", "The Snomed Database was not found.", 
								"Please download the file " + System.getProperty("line.separator") + System.getProperty("line.separator")
								+ "https://csfe.aceworkspace.net/sf/frs/do/downloadFile/projects.veterans_administration_project/frs.lego_editor.1_13_13/frs3751?dl=1"
								+ System.getProperty("line.separator") + System.getProperty("line.separator") + " and unzip it into " + System.getProperty("line.separator")
								+ new File("").getAbsolutePath() + System.getProperty("line.separator") + " and then restart the editor.");
					}
					throw new DataStoreException("Couldn't find the database - had: " + bdbFolderPath.getAbsolutePath());
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
	
	/**
	 * Returns null if search not available (only works with local DBs) - otherwise, returns the list of descriptions that matched.
	 */
	@SuppressWarnings("deprecation")
    public List<ComponentChroncileBI<?>> descriptionSearch(String query) throws IOException
	{
        if (!(dataStore_ instanceof BdbTerminologyStore))
        {
            return null;
        }
        else
        {
            try
            {
                BdbTerminologyStore bts = (BdbTerminologyStore)dataStore_;
                
                //sort of copied from Termstore.searchLucene(...)
                //because that API throws away the scores, and returns the results in random order... which is rather useless.
                Query q = new QueryParser(LuceneManager.version, "desc", new StandardAnalyzer(LuceneManager.version)).parse(query);
                SearchResult result = LuceneManager.search(q);

                if (result.topDocs.totalHits == 0) 
                {
                    q = new QueryParser(LuceneManager.version, "desc", new WhitespaceAnalyzer()).parse(query);
                }
                
                result = LuceneManager.search(q);
                
                ArrayList<ComponentChroncileBI<?>> resultToReturn = new ArrayList<>(result.topDocs.totalHits);
                for (int i = 0; i < result.topDocs.totalHits; i++) 
                {
                    Document doc  = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
                    resultToReturn.add(bts.getComponent(Integer.parseInt(doc.get("dnid"))));
                }
                return resultToReturn;
            }
            catch (NumberFormatException | ParseException e)
            {
                throw new IOException("Unexpected error during search", e);
            }
        }
	}
}
