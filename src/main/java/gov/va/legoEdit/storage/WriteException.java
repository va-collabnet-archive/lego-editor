package gov.va.legoEdit.storage;

/**
 *
 * @author darmbrust
 */
public class WriteException extends Exception
{

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
