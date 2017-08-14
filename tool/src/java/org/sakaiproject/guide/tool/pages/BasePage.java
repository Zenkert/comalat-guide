package org.sakaiproject.guide.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.guide.logic.*;
import org.sakaiproject.guide.model.ComalatUser;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * This is our base page for our Sakai app. It sets up the containing markup and
 * top navigation. All top level pages should extend from this page so as to
 * keep the same navigation. The content for those pages will be rendered in the
 * main area below the top nav.
 * 
 * It also allows us to setup the API injection and any other common methods,
 * which are then made available in the other pages.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 * Modified for the COMALAT Project
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 * @author George Kakarontzas (gkakaron@teilar.gr)
 *
 */

public class BasePage extends WebPage implements IHeaderContributor {

	public static final String IFRAMESCRIPT = "/library/webjars/iframe-resizer/3.5.0/iframeResizer.min.js";
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(BasePage.class);

	@SpringBean(name = "org.sakaiproject.guide.logic.SakaiProxy")
	protected static SakaiProxy sakaiProxy;

    @SpringBean(name = "org.sakaiproject.tool.api.SessionManager")
    protected static SessionManager sessionManager;

    @SpringBean(name = "org.sakaiproject.event.api.UsageSessionService")
    protected static UsageSessionService usageSessionService;


	@SpringBean(name = "org.sakaiproject.guide.logic.LessonBuilderProxy")
	protected static LessonBuilderProxy lessonProxy;

	protected static ComalatUser comalatUser;

	@SpringBean(name = "org.sakaiproject.guide.logic.SamigoProxy")
	protected static SamigoProxy samProxy;

	@SpringBean(name = "org.sakaiproject.event.api.EventTrackingService")
	protected EventTrackingService eventTrackingService;

	// protected QuestionPoolServiceAPI qpSevice;

	@SpringBean(name = "org.sakaiproject.guide.logic.ProjectLogic")
	protected ProjectLogic projectLogic;

	@SpringBean(name = "org.sakaiproject.guide.logic.ComalatUtilities")
	protected ComalatUtilities comalatUtilities;

	@SpringBean(name = "org.sakaiproject.guide.logic.StatisticalCompetenceAssessment")
	protected StatisticalCompetenceAssessment stat;

	protected String variant;
	Link<Void> firstLink;
	Link<Void> secondLink;
	Link<Void> thirdLink;
	Link<Void> fourthLink;
	Link<Void> fifthLink;

	FeedbackPanel feedbackPanel;
	private boolean renderHeader = true;

	@SuppressWarnings("deprecation")
	public BasePage() {

		String userId=sakaiProxy.getCurrentUserId();
		comalatUser =
				projectLogic.createComalatUserIfNotExistsForCurrentLanguage(userId );
		if (comalatUser.getLanguage()==null) {
			try {
				ResourceProperties rp =
						sakaiProxy.getCurrentSite().getProperties();
				String lang = (String) rp.get("language");
				comalatUser.setLanguage(lang);
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		log.debug("BasePage()");

		setUserPreferredLocale();

		// first link
		firstLink = new Link<Void>("firstLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {

                setResponsePage(LessonPage.class);
            }
		};
		firstLink.add(new Label("firstLinkLabel", new ResourceModel("base.link.first")).setRenderBodyOnly(true));
		firstLink.add(new AttributeModifier("title", true, new ResourceModel("base.link.first.tooltip")));
		add(firstLink);

		// second link
		secondLink = new Link<Void>("secondLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ProgressPage());
			}
		};
		secondLink.add(new Label("secondLinkLabel", new ResourceModel("base.link.second")).setRenderBodyOnly(true));
		secondLink.add(new AttributeModifier("title", true, new ResourceModel("base.link.second.tooltip")));
		add(secondLink);

		// third link
		thirdLink = new Link<Void>("thirdLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ProfilePage());
			}
		};
		thirdLink.add(new Label("thirdLinkLabel", new StringResourceModel("base.link.third", null, new String[] { "3" }))
				.setRenderBodyOnly(true));
		thirdLink.add(new AttributeModifier("title", true, new ResourceModel("base.link.third.tooltip")));
		add(thirdLink);

		// fourthPageLink
		fourthLink = new Link<Void>("fourthLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {

				setResponsePage(new AchievementPage());
			}
		};
		fourthLink.add(new Label("fourthLinkLabel", new ResourceModel("base.link.fourth")).setRenderBodyOnly(true));
		fourthLink.add(new AttributeModifier("title", true, new ResourceModel("base.link.fourth.tooltip")));
		add(fourthLink);

		// Add a FeedbackPanel for displaying our messages
		feedbackPanel = new FeedbackPanel("feedback") {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
				final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

				if (message.getLevel() == FeedbackMessage.ERROR || message.getLevel() == FeedbackMessage.DEBUG
						|| message.getLevel() == FeedbackMessage.FATAL
						|| message.getLevel() == FeedbackMessage.WARNING) {
					add(AttributeModifier.replace("class", "alertMessage"));
				} else if (message.getLevel() == FeedbackMessage.INFO) {
					add(AttributeModifier.replace("class", "success"));
				}

				return newMessageDisplayComponent;
			}
		};
		add(feedbackPanel);

	}

	public ProjectLogic getProjectLogic() {
		return projectLogic;
	}

	public void setProjectLogic(ProjectLogic projectLogic) {
		this.projectLogic = projectLogic;
	}

	/**
	 * Helper to clear the feedbackpanel display.
	 * 
	 * @param f
	 *            FeedBackPanel
	 */
	public void clearFeedback(FeedbackPanel f) {
		if (!f.hasFeedbackMessage()) {
			f.add(AttributeModifier.replace("class", ""));
		}
	}

	public String getVariation() {
		return variant;
	}

	/**
	 * This block adds the required wrapper markup to style it like a Sakai
	 * tool. Add to this any additional CSS or JS references that you need.
	 * 
	 */
	public void renderHead(IHeaderResponse response) {
		if (renderHeader) {
			// get the Sakai skin header fragment from the request attribute
			HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

			response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
			response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

			// Tool additions (at end so we can override if required)
			response.render(StringHeaderItem
					.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
			// response.renderCSSReference("css/my_tool_styles.css");
			// response.renderJavascriptReference("js/my_tool_javascript.js");

			response.render(JavaScriptHeaderItem.forUrl(IFRAMESCRIPT));

		} else {

		}
	}

	/**
	 * Allow overrides of the user's locale
	 */
	public void setUserPreferredLocale() {
		ResourceLoader rl = new ResourceLoader();
		Locale locale = rl.getLocale();
		log.debug("User preferred locale: " + locale);
		getSession().setLocale(locale);
	}

	/**
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setEnabled(false);
	}

	protected void setRenderHeader(boolean in) {
		renderHeader = in;
		setRenderBodyOnly(!in);
	}

}
