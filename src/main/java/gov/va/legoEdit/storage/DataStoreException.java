package gov.va.legoEdit.storage;

/**
 * 
 * DataStoreException
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class DataStoreException extends RuntimeException
{
	private static final long serialVersionUID = 4628690782411628595L;

	public DataStoreException()
    {
        super();
    }

    public DataStoreException(String message)
    {
        super(message);
    }

    public DataStoreException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataStoreException(Throwable cause)
    {
        super(cause);
    }
}
