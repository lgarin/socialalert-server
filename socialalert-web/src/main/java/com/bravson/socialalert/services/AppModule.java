package com.bravson.socialalert.services;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.OrderConstraintBuilder;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.internal.services.ClasspathResourceSymbolProvider;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.RequestFilter;
import org.got5.tapestry5.jquery.JQuerySymbolConstants;
import org.joda.time.LocalDate;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 */
public class AppModule {

	@Contribute(TypeCoercer.class)
	public static void contributeTypeCoercer(Configuration<CoercionTuple<?, ?>> configuration) {
		Coercion<String, UUID> coercionUuid = new Coercion<String, UUID>() {
			public UUID coerce(String input) {
				try {
					return UUID.fromString(input);
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		};

		configuration.add(new CoercionTuple<String, UUID>(String.class, UUID.class, coercionUuid));
		
		Coercion<String, URL> coercionUrl = new Coercion<String, URL>() {
			public URL coerce(String input) {
				try {
					return new URL(input);
				} catch (MalformedURLException e) {
					return null;
				}
			}
		};

		configuration.add(new CoercionTuple<String, URL>(String.class, URL.class, coercionUrl));

		Coercion<String, URI> coercionUri = new Coercion<String, URI>() {
			public URI coerce(String input) {
				try {
					return new URI(input);
				} catch (URISyntaxException x) {
					return null;
				}
			}
		};

		configuration.add(new CoercionTuple<String, URI>(String.class, URI.class, coercionUri));
		
		Coercion<Date, LocalDate> coercionLocalDate = new Coercion<Date, LocalDate>() {
			@Override
			public LocalDate coerce(Date input) {
				return new LocalDate(input);
			}
		};
		
		configuration.add(new CoercionTuple<Date, LocalDate>(Date.class, LocalDate.class, coercionLocalDate));
		
		Coercion<LocalDate, Date> coercionDateLocalDate = new Coercion<LocalDate, Date>() {
			@Override
			public Date coerce(LocalDate input) {
				return input.toDate();
			}
		};
		
		configuration.add(new CoercionTuple<LocalDate, Date>(LocalDate.class, Date.class, coercionDateLocalDate));
	}

	@Contribute(SymbolSource.class)
	public void contributeSymbolSource(OrderedConfiguration<SymbolProvider> providers) {
		providers.add("extraConfiguration", new ClasspathResourceSymbolProvider("/app.properties"),
				"after:SystemProperties", "before:ApplicationDefaults");
	}

	@Contribute(MasterObjectProvider.class)
	public void contributeMasterObjectProvider(OrderedConfiguration<ObjectProvider> providers) {
		providers.addInstance("jsonServiceProvider", JsonServiceProvider.class, OrderConstraintBuilder.afterAll()
				.build());
	}

	public static void bind(ServiceBinder binder) {
	}
	
	public static HttpClient buildHttpClient() {
		return HttpClientBuilder.create().build();
	}

	public void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration) {
		configuration.addInstance("AccessController", AccessController.class, "before:PageRender");
	}

	@FactoryDefaults
	public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
		// The application version number is incorprated into URLs for some
		// assets. Web browsers will cache assets because of the far future
		// expires
		// header. If existing assets are changed, the version number should
		// also
		// change, to force the browser to download new versions. This overrides
		// Tapesty's default
		// (a random hexadecimal number), but may be further overriden by
		// DevelopmentModule or
		// QaModule.
		// configuration.override(SymbolConstants.APPLICATION_VERSION,
		// "1.0-SNAPSHOT");
	}

	@ApplicationDefaults
	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
		// Contributions to ApplicationDefaults will override any contributions
		// to
		// FactoryDefaults (with the same key). Here we're restricting the
		// supported
		// locales to just "en" (English). As you add localised message catalogs
		// and other assets,
		// you can extend this list of locales (it's a comma separated series of
		// locale names;
		// the first locale name is the default when there's no reasonable
		// match).
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");

		configuration.add(JQuerySymbolConstants.SUPPRESS_PROTOTYPE, "true");
	}


	public RequestFilter buildRequestLoggingFilter(final ApplicationStateManager asm) {
	  return new RequestLoggingFilter(asm);
	} 
}
