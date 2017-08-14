package org.sakaiproject.guide.dao.impl;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.sakaiproject.guide.dao.ProjectDao;


/**
 * Implementation of ProjectDao
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class ProjectDaoImpl extends HibernateGeneralGenericDao implements ProjectDao {

	private static final Logger log = Logger.getLogger(ProjectDaoImpl.class);

	private PropertiesConfiguration statements;
	
	public void init() {
		log.info("init()");
	}

	

}
