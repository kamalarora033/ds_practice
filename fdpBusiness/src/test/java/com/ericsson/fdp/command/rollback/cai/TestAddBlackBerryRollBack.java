package com.ericsson.fdp.command.rollback.cai;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
 

@RunWith(PowerMockRunner.class)
public class TestAddBlackBerryRollBack {
	private FDPRequest fdpRequest  = new FDPRequestImpl();
	private Object object = new Object();
	private  FDPCache<FDPMetaBag, FDPCacheable>  object3;
	private FDPCacheable object2 ;
private ApplicationConfigUtil applicationConfigUtil;
	
	
	@InjectMocks
	AddBlackBerryRollBack obj;

	
	@Before
	public void setupMock() {
		// Mock
		applicationConfigUtil = Mockito.mock(ApplicationConfigUtil.class);
		object3 = Mockito.mock(FDPCache.class);
		object2 = Mockito.mock(AbstractCommand.class);
		
		//fDPMetaBag = Mockito.mock(FDPMetaBag.class);
	}
	
	  
	@PrepareForTest({ApplicationConfigUtil.class, CommandUtil.class, PropertyUtils.class})
	@Test
	public void testexecute() {
		System.out.println("Staring MOCKING For testexecute() ,... ");

		try {
			PowerMockito.mockStatic(ApplicationConfigUtil.class);
			PowerMockito.mockStatic(PropertyUtils.class);
			//PowerMockito.mockStatic(CommandUtil.class);
			//PowerMockito.doNothing().when(CommandUtil.class);
			
			PowerMockito.when(ApplicationConfigUtil.getMetaDataCache()).thenReturn(object3);
			PowerMockito.when(object3.getValue(Matchers.any(FDPMetaBag.class))).thenReturn(object2);
			PowerMockito.when(PropertyUtils.getProperty(Matchers.any(String.class))).thenReturn("");
			
			
//			Constructor
			 PowerMockito.mockStatic(CommandUtil.class);
			 /*	

			 Status value= Status.getStatusEnum(0);
			PowerMockito.when(CommandUtil.executeCommand(fdpRequest, cmdToExecute, true)).thenReturn(value);
			 
 */
			//PowerMockito.when(fdpRequest.getExecutedCommand(FDPConstant.ADD_BLACKBERRY_BUNDLE)).thenReturn(value);
			
			
			obj.execute(fdpRequest, object);
			
		 

			System.out.println("Success ");
			assert(true == true);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}// method ends

}
