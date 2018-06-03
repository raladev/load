package RTMPSubscriber;

import org.red5.client.net.rtmp.ClientExceptionHandler;
import org.red5.client.net.rtmp.INetStreamEventHandler;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.io.utils.ObjectMap;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Subscriber  {

    private Logger log = LoggerFactory.getLogger(Subscriber.class);
    private final String url;
    private final int port;
    private final String application;
    private final String streamName;
    private int timeout;
    private RTMPClient client;
    public boolean exception = false;

    /**
     * @param url
     * @param port
     * @param application
     * @param streamName
     */
    public Subscriber(String url, int port, String application, String streamName, int timeout) {
        this.url = url;
        this.port = port;
        this.application = application;
        this.streamName = streamName;
        this.timeout = timeout;
    }


    public void Subscribe() {

        client = new RTMPClient();
        client.setServiceProvider(this);

        client.setExceptionHandler(new ClientExceptionHandler() {
            @Override
            public void handleException(Throwable throwable) {
                exception=true;
                throwable.printStackTrace();
            }
        });
        

        client.setStreamEventDispatcher(new IEventDispatcher() {
            @Override
            public void dispatchEvent(IEvent event) {
               // @SuppressWarnings("unused")
                IStreamPacket data = (IStreamPacket) event;
                System.out.println("dispatchEvent: " + event);
                if(event.toString().contains("Video"))
                {
                    System.out.println("dispatchEvent: " + event);
                }
            }
        });


        final INetStreamEventHandler netStreamEventHandler = new INetStreamEventHandler() {
            @Override
            public void onStreamEvent(Notify notify) {
                System.out.println("ClientStream.onStreamEvent: " + notify);
            }
        };
        client.setStreamEventHandler(netStreamEventHandler);


        final IPendingServiceCallback streamCallback = new IPendingServiceCallback() {
            @Override
            public void resultReceived(IPendingServiceCall call) {
                if (call.getServiceMethodName().equals("createStream")) {
                    Number streamId = (Number) call.getResult();
                    // -2: live then recorded, -1: live, >=0: recorded
                    // streamId, streamName, mode, length
                    System.out.println("streamCallback:resultReceived: (streamId " + streamId + ")");
                    client.play(streamId, streamName, -1, 0);

                }
            }
        };
        
        IPendingServiceCallback connectCallback = new IPendingServiceCallback() {
            @Override
            public void resultReceived(IPendingServiceCall call) {
                ObjectMap<?, ?> map = (ObjectMap<?, ?>) call.getResult();
                String code = (String) map.get("code");
                System.out.println("connectCallback: (code " + code + ")");
                // Server connection established, but issue in connection.
                if (StatusCodes.NC_CONNECT_FAILED.equals(code) || StatusCodes.NC_CONNECT_REJECTED.equals(code) || StatusCodes.NC_CONNECT_INVALID_APPLICATION.equals(code)) {
                    log.error("Rejected: " + map.get("description"));
                    exception=true;
                    client.disconnect();
                    }
                // If connection successful, establish a stream
                else if (StatusCodes.NC_CONNECT_SUCCESS.equals(code)) {
                    client.createStream(streamCallback);
                } else {
                    exception=true;
                    log.error("ERROR code:" + code);
                }
            }
        };
        client.connect(url, port, client.makeDefaultConnectionParams(url, port, application), connectCallback);

            try {
                Thread.sleep(timeout*1000);
            } catch (InterruptedException e) {
                exception=true;
                log.error("Exception: " + e);
            }

        client.disconnect();
        System.out.println("End");
    }

    public void onBWCheck(Object params) {
        System.out.println("onBWCheck: " + params);
    }

    /**
     * Called when bandwidth has been configured.
     */
    public void onBWDone(Object params) {
        System.out.println("onBWDone: " + params);
    }

    public void onStatus(Object params) {
        System.out.println("onStatus: " +  params);
        String status = params.toString();
        // if stream is stopped or unpublished
        if (status.indexOf("Stop") != -1 || status.indexOf("UnPublish") != -1) {
            client.disconnect();
        }
    }
}
