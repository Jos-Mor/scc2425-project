package scc.serverless;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class TimerFunction {
	private static final String TIMER_FUNCTION_NAME = "timerFunctionExample";
	private static final String TIMER_TRIGGER_NAME = "timerFunctionTrigger";
	private static final String TIMER_TRIGGER_SCHEDULE = "0 * * * * *";
	
	@FunctionName(TIMER_FUNCTION_NAME)
	public void run(
	  @TimerTrigger(name = TIMER_TRIGGER_NAME, schedule = TIMER_TRIGGER_SCHEDULE) String timerInfo,
	      ExecutionContext context
	 ) {
	     context.getLogger().info("Timer was triggered: " + timerInfo);
	}
}
