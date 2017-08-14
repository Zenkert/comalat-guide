package org.sakaiproject.guide.logic;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

import java.util.Map;

/**
 * An interface to abstract all Sakai related API calls in a central method that
 * can be injected into our app.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 * Modified for the COMALAT Project
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 * @author Sascha Klein (sascha.klein@student.uni-siegen.de)
 * 
 */

public interface SakaiProxy {

	/**
	 * Get current siteid
	 * 
	 * @return
	 */
	public String getCurrentSiteId();

	/**
	 * Get current user id
	 * 
	 * @return
	 */
	public String getCurrentUserId();

	/**
	 * Get current user display name
	 * 
	 * @return
	 */
	public String getCurrentUserDisplayName();

	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * 
	 * @return
	 */
	public boolean isSuperUser();

	/**
	 * Post an event to Sakai
	 * 
	 * @param event
	 *            name of event
	 * @param reference
	 *            reference
	 * @param modify
	 *            true if something changed, false if just access
	 * 
	 */
	public void postEvent(String event, String reference, boolean modify);

	/**
	 * Get a configuration parameter as a boolean
	 * 
	 * @param dflt
	 *            the default value if the param is not set
	 * @return
	 */
	public boolean getConfigParam(String param, boolean dflt);

	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param dflt
	 *            the default value if the param is not set
	 * @return
	 */
	public String getConfigParam(String param, String dflt);

	/**
	 * Gets the LessonBuilderTool UUIDs in given Site.
	 * 
	 * @param siteId
	 *            The SiteID to look into.
	 * @return A {@link Map} where the key is the UUID and the value represents
	 *         the tools name in the Site. <br/>
	 *         If no LessonBuilderTool is implemented the {@link Map} will be
	 *         <code>null</code>.
	 */
	public Map<String, String> getLessonToolList(String siteId);

	/**
	 * Gets all the LessonBuilderTool UUIDs from all the Sites the User has
	 * joined.
	 * 
	 * @return A {@link Map} where the key is the UUID and the value represents
	 *         the tools name in the Site. <br/>
	 *         If no LessonBuilderTool is implemented the {@link Map} will be
	 *         <code>null</code>.
	 * @throws IdUnusedException
	 */
	public Map<String, String> getLessonToolListForUser()
			throws IdUnusedException;

	/**
	 * Gets all the LessonBuilderTool UUIDs from all the Sites the User has
	 * joined where the language property matches the given.
	 * 
	 * @param lang
	 *            The language requested.
	 * @return A {@link Map} where the key is the UUID and the value represents
	 *         the tools name in the Site. <br/>
	 *         If no LessonBuilderTool is implemented the {@link Map} will be
	 *         <code>null</code>.
	 * @throws IdUnusedException
	 */
	public Map<String, String> getLessonToolListForUserByLanguage(String lang)
			throws IdUnusedException;

	/**
	 * Gets the sites a user joined.
	 * 
	 * @return Preformatted {@link String} containing the Sites.
	 */
	public String getUserSites();

	public Site getCurrentSite() throws IdUnusedException;

	public SiteService getSiteService();

	public ToolManager getToolManager();

	public String getLastEvent();

	public SecurityService getSecurityService();

	public AuthzGroupService getAuthzGroupService();

}
