package gov.va.legoEdit.storage;

/**
 * 
 * IteratorClosedException
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class IteratorClosedException extends RuntimeException
{
	private static final long serialVersionUID = -6837709385047808197L;

	public IteratorClosedException()
    {
        super();
    }

    public IteratorClosedException(String message)
    {
        super(message);
    }
}
