package gov.va.legoEdit.storage;

/**
 *
 * @author darmbrust
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
