package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of our LessonBuilderProxy API
 * 
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 * @author Sascha Klein (sascha.klein@student.uni-siegen.de)
 *
 */

public class LessonBuilderProxyImpl implements LessonBuilderProxy {

	@Getter
	@Setter
	private LessonBuilderAccessAPI lessonBuilder;
	@Getter
	@Setter
	private SimplePageToolDao dao;

	@Override
	public String getStuff() {
		return lessonBuilder.getHttpAccess().toString();
	}

	/**
	 * Returns URL for any item related to specific page
	 * @param pageId
	 * @param itemName
	 * @param siteId
     * @return
     */
	public String getURLforItem(long pageId, String itemName, String siteId) {
		String output = "";
		for (SimplePageItem item : findItemsOnPage(pageId)) {
			if (item.getName().equals(itemName)) {
				output = item.getItemURL(siteId, null);
			}
		}
		return output;
	}

	public List<SimplePageItem> findItemsInSite(String siteId) {
		return dao.findItemsInSite(siteId);
	}

	public List<SimplePageItem> findItemsOnPage(long pageId) {
		return dao.findItemsOnPage(pageId);
	}

	public Map<String, String> getStiteIdsWithName(String siteId) {
		Map<String, String> map = new HashMap<String, String>();
		List<SimplePageItem> itemsFromSite = (ArrayList<SimplePageItem>) dao
				.findItemsInSite(siteId);
		for (SimplePageItem spi : itemsFromSite) {
			map.put(dao.getPage(spi.getPageId()).getSiteId(), spi.getName());
		}
		return map;
	}

	public long getPageId(String siteID) {
		// return dao.getPage(dao.findItemsInSite(arg0).getPageId()).getOwner();
		long out = 0;
		for (SimplePage page : dao.getSitePages(siteID)) {
			if (page.getTitle().equals("Lerneinheiten")) {
				out = page.getPageId();
			}
		}
		return out;
	}

	public String getLessonNames(String arg0) {
		String out = "";

		// gibt lessonbuilder items aus
		// for(SimplePageItem spi : dao.findItemsInSite(arg0)){
		//
		// }
		// if(spi.getNextPage()){
		// out += "<p Item: "+dao.findItem(spi.getId()+1)+"/><br/>";
		// }
		// HashSet<String> toolIds = new HashSet<String>();
		// for(SimplePage page : dao.getSitePages(arg0)){
		// out += page.getPageId()+" "+page.getSiteId()+"\n";
		// }
		// for(String s : toolIds){
		// out += dao.getPage(dao.getTopLevelPageId(s)).getSiteId();
		// }

		out += dao.findItemsInSite(arg0).size() + "\n";

		for (SimplePageItem spiParent : dao.findItemsInSite(arg0)) {
			out += spiParent.getName() + " " + spiParent.getSakaiId() + "\n";
			for (SimplePageItem spi : dao.findItemsInSite(spiParent
					.getSakaiId())) {
				out += "Site: " + dao.getPage(spi.getPageId()).getSiteId();
				out += " ID: " + spi.getId();
				out += " Next: " + spi.getGroups();
				out += " Lesson: " + spiParent.getName();
				out += " Name: " + spi.getName();
				out += " SakaiID: " + spi.getSakaiId();
				// out +=" Tool UUID: "+(p!=null?p.getToolId():"null")+"\n";
			}
		}

		return out;
	}

}
