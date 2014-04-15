package com.a1systems.smpp.multiplexer.client;

import com.cloudhopper.smpp.SmppClient;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.type.SmppBindException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RebindTask implements Runnable {

    public static final Logger log = LoggerFactory.getLogger(RebindTask.class);

    protected Client client;

    public RebindTask(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        if (client.isBinding()) {
            SmppClient smppClient = client.getSmppClient();
            try {
                SmppSessionConfiguration cfg = client.getCfg();

                log.debug("Try to bind host:[{}:{}] Credentials:[{}]:[{}]", cfg.getHost(), cfg.getPort(), cfg.getSystemId(), cfg.getPassword());

                SmppSession session = smppClient.bind(client.getCfg(), client.getSessionHandler());

                client.bound(session);
            } catch (SmppTimeoutException ex) {
                log.error("{}", ex.getMessage());
            } catch (SmppChannelException ex) {
                log.error("{}", ex.getMessage());
            } catch (SmppBindException ex) {
                log.error("{}", ex.getMessage());
            } catch (UnrecoverablePduException ex) {
                log.error("{}", ex.getMessage());
            } catch (InterruptedException ex) {
                log.error("{}", ex.getMessage());
            }
        }
    }

}
