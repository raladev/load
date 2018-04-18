package RTMPSubscriber;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Hello world!
 *
 */
public class App implements JavaSamplerClient
{

    @Override
    public void setupTest(JavaSamplerContext javaSamplerContext) {

    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        String url = javaSamplerContext.getParameter("url");
        int port = Integer.parseInt(javaSamplerContext.getParameter("port"));
        String app = javaSamplerContext.getParameter("app");
        String streamName = javaSamplerContext.getParameter("streamName");
        int timeout = Integer.parseInt(javaSamplerContext.getParameter("timeout"));


        SampleResult result = new SampleResult();
        result.sampleStart();
        result.setSampleLabel("Test Sample");
        Subscriber sub = new Subscriber( url, port, app, streamName, timeout);
        sub.Subscribe();

        result.sampleEnd();
        if(sub.exception) {
            result.setResponseMessage("OK");
            result.setSuccessful(true);
        } else {
            result.setResponseMessage("Error. See logs. If the log does not " +
                    "contain information about the error, I forgot about some exceptions and I am really sorry. T_T");
            result.setSuccessful(false);
        }
        return result;
    }

    @Override
    public void teardownTest(JavaSamplerContext javaSamplerContext) {

    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("url","localhost");
        defaultParameters.addArgument("port","1935");
        defaultParameters.addArgument("app","live");
        defaultParameters.addArgument("name","stream");
        defaultParameters.addArgument("timeout","600");

        return defaultParameters;
    }
}
