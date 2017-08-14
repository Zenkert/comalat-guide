package org.sakaiproject.guide.tool;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.guide.tool.pages.*;
import org.sakaiproject.util.ResourceLoader;

import java.util.Locale;

/**
 * Main application class for our app
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @author David F. Torres
 * @author Michael Mertins (mertins@zedat.fu-berlin.de)
 *
 *         Cf. https://confluence.sakaiproject.org/pages/viewpage.action?pageId=
 *         83034325
 */
public class MyApplication extends WebApplication {

	/**
     * Constructor
     */
    public MyApplication() {
    }

    /**
     * Configure your app here
	 */
	@Override
	protected void init() {
		super.init();

		// Mount progress page
		mountPage("/progress/", ProgressPage.class);

		// Mount profile page
		mountPage("/profile/", ProfilePage.class);

		// Mount achievement page
		mountPage("/achievement/", AchievementPage.class);

        // Mount welcome page
        mountPage("/welcome/", WelcomePage.class);

		// Mount transition page
		mountPage("/transition/", TransitionPage.class);

		// Mount result page
		mountPage("/result/", RecordPage.class);

        // Mount skipped activities page
        mountPage("/skippedActivities/", SkippedActivitiesPage.class);

		// Configure for Spring injection
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		// Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);

		// Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);

		// Don't add any extra tags around a disabled link (default is
		// <em></em>)
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);

		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(LessonPage.class);
		getApplicationSettings().setAccessDeniedPage(LessonPage.class);
		getResourceSettings().getStringResourceLoaders().add(new SakaiStringResourceLoader());

		getRequestCycleListeners().add(new IRequestCycleListener() {

			public void onBeginRequest() {
				// optionally do something at the beginning of the request
			}

			public void onEndRequest() {
				// optionally do something at the end of the request
			}

			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				// optionally do something here when there's an exception

				// then, return the appropriate IRequestHandler, or "null"
				// to let another listener handle the exception
				ex.printStackTrace();
				return null;
			}

			@Override
			public void onBeginRequest(RequestCycle arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDetach(RequestCycle arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onEndRequest(RequestCycle arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle arg0, IRequestHandler arg1, Exception arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestHandlerExecuted(RequestCycle arg0, IRequestHandler arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestHandlerResolved(RequestCycle arg0, IRequestHandler arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestHandlerScheduled(RequestCycle arg0, IRequestHandler arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUrlMapped(RequestCycle arg0, IRequestHandler arg1, Url arg2) {
				// TODO Auto-generated method stub

			}
		});

		// to put this app into deployment mode, see web.xml
	}

	/**
	 * The main page for our app
     *
     * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class getHomePage() {
		return LessonPage.class;
	}

	// Custom resource loader
	private static class SakaiStringResourceLoader implements IStringResourceLoader {

		private ResourceLoader messages = new ResourceLoader("MyApplication");

		@Override
		public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation) {
			messages.setContextLocale(locale);
			return messages.getString(key, key);
		}

		@Override
		public String loadStringResource(Component component, String key, Locale locale, String style,
				String variation) {
			messages.setContextLocale(locale);
			return messages.getString(key, key);
		}

	}

}
