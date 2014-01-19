package com.a1systems.http.adapter.smpp.client;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

public class ElinkTask implements Runnable {
	public static final org.slf4j.Logger log = LoggerFactory.getLogger(ElinkTask.class);

	protected Client client;

	public ElinkTask(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		if (client.state == ClientState.BOUND) {
			SmppSession session = client.getSession();

			log.debug("Send elink");
            try {
                session.sendRequestPdu(new EnquireLink(), TimeUnit.SECONDS.toMillis(10), false);
            } catch (RecoverablePduException ex) {
                log.debug("{}", ex);
            } catch (UnrecoverablePduException ex) {
                log.debug("{}", ex);
            } catch (SmppTimeoutException ex) {
                log.debug("{}", ex);
            } catch (SmppChannelException ex) {
                log.debug("{}", ex);
            } catch (InterruptedException ex) {
                log.debug("{}", ex);
            }

		}
	}

}
