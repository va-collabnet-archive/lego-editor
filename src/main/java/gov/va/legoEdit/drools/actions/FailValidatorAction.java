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
		DISCERNIBLE_OBSERVABLE("The Discernible Concept must be a descendant of 'Observable entity (observable entity)'"), 
		OBSERVABLE_CHARACTERIZES_PROCESS_REL_MESSAGE("The Source of a Relationship which is a descendant of 'Observable entity (observable entity)'" 
				+ " with a 'CHARACTERIZES (attribute)' Rel type must have a descendant of 'Process (observable entity)' as the Target"), 
		PROCEDURE_METHOD_ACTION_REL_MESSAGE("The Source of a relationship which is a descendant of 'Procedure (procedure)'"
				+ " with a 'Method (attribute)' rel type must have a descendant of 'Action (qualifier value)' as the Target");

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
