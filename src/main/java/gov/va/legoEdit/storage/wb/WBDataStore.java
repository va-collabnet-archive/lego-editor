package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.sctSearch.SnomedSearchResult;
import gov.va.legoEdit.gui.sctSearch.SnomedSearchResultComparator;
import gov.va.legoEdit.gui.util.TaskCompleteCallback;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.util.Utility;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.ihtsdo.bdb.BdbTerminologyStore;
import org.ihtsdo.cc.lucene.LuceneManager;
import org.ihtsdo.cc.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.rest.client.TtkRestClient;
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
	public static File bdbFolderPath = new File("wb-berkeley-db");
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

				if (bdbFolderPath.exists())
				{
					Ts.setup(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS, bdbFolderPath.getAbsolutePath());
				}
				else
				{
					if (LegoGUI.getInstance() != null)
					{
						LegoGUI.getInstance()
								.showErrorDialog(
										"No Snomed Database",
										"The Snomed Database was not found.",
										"Please download the file "
												+ System.getProperty("line.separator")
												+ System.getProperty("line.separator")
												+ "https://csfe.aceworkspace.net/sf/frs/do/downloadFile/projects.veterans_administration_project/frs.lego_editor.snomed_database_0_56_and_newer/frs4344?dl=1"
												+ System.getProperty("line.separator") + System.getProperty("line.separator") + " and unzip it into "
												+ System.getProperty("line.separator") + new File("").getAbsolutePath() + System.getProperty("line.separator")
												+ " and then restart the editor.");
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
	 * Logs an error and returns no results if a local database is not available.  Otherwise, returns results sorted by score.
	 */
	public static SnomedSearchHandle prefixSearch(String query, int sizeLimit, TaskCompleteCallback callback, Integer taskId)
	{
		return search(query, sizeLimit, true, callback, taskId);
	}

	/**
	 * Logs an error and returns no results if a local database is not available.  Otherwise, returns results sorted by score.
	 */
	public static SnomedSearchHandle descriptionSearch(String query, TaskCompleteCallback callback)
	{
		return search(query, Integer.MAX_VALUE, false, callback, null);
	}

	private static SnomedSearchHandle search(String query, final int resultLimit, final boolean prefixSearch, final TaskCompleteCallback callback, final Integer taskId)
	{
		final SnomedSearchHandle ssh = new SnomedSearchHandle();

		if (!prefixSearch)
		{
			// Just strip out parens, which are common in FSNs, but also lucene search operators (which our users likely won't use)
			query = query.replaceAll("\\(", "");
			query = query.replaceAll("\\)", "");
		}

		final String localQuery = query;
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				HashMap<Integer, SnomedSearchResult> tempUserResults = new HashMap<>();
				try
				{
					if (localQuery.length() > 0)
					{
						if (Utility.isUUID(localQuery) || Utility.isLong(localQuery))
						{
							ConceptVersionBI temp = WBUtility.lookupSnomedIdentifierAsCV(localQuery);
							if (temp != null)
							{
								SnomedSearchResult sr = new SnomedSearchResult(temp.getConceptNid(), 2.0f);
								sr.addMatchingString(localQuery);
								tempUserResults.put(temp.getConceptNid(), sr);
							}
						}

						if (WBDataStore.getInstance().dataStore_ instanceof BdbTerminologyStore)
						{
							logger.debug("Lucene Search: '" + localQuery + "'");
							// sort of copied from Termstore.searchLucene(...)
							// because that API throws away the scores, and returns the results in random order... which is rather useless.
							
							Query q = null;
							if (prefixSearch)
							{
								q = buildQuery(localQuery, new StandardAnalyzer(Version.LUCENE_40));
							}
							else
							{
								q = new QueryParser(Version.LUCENE_40, "desc", new StandardAnalyzer(Version.LUCENE_40)).parse(localQuery);
							}
							SearchResult searchResults = LuceneManager.search(q);

							if (searchResults.topDocs.totalHits == 0)
							{
								if (prefixSearch)
								{
									q = buildQuery(localQuery, new WhitespaceAnalyzer(Version.LUCENE_40));
								}
								else
								{
									q = new QueryParser(Version.LUCENE_40, "desc", new WhitespaceAnalyzer(Version.LUCENE_40)).parse(localQuery);
								}
								searchResults = LuceneManager.search(q);
							}

							BdbTerminologyStore bts = (BdbTerminologyStore) WBDataStore.getInstance().dataStore_;
							for (int i = 0; i < searchResults.topDocs.totalHits; i++)
							{
								if (ssh.isCancelled())
								{
									break;
								}
								
								Document doc = searchResults.searcher.doc(searchResults.topDocs.scoreDocs[i].doc);
								ComponentChroncileBI<?> cc = bts.getComponent(Integer.parseInt(doc.get("dnid")));

								SnomedSearchResult sr = tempUserResults.get(cc.getConceptNid());
								if (sr == null)
								{
									//normalize the scores between 0 and 1
									sr = new SnomedSearchResult(cc.getConceptNid(), searchResults.topDocs.scoreDocs[i].score / searchResults.topDocs.getMaxScore() );
									tempUserResults.put(cc.getConceptNid(), sr);
								}
								
								String matchingString = null;
								
								if (cc instanceof DescriptionAnalogBI)
								{
									matchingString = ((DescriptionAnalogBI<?>) cc).getText();
								}
								else
								{
									logger.error("Unexpected type returned from search: " + cc.getClass().getName());
									matchingString = "oops";
								}
								
								sr.addMatchingString(matchingString);
								
								//add one to the scores when we are doing a prefix search, and it hits.
								if (prefixSearch && sr.getBestScore() <= 1.0f)
								{
									float adjustValue = 0f;
									//exact match, bump by 2
									if (matchingString.toLowerCase().equals(localQuery.trim().toLowerCase()))
									{
										adjustValue = 2.0f;
									}
									else if (matchingString.toLowerCase().startsWith(localQuery.trim().toLowerCase()))
									{
										//add 1, plus a bit more boost based on the length of the matches (shorter matches get more boost)
										adjustValue = 1.0f + (1.0f - ((float)(matchingString.length() - localQuery.trim().length()) / (float)matchingString.length()));
									}
									
									if (adjustValue > 0f)
									{
										sr.adjustScore(sr.getBestScore() + adjustValue);
									}
								}
							}
						}
						else
						{
							logger.warn("Lucene search is not available - no local database!");
						}
					}

					//Now, sort the results.
					ArrayList<SnomedSearchResult> userResults = new ArrayList<>(tempUserResults.size());
					userResults.addAll(tempUserResults.values());
					Collections.sort(userResults, new SnomedSearchResultComparator());
					if (userResults.size() > resultLimit)
					{
						ssh.setResults(userResults.subList(0,  resultLimit - 1));
					}
					else
					{
						ssh.setResults(userResults);
					}
				}
				catch (Exception e)
				{
					logger.error("Unexpected error during lucene search", e);
					ssh.setError(e);
				}
				callback.taskComplete(ssh.getSearchStartTime(), taskId);
			}
		};
		Utility.tpe.execute(r);
		return ssh;
	}
	
	private static Query buildQuery(String searchString, Analyzer analyzer) throws IOException
	{
		StringReader textReader = new StringReader(searchString);
		TokenStream tokenStream = analyzer.tokenStream("desc", textReader);
		tokenStream.reset();
		List<String> terms = new ArrayList<>();
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		
		while (tokenStream.incrementToken())
		{
			terms.add(charTermAttribute.toString());
		}
		textReader.close();
		tokenStream.close();
		analyzer.close();
		
		BooleanQuery bq = new BooleanQuery();
		if (terms.size() > 0 && !searchString.endsWith(" "))
		{
			String last = terms.remove(terms.size() - 1);
			bq.add(new PrefixQuery((new Term("desc", last))), Occur.MUST);
		}
		for (String s : terms)
		{
			bq.add(new TermQuery(new Term("desc", s)), Occur.MUST);
		}
		
		return bq;
	}
}
