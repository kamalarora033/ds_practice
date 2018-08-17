package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.AsyncCommandCacheService;
import com.ericsson.fdp.business.vo.FDPAsycCommandVO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.util.DozerHelper;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.entity.FDPAsync;
import com.ericsson.fdp.dao.fdpadmin.FDPAsyncDAO;

@Stateless(mappedName = "asyncCommandCache")
public class FDPAsyncCommandCacheImpl implements AsyncCommandCacheService{

	private static final Mapper MAPPER = DozerBeanMapperSingletonWrapper.getInstance();
	
	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Inject
	private FDPAsyncDAO fdpAsyncdao;
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		List<FDPAsycCommandVO> asyncvoList=new ArrayList<FDPAsycCommandVO>();
		List<FDPAsync> asyncCommandLst=fdpAsyncdao.getAllAsyncCommands();
		for (Iterator iterator = asyncCommandLst.iterator(); iterator.hasNext();) {
			FDPAsync fdpAsync = (FDPAsync) iterator.next();
			FDPAsycCommandVO fdpAsyncCommandvo=new FDPAsycCommandVO();
			fdpAsyncCommandvo.setAsyncCommandID(fdpAsync.getAsyncCommandID());
	//		fdpAsyncCommandvo.setAsyncHandlerclass(fdpAsync.getAsyncHandlerclass());
			fdpAsyncCommandvo.setCommanddiscplayname(fdpAsync.getFdpcommandname().getCommandNameToDisplay());
			fdpAsyncCommandvo.setNotificationid(fdpAsync.getFdpnotification().getNotificationsId());
			fdpAsyncCommandvo.setTransactionparam(fdpAsync.getTransactionparam());
			fdpAsyncCommandvo.setTransactionparamtype(fdpAsync.getTransactionparamtype());
			fdpAsyncCommandvo.setNotificationstatus(fdpAsync.isNotificationstatus());
			asyncvoList.add(fdpAsyncCommandvo);
		}
	
		
		
		for(FDPAsycCommandVO fdpasync:asyncvoList)
		{
			FDPMetaBag metaBag = new FDPMetaBag(fdpCircle,ModuleType.ASYNC_COMMANDS,fdpasync.getCommanddiscplayname());
			fdpCache.putValue(metaBag, fdpasync);
		}
		return true;
	}

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return ModuleType.ASYNC_COMMANDS;
	}

}
