import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.util.ServerRunner;


/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
public class HelloServer extends NanoHTTPD {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(HelloServer.class.getName());

    public static void main(String[] args) {
        ServerRunner.run(HelloServer.class);
    }

    public HelloServer() {
        super(8085);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        String msg;
        HelloServer.LOG.info(method + " '" + uri + "' ");
        if (method.toString().equals("GET") && uri.equals("/build")) {
            Map<String, String> parms = session.getParms();
            msg = "<html><body><h1>Hello. Building of a report with " + parms.get("reportId") + "id starts.</h1></body></html>\n";

            CompletableFuture.runAsync(() -> {
                try {
                    String id = parms.get("reportId");
                    Thread.sleep(5000); // LOCK

                    String url = "http://localhost:8080/";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Tested Server");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    String msg2= "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"" +
                            "xmlns:wsdl=\"http://www.pleutre.org/mysample\"" +
                            "xmlns:xd=\"http://www.w3.org/2000/09/xmldsig#\">" +
                            "<soap:Header/>" +
                            "<soap:Body>" +
                            "<wsdl:MySampleResponse>" +
                            "<wsdl:FunctionalIdentifier>"+ id +"</wsdl:FunctionalIdentifier>" +
                            "<wsdl:StatusMessage>VALID</wsdl:StatusMessage>" +
                            "</wsdl:MySampleResponse>" +
                            "</soap:Body>" +
                            "</soap:Envelope>";
                    con.setRequestProperty("content-length",Integer.toString(msg2.length()));


                    // Send post request
                    con.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter( con.getOutputStream() );
                    writer.write( msg2 );
                    writer.flush();
                    writer.close();

                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'POST' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                    System.out.println(response.toString());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            return Response.newFixedLengthResponse(msg);
        }
        else
        {
            msg = "<html><body><h1>Hello too, but its wrong page or method</h1></body></html>\n";
            return Response.newFixedLengthResponse(msg);
        }
    }
}
