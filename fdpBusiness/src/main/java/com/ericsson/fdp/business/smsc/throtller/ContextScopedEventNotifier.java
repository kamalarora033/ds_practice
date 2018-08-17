package com.ericsson.fdp.business.smsc.throtller;

import java.util.EventObject;

import org.apache.camel.Route;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.support.EventNotifierSupport;

/**
 * The Class ContextScopedEventNotifier. {@link org.apache.camel.spi.EventNotifier} to keep track on when
 * {@link Exchange} is done, so we can throttle accordingly.
 * 
 * @author Ericsson
 */
public class ContextScopedEventNotifier extends EventNotifierSupport {

	/** The throtller water gate. */
	private final ThrotllerWaterGate throtllerWaterGate;

	/**
	 * Instantiates a new context scoped event notifier.
	 * @param throtllerWaterGate the throtller water gate
	 */
	ContextScopedEventNotifier(final ThrotllerWaterGate throtllerWaterGate) {
		this.throtllerWaterGate = throtllerWaterGate;
	}

	@Override
	public void notify(final EventObject event) throws Exception {
		ExchangeCompletedEvent completedEvent = (ExchangeCompletedEvent) event;
		for (Route route : this.throtllerWaterGate.getRoutes()) {
			this.throtllerWaterGate.throttle(route, completedEvent.getExchange());
		}
	}

	@Override
	public boolean isEnabled(final EventObject event) {
		return event instanceof ExchangeCompletedEvent;
	}

	@Override
	protected void doStart() throws Exception {
		// noop
	}

	@Override
	protected void doStop() throws Exception {
		// noop
	}

	@Override
	public String toString() {
		return "ContextScopedEventNotifier";
	}
}