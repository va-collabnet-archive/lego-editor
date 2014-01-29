package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.gui.sctSearch.SnomedSearchResult;
import java.util.Collection;

/**
 * 
 * SnomedSearchHandle
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class SnomedSearchHandle
{
	private Collection<SnomedSearchResult> result_;
	private volatile boolean cancelled = false;
	private long searchStartTime = System.currentTimeMillis();
	private Exception error = null;

	/**
	 * Blocks until the results are available....
	 * 
	 * @return
	 * @throws Exception
	 */
	public Collection<SnomedSearchResult> getResults() throws Exception
	{
		if (result_ == null)
		{
			synchronized (SnomedSearchHandle.this)
			{
				while (result_ == null && error == null && !cancelled)
				{
					try
					{
						SnomedSearchHandle.this.wait();
					}
					catch (InterruptedException e)
					{
						// noop
					}
				}
			}
		}
		if (error != null)
		{
			throw error;
		}
		return result_;
	}

	protected void setResults(Collection<SnomedSearchResult> results)
	{
		synchronized (SnomedSearchHandle.this)
		{
			result_ = results;
		}
	}

	protected void setError(Exception e)
	{
		synchronized (SnomedSearchHandle.this)
		{
			this.error = e;
		}
	}

	public long getSearchStartTime()
	{
		return searchStartTime;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}

	public void cancel()
	{
		this.cancelled = true;
	}
}
