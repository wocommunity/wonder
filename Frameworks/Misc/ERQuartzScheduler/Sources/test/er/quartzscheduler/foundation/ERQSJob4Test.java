package er.quartzscheduler.foundation;


public class ERQSJob4Test extends ERQSJob {

	public boolean isExecuteMethodCalled = false, isWillDeleteMethodCalled = false,  isWillSaveMethodCalled = false,  isValidateForDeleteMethodCalled = false,  isValidateForSaveMethodCalled = false;
	@Override
	protected void _execute() 
	{
		isExecuteMethodCalled = true;
	}

	@Override
	public void willDelete(final ERQSJobDescription jobDescription) 
	{
		isWillDeleteMethodCalled = true;
	}

	@Override
	public void willSave(final ERQSJobDescription jobDescription) 
	{
		isWillSaveMethodCalled = true;
	}

	@Override
	public void validateForDelete(final ERQSJobDescription jobDescription) 
	{
		isValidateForDeleteMethodCalled = true;
	}

	@Override
	public void validateForSave(final ERQSJobDescription jobDescription) 
	{
		isValidateForSaveMethodCalled = true;
	}
}
