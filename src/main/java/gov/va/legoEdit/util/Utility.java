package gov.va.legoEdit.util;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utility
{
	private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	public static ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS, workQueue, new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			t.setName("Background-Thread-" + t.getId());
			return t;
		}
	});

	public static boolean isLong(String string)
	{
		try
		{
			Long.parseLong(string);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	public static boolean isUUID(String string)
	{
		if (string.length() != 36)
		{
			return false;
		}
		try
		{
			UUID.fromString(string);
			return true;
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
	}

	public static boolean isEmpty(String string)
	{
		if (string == null || string.length() == 0)
		{
			return true;
		}
		return false;
	}

	public static boolean isEqual(String a, String b)
	{
		if (a == null)
		{
			return (b == null ? true : false);
		}
		if (b == null)
		{
			return (a == null ? true : false);
		}
		return a.equals(b);
	}
}
