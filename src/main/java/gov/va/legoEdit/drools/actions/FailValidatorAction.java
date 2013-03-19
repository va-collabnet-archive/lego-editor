package gov.va.legoEdit.drools.actions;


/**
 * 
 * @author jefron
 * @author darmbrust
 */
public class FailValidatorAction extends DroolsLegoAction
{
	public enum ReasonFailed
	{
		DISCERNIBLE_OBSERVABLE("Discernible Concept must be an Observable");
		
		private String reason;
		private ReasonFailed(String reason)
		{
			this.reason = reason;
		}
		
		public String getReason()
		{
			return reason;
		}
	};

	private ReasonFailed reasonFailed;
	
	public FailValidatorAction(ReasonFailed reason)
	{
		this.reasonFailed = reason;
	}
	
	public String getFailureReason()
	{
		return reasonFailed.getReason();
	}
}
