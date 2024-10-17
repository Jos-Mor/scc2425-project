package scc.serverless;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class HttpFunction {
	private static final String TEXT = "text";
	private static final String HTTP_TRIGGER_NAME="req";
	private static final String HTTP_FUNCTION_NAME="HttpExample";
	private static final String HTTP_TRIGGER_ROUTE="serverless/echo/{" + TEXT + "}";
	
	@FunctionName(HTTP_FUNCTION_NAME)
    public HttpResponseMessage run(
            @HttpTrigger(
                name = HTTP_TRIGGER_NAME,
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = HTTP_TRIGGER_ROUTE)
                HttpRequestMessage<Optional<String>> request,
                @BindingName(TEXT) String text,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + text + System.getenv().toString()).build();
    }
}
