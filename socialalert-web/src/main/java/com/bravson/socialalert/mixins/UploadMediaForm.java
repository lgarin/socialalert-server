package com.bravson.socialalert.mixins;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;

public class UploadMediaForm {
	
	@Inject
	private HttpClient httpClient;
	
	@SessionAttribute("AppCookie")
	private String applicationCookie;
	
	@Inject
	@Symbol("server.upload.url")
	private URI uploadUri;
	
	@InjectContainer
    private Form uploadForm;

	@Parameter(defaultPrefix=BindingConstants.LITERAL)
	private String nextPage;
	
	@Property
	@Parameter(defaultPrefix=BindingConstants.PROP, required=true)
	private UploadedFile file;
	
	@Property
	@Parameter(defaultPrefix=BindingConstants.LITERAL, value="mediaUri")
	private String activationParameter;
	
	@Inject
	private PageRenderLinkSource pageRenderLinkSource;
	
	@Inject
	private ComponentSource componentSource;
	
	@Inject
	private RequestGlobals requestGlobals;
	
	Object onSuccess() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(uploadUri);
    	post.setEntity(new InputStreamEntity(file.getStream(), file.getSize(), ContentType.create(file.getContentType())));
    	
    	post.setHeader("Cookie", applicationCookie);

    	// TODO reuse the http client
    	HttpResponse response = httpClient.execute(post);
    	//try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
	    	//HttpResponse response = client.execute(post);
	    	switch (response.getStatusLine().getStatusCode()) {
	    	case HttpStatus.SC_CREATED:
	    		String mediaUri = response.getFirstHeader("Location").getValue();
	    		if (nextPage != null) {
	    			return pageRenderLinkSource.createPageRenderLinkWithContext(nextPage).addParameterValue(activationParameter, mediaUri);
	    		}
	    		return pageRenderLinkSource.createPageRenderLinkWithContext(requestGlobals.getActivePageName()).addParameterValue(activationParameter, mediaUri);
	    	case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
	    		uploadForm.recordError("Invalid picture format");
	    		return componentSource.getActivePage();
	    	default:
	    		uploadForm.recordError("Upload failed: " + response.getStatusLine());
	    		return componentSource.getActivePage();
	    	}
    	//}
    }
	
	Object onUploadException(FileUploadException ex)
    {
       uploadForm.recordError("Upload failed.");
       return componentSource.getActivePage();
    }
}
