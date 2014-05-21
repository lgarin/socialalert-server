package com.bravson.socialalert.pages;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;

import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.PictureFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(allow={UserRole.USER})
public class ClaimPicture {

	@ActivationRequestParameter
	@Property
	private String pictureUri;
	
	@Inject
	@Symbol("app.thumbnail.url")
	private String thumbnailUrl;
	
	@Component(parameters={"clientValidation=SUBMIT"})
    private Form claimForm;
	
	@Property
	@Validate("required")
	private String title;
	
	@Property
	private String tags;
	
	@Property
	private List<MediaCategory> categories;
	
	@Inject
    private Messages messages;
	
	@Inject
	private TypeCoercer typeCoercer;

	@Inject
    private PictureFacade pictureService;

	Object onCancel() {
    	return UserHome.class;
    }
	
	public String getPictureUrl() {
		return thumbnailUrl + "/" + pictureUri;
	}
	
	public ValueEncoder<MediaCategory> getCategoryEncoder() {
        return new EnumValueEncoder<MediaCategory>(typeCoercer, MediaCategory.class);
    }

    public SelectModel getCategoryModel() {
        return new EnumSelectModel(MediaCategory.class, messages);
    }
	
	public Object onSuccess() throws ClientProtocolException, IOException
    {
		List<String> tagList = Arrays.asList(StringUtils.split(StringUtils.defaultString(tags), " ,;"));
		GeoAddress address = new GeoAddress(7.5, 46.9, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		pictureService.claimPicture(URI.create(pictureUri), title, address, categories, tagList);
		return UserHome.class;
    }
}
