package org.sakaiproject.guide.logic;

import org.sakaiproject.lessonbuildertool.SimplePageItem;

import java.util.List;
import java.util.Map;

/**
 * An interface to abstract the Lessonbuilder related API calls in a central
 * method that can be injected into our app.
 * 
 * @author Sascha Klein (sascha.klein@student.uni-siegen.de)
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 *
 */

public interface LessonBuilderProxy {

	public String getStuff();

	public String getURLforItem(long pageId, String itemName, String siteId);

	public long getPageId(String siteID);

	public List<SimplePageItem> findItemsInSite(String siteId);

	public List<SimplePageItem> findItemsOnPage(long pageId);

	public Map<String, String> getStiteIdsWithName(String siteId);

	public String getLessonNames(String siteId);

}
