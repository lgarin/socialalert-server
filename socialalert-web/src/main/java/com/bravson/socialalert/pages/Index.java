package com.bravson.socialalert.pages;

import java.util.Date;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;

import com.bravson.socialalert.common.facade.UserFacade;

/**
 * Start page of application.
 */
public class Index
{
	
//	@Property
//	@ActivationRequestParameter
//	private boolean loginVisible;
//	
//	@Property
//	@ActivationRequestParameter
//	private boolean registerVisible;
//	
	
    @Property
    @Inject
    @Symbol(SymbolConstants.TAPESTRY_VERSION)
    private String tapestryVersion;

    @InjectComponent
    private Zone zone;

    @Persist
    @Property
    private int clickCount;
    
    @Inject
    private Request request; 

    @Inject
    private AlertManager alertManager;
    
    @Inject
    private UserFacade userService;

    public Date getCurrentTime()
    {
        return new Date();
    }

    void onActionFromIncrement()
    {
        alertManager.info("Increment clicked");

        clickCount++;
    }

    Object onActionFromIncrementAjax()
    {
        clickCount++;

        alertManager.info("Increment (via Ajax) clicked");

        return zone;
    }
    
   
//	public Object showLogin() {
//		loginVisible = true;
//		registerVisible = false;
//		return this;
//	}
}
