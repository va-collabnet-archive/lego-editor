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
		DISCERNIBLE_OBSERVABLE("Discernible Concept must be an Observable"),
		OBSERVABLE_CHARACTERIZES_PROCESS_REL_MESSAGE("An Observable Source (or desc) with CHARACTERIZES (attribute) Rel Type must have Process (or desc) as Target"),
                PROCEDURE_METHOD_ACTION_REL_MESSAGE("A Procedure Source (or desc) with Method (attribute) Rel Type must have Action (qualifier value) (or desc) as Target"),
                TEST_CASE("TEST TEST TEST");
		
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
