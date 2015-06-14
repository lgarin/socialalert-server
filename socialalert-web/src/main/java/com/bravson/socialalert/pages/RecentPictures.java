package com.bravson.socialalert.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.SelectModelFactory;

import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.MediaFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(allow={UserRole.USER,UserRole.GUEST})
public class RecentPictures {
	
	@Inject
    private MediaFacade pictureService;
	
	@Inject
	@Symbol("default.max.distance")
	private double defaultMaxDistance;
	
	@Property
	@Persist
	private int pageNumber;
	
	@Property
	QueryResult<MediaInfo> searchResult;
	
	@Persist
	@Property
	private String keywords;

	@Persist
	@Property
	String addressQuery;
	
	@Persist
	List<GeoAddress> addressList;
	
	@Persist
	@Property
	private Double maxDistance;
	
	@Inject 
	private Locale locale;
	
	@InjectComponent("search")
    private Form searchForm;
	
	@InjectComponent("address")
	private TextField addressField;
	
	@Property
	private List<String> addressSelectModel;
	
	@InjectComponent("addressSelect")
	private Select addressSelectField;
	
	@Inject
	private SelectModelFactory selectModelFactory;
	
	@SetupRender
	void setupRender() throws IOException {
		if (addressList != null && addressList.size() > 1) {
			addressSelectModel = new ArrayList<>(addressList.size());
			for (GeoAddress address : addressList) {
				addressSelectModel.add(address.getFormattedAddress());
			}
		} else {
			addressSelectModel = null;
		}
		
		Double longitude = null;
		Double latitude = null;
		if (addressList != null && addressList.size() == 1) {
			GeoAddress address = addressList.get(0);
			longitude = address.getLongitude();
			latitude = address.getLatitude();
		}
		if (maxDistance == null) {
			maxDistance = defaultMaxDistance;
		}
		searchResult = pictureService.searchMedia(MediaType.PICTURE, latitude, longitude, maxDistance, keywords, 360L * DateUtils.MILLIS_PER_DAY, pageNumber, 5);
	}
	
	
	Object onClear() {
		pageNumber = 0;
		addressQuery = null;
		addressList = null;
		maxDistance = defaultMaxDistance;
		keywords = null;
		return this;
	}
	
	void onValidateFromSearch() throws IOException {
		if (addressQuery != null) {
			addressList = Collections.emptyList();
			//addressList = pictureService.findLocation(addressQuery, locale.getCountry(), locale.getLanguage());
			if (addressList.isEmpty()) {
				searchForm.recordError(addressField, "No matching address found");
			} else if (addressList.size() > 1) {
				searchForm.recordError(addressSelectField, "Select the desired address");
			}
		} else {
			addressList = null;
		}
		
	}
	
	void onSuccessFromSearch() throws IOException {
		pageNumber = 0;
	}
	
	List<String> onProvideCompletionsFromKeywords(String partial) throws IOException {
		return pictureService.findKeywordSuggestions(MediaType.PICTURE, partial);
	}
}
