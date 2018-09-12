package bx.allpay.daily.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import bx.allpay.daily.lambda.Handler_s3;
public class LambdaFunctionHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        Handler_s3 hs3 = new Handler_s3();
        hs3.process();
        return null;
    }

}
