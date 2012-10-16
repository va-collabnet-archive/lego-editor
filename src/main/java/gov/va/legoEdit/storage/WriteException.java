package gov.va.legoEdit.storage;

/**
 *
 * @author darmbrust
 */
public class WriteException extends Exception
{
	private static final long serialVersionUID = -7869305643117695385L;

	public WriteException()
    {
        super();
    }

    public WriteException(String message)
    {
        super(message);
    }

    public WriteException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WriteException(Throwable cause)
    {
        super(cause);
    }
}
