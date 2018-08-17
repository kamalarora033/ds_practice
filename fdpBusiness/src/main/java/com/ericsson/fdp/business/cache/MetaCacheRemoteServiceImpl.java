package com.ericsson.fdp.business.cache;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Clustered;

import com.ericsson.fdp.core.cache.service.CacheRemoteService;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPMetaCacheProducer;
import com.ericsson.fdp.dao.dto.FDPCacheRequest;

@Stateless
@Clustered
public class MetaCacheRemoteServiceImpl implements CacheRemoteService {

	/** The meta cache producer. */
	@EJB(mappedName = "java:app/fdpCoreServices-1.0/FDPMetaCacheProducer")
	private FDPMetaCacheProducer metaCacheProducer;

	@Override
	public void pushToQueue(final FDPCacheRequest fdpCacheRequest) {
		metaCacheProducer.pushToQueue(fdpCacheRequest);

	}

}
