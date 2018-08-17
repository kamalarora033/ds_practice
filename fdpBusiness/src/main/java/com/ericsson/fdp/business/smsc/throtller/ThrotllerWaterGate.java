package com.ericsson.fdp.business.smsc.throtller;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Route;
import org.apache.camel.impl.RoutePolicySupport;
import org.apache.camel.util.CamelLogger;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;

/**
 * A throttle based {@link org.apache.camel.spi.RoutePolicy} which is capable of
 * dynamic throttling a route based on number of current inflight exchanges.
 * <p/>
 * This implementation supports two scopes {@link ThrottlingScope#Context} and
 * {@link ThrottlingScope#Route} (is default). If context scope is selected then
 * this implementation will use a {@link org.apache.camel.spi.EventNotifier} to
 * listen for events when {@link Exchange}s is done, and trigger the
 * {@link #throttle(org.apache.camel.Route, org.apache.camel.Exchange)} method.
 * If the route scope is selected then <b>no</b>
 * {@link org.apache.camel.spi.EventNotifier} is in use, as there is already a
 * {@link org.apache.camel.spi.Synchronization} callback on the current
 * {@link Exchange} which triggers the
 * {@link #throttle(org.apache.camel.Route, org.apache.camel.Exchange)} when the
 * current {@link Exchange} is done.
 * 
 * @author Ericsson
 */
// @Named
public class ThrotllerWaterGate extends RoutePolicySupport {

	/** The routes. */
	private final Set<Route> routes = new LinkedHashSet<Route>();

	/** The event notifier. */
	private ContextScopedEventNotifier eventNotifier;

	/** The camel context. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The lock. */
	private final Lock lock = new ReentrantLock();

	/** The scope. */
	private ThrottlingScope scope = ThrottlingScope.Route;

	/** The max inflight exchanges. */
	private int maxInflightExchanges;

	/** The max inflight exchanges for circle. */
	private int maxInflightExchangesForCircle;

	/** The resume percent of max. */
	private int resumePercentOfMax = 5;

	/** The resume inflight exchanges. */
	private int resumeInflightExchanges;

	/** The resume inflight exchanges for circle. */
	private int resumeInflightExchangesForCircle;

	/** The logging level. */
	private LoggingLevel loggingLevel = LoggingLevel.INFO;

	/** The logger. */
	private CamelLogger logger;

	/** The application cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/**
	 * The Constant DATA_CACHE. private static final MetaDataRouteCache
	 * ROUTE_DATA_CACHE = MetaDataRouteCache.getInstance();
	 */

	/** The route count. */
	private final ConcurrentHashMap<String, AtomicInteger> routeCount = new ConcurrentHashMap<String, AtomicInteger>();

	/**
	 * Instantiates a new throtller water gate.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return "ThrottlingInflightRoutePolicy[" + maxInflightExchanges + " / " + resumePercentOfMax + "% using scope "
				+ scope + "]";
	}

	/**
	 * Gets the routes.
	 * 
	 * @return the routes
	 */
	public Set<Route> getRoutes() {
		return routes;
	}

	@Override
	public void onInit(final Route route) {
		// we need to remember the routes we apply for
		routes.add(route);
		this.resumeInflightExchanges = Integer.valueOf(PropertyUtils.getProperty("throtller.resumeInflightExchanges"));
		this.resumeInflightExchangesForCircle = Integer.valueOf(PropertyUtils
				.getProperty("throtller.resumeInflightExchangesForCircle"));
	}

	@Override
	public void onExchangeDone(final Route route, final Exchange exchange) {
		// if route scoped then throttle directly
		// as context scoped is handled using an EventNotifier instead
		if (scope == ThrottlingScope.Route) {
			throttle(route, exchange);
		}
	}

	/**
	 * Throttles the route when {@link Exchange}s is done and check all incoming
	 * rate of the MO messages. When incoming rate gets exceeded by its normal
	 * rate then it stops the consumer. As soon as incoming MO rate gets normal
	 * then it simply resume the route.
	 * 
	 * @param route
	 *            the route
	 * @param exchange
	 *            the exchange
	 */
	@SuppressWarnings("unchecked")
	protected void throttle(final Route route, final Exchange exchange) {
		// this works the best when this logic is executed when the exchange is
		// done

		final Consumer consumer = route.getConsumer();
		final String routeId = route.getId();

		if ((routeId.startsWith(BusinessConstants.MAIN_ROUTE) && routeId.endsWith(BusinessConstants.ROUTE_RX))
				|| (routeId.startsWith(BusinessConstants.ROUTE_STR) && routeId.endsWith(BusinessConstants.ROUTE_RX))) {

			if (routeId.startsWith(BusinessConstants.MAIN_ROUTE) && routeId.endsWith(BusinessConstants.ROUTE_RX)) {

				add(exchange, routeId);
			}

			final String circleId = (String) exchange.getIn().getHeader(BusinessConstants.CIRCLE_ID);
			if (routeId.startsWith(BusinessConstants.ROUTE_STR) && routeId.endsWith(BusinessConstants.ROUTE_RX)
					&& routeId.contains(circleId)) {

				add(exchange, BusinessConstants.ROUTE_STR + circleId + BusinessConstants.ROUTE_RX);

			}
			final FDPAppBag appBag = new FDPAppBag();
			appBag.setSubStore(AppCacheSubStore.THROTTLER_DATA_EXCHANGES);
			appBag.setKey(BusinessConstants.THROTTLER_DATA_EXCHANGES_MAP);

			final Map<String, AtomicInteger> countMap = (ConcurrentHashMap<String, AtomicInteger>) applicationConfigCache
					.getValue(appBag);

			if (countMap != null) {
				int size = getLatestSizeForRoute(countMap, routeId);
				boolean stop = false;
				if (routeId.startsWith(BusinessConstants.MAIN_ROUTE) && routeId.endsWith(BusinessConstants.ROUTE_RX)) {
					stop = maxInflightExchanges > 0 && size > maxInflightExchanges;
				}
				if (routeId.startsWith(BusinessConstants.ROUTE_STR) && routeId.endsWith(BusinessConstants.ROUTE_RX)
						&& routeId.contains(circleId)) {
					stop = maxInflightExchangesForCircle > 0 && size > maxInflightExchangesForCircle;
				}
				if (log.isTraceEnabled()) {
					log.trace("{} > 0 && {} > {} evaluated as {}", new Object[] { maxInflightExchanges, size,
							maxInflightExchanges, stop });
				}
				if (stop) {
					try {
						lock.lock();
						stopConsumer(size, consumer);
					} catch (final Exception e) {
						handleException(e);
					} finally {
						lock.unlock();
					}
				}

				// reload size in case a race condition with too many at once
				// being
				// invoked
				// so we need to ensure that we read the most current size and
				// start
				// the consumer if we are already to low

				size = getLatestSizeForRoute(countMap, routeId);
				boolean start = false;

				if (routeId.startsWith(BusinessConstants.MAIN_ROUTE) && routeId.endsWith(BusinessConstants.ROUTE_RX)) {
					start = maxInflightExchanges > 0 && size > maxInflightExchanges;
					start = size <= resumeInflightExchanges;
				}
				if (routeId.startsWith(BusinessConstants.ROUTE_STR) && routeId.endsWith(BusinessConstants.ROUTE_RX)
						&& routeId.contains(circleId)) {
					start = maxInflightExchangesForCircle > 0 && size > maxInflightExchangesForCircle;
					start = size <= resumeInflightExchangesForCircle;
				}

				if (log.isTraceEnabled()) {
					log.trace("{} <= {} evaluated as {}", new Object[] { size, resumeInflightExchanges, start });
				}
				if (start) {
					try {
						startConsumer(size, consumer);
					} catch (final Exception e) {
						handleException(e);
					} finally {
						remove(exchange, routeId);
					}
				}
			}
		}
	}

	/**
	 * Gets the latest size for route.
	 * 
	 * @param countMap
	 *            the count map
	 * @param routeId
	 *            the route id
	 * @return the latest size for route
	 */
	private int getLatestSizeForRoute(final Map<String, AtomicInteger> countMap, final String routeId) {
		AtomicInteger size = new AtomicInteger(0);
		if (countMap.containsKey(routeId)) {
			size = countMap.get(routeId);
		}
		return size.get();
	}

	/**
	 * This method is used to put the routeId in map if it absent and initialize
	 * by zero(0) , Also increases the count by one on latest value of
	 * routeId(as key).
	 * 
	 * @param exchange
	 *            contains Exchange Object reference.
	 * @param routeId
	 *            the route id.
	 */
	public void add(final Exchange exchange, final String routeId) {
		final AtomicInteger existing = routeCount.putIfAbsent(routeId, new AtomicInteger(1));
		final FDPAppBag appBag = new FDPAppBag();
		if (existing != null) {
			existing.incrementAndGet();
			routeCount.put(routeId, existing);
			appBag.setSubStore(AppCacheSubStore.THROTTLER_DATA_EXCHANGES);
			appBag.setKey(BusinessConstants.THROTTLER_DATA_EXCHANGES_MAP);
			applicationConfigCache.putValue(appBag, routeCount);
		} else {
			routeCount.put(routeId, new AtomicInteger(1));
			appBag.setSubStore(AppCacheSubStore.THROTTLER_DATA_EXCHANGES);
			appBag.setKey(BusinessConstants.THROTTLER_DATA_EXCHANGES_MAP);
			applicationConfigCache.putValue(appBag, routeCount);
		}
	}

	/**
	 * On the invocation of this method it simply decreases the count of routeId
	 * in Map.
	 * 
	 * @param exchange
	 *            contains Exchange Object reference.
	 * @param routeId
	 *            the route id.
	 */
	public void remove(final Exchange exchange, final String routeId) {
		final AtomicInteger existing = routeCount.get(routeId);
		final FDPAppBag appBag = new FDPAppBag();
		if (existing != null) {
			existing.decrementAndGet();
			routeCount.put(routeId, existing);
			appBag.setSubStore(AppCacheSubStore.THROTTLER_DATA_EXCHANGES);
			appBag.setKey(BusinessConstants.THROTTLER_DATA_EXCHANGES_MAP);
			applicationConfigCache.putValue(appBag, routeCount);
		}
	}

	/**
	 * Gets the max inflight exchanges.
	 * 
	 * @return the max inflight exchanges
	 */
	public int getMaxInflightExchanges() {
		return maxInflightExchanges;
	}

	/**
	 * Sets the upper limit of number of concurrent inflight exchanges at which
	 * point reached the throttler should suspend the route.
	 * <p/>
	 * Is default 1000.
	 * 
	 * @param maxInflightExchanges
	 *            the upper limit of concurrent inflight exchanges
	 */
	public void setMaxInflightExchanges(final int maxInflightExchanges) {
		this.maxInflightExchanges = maxInflightExchanges;
		// recalculate, must be at least at 1
		this.resumeInflightExchanges = Math.max(resumePercentOfMax * maxInflightExchanges / 100, 1);
	}

	/**
	 * Gets the resume percent of max.
	 * 
	 * @return the resume percent of max
	 */
	public int getResumePercentOfMax() {
		return resumePercentOfMax;
	}

	/**
	 * Sets at which percentage of the max the throttler should start resuming
	 * the route.
	 * <p/>
	 * Will by default use 70%.
	 * 
	 * @param resumePercentOfMax
	 *            the percentage must be between 0 and 100
	 */
	public void setResumePercentOfMax(final int resumePercentOfMax) {
		if (resumePercentOfMax < 0 || resumePercentOfMax > 100) {
			throw new IllegalArgumentException("Must be a percentage between 0 and 100, was: " + resumePercentOfMax);
		}

		this.resumePercentOfMax = resumePercentOfMax;
		// recalculate, must be at least at 1
		this.resumeInflightExchanges = Math.max(resumePercentOfMax * maxInflightExchanges / 100, 1);
	}

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	public ThrottlingScope getScope() {
		return scope;
	}

	/**
	 * Sets which scope the throttling should be based upon, either route or
	 * total scoped.
	 * 
	 * @param scope
	 *            the scope
	 */
	public void setScope(final ThrottlingScope scope) {
		this.scope = scope;
	}

	/**
	 * Gets the logging level.
	 * 
	 * @return the logging level
	 */
	public LoggingLevel getLoggingLevel() {
		return loggingLevel;
	}

	/**
	 * Gets the logger.
	 * 
	 * @return the logger
	 */
	public CamelLogger getLogger() {
		if (logger == null) {
			logger = createLogger();
		}
		return logger;
	}

	/**
	 * Sets the logger to use for logging throttling activity.
	 * 
	 * @param logger
	 *            the logger
	 */
	public void setLogger(final CamelLogger logger) {
		this.logger = logger;
	}

	/**
	 * Sets the logging level to report the throttling activity.
	 * <p/>
	 * Is default <tt>INFO</tt> level.
	 * 
	 * @param loggingLevel
	 *            the logging level
	 */
	public void setLoggingLevel(final LoggingLevel loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	/**
	 * Creates the logger.
	 * 
	 * @return the camel logger
	 */
	protected CamelLogger createLogger() {
		return new CamelLogger(LoggerFactory.getLogger(ThrotllerWaterGate.class), getLoggingLevel());
	}

	/**
	 * Gets the size.
	 * 
	 * @param size
	 *            the size
	 * @param consumer
	 *            the consumer
	 * @return the size
	 * @throws Exception
	 *             the exception
	 */
	/*
	 * private int getSize(final Route route, final Exchange exchange) { if
	 * (scope == ThrottlingScope.Context) { return
	 * exchange.getContext().getInflightRepository().size(); } else { return
	 * exchange.getContext().getInflightRepository().size(route.getId()); } }
	 */

	/**
	 * Start consumer.
	 * 
	 * @param size
	 *            the size
	 * @param consumer
	 *            the consumer
	 * @throws Exception
	 *             the exception
	 */
	private void startConsumer(final int size, final Consumer consumer) throws Exception {
		final boolean started = super.startConsumer(consumer);
		if (started) {
			getLogger().log(
					"Throttling consumer: " + size + " <= " + resumeInflightExchanges
							+ " inflight exchange by resuming consumer: " + consumer);
		}
	}

	/**
	 * Stop consumer.
	 * 
	 * @param size
	 *            the size
	 * @param consumer
	 *            the consumer
	 * @throws Exception
	 *             the exception
	 */
	private void stopConsumer(final int size, final Consumer consumer) throws Exception {
		final boolean stopped = super.stopConsumer(consumer);
		if (stopped) {
			getLogger().log(
					"Throttling consumer: " + size + " > " + maxInflightExchanges
							+ " inflight exchange by suspending consumer: " + consumer);
		}
	}

	@Override
	protected void doStart() throws Exception {
		ObjectHelper.notNull(cdiCamelContextProvider, "CamelContext", this);
		if (scope == ThrottlingScope.Context) {
			eventNotifier = new ContextScopedEventNotifier(this);
			// must start the notifier before it can be used
			ServiceHelper.startService(eventNotifier);
			// we are in context scope, so we need to use an event notifier to
			// keep track
			// when any exchanges is done on the camel context.
			// This ensures we can trigger accordingly to context scope
			cdiCamelContextProvider.getContext().getManagementStrategy().addEventNotifier(eventNotifier);
		}
	}

	@Override
	protected void doStop() throws Exception {
		ObjectHelper.notNull(cdiCamelContextProvider, "CamelContext", this);
		if (scope == ThrottlingScope.Context) {
			cdiCamelContextProvider.getContext().getManagementStrategy().removeEventNotifier(eventNotifier);
		}
	}

}
