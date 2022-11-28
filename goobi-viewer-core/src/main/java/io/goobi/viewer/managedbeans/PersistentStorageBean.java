/*
 * This file is part of the Goobi viewer - a content presentation and management
 * application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goobi.viewer.managedbeans;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.Pair;
import org.omnifaces.cdi.Eager;

import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.dao.IDAO;
import io.goobi.viewer.dao.update.DatabaseUpdater;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.model.cms.pages.CMSTemplateManager;

/**
 * Used for application wide storage of objects accessible to other managed objects
 *
 * @author florian
 *
 */
@Named("applicationBean")
@Eager
@ApplicationScoped
public class PersistentStorageBean implements Serializable {

    private static final long serialVersionUID = -5127431137772735598L;

    private Map<String, Pair<Object, Instant>> map = new HashMap<>();

    @Inject 
    transient private CMSTemplateManager templateManager;
    private IDAO dao;
    
    @PostConstruct
    public void startup() throws DAOException {
        this.dao = DataManager.getInstance().getDao();
        new DatabaseUpdater(dao, templateManager).update();
    }
    
    @PreDestroy
    public void shutdown() {
        
    }

    public synchronized Object get(String key) {
        return map.get(key).getLeft();
    }

    public synchronized boolean olderThan(String key, Instant now) {
        return map.get(key).getRight().isBefore(now);
    }

    public synchronized Object put(String key, Object object) {
        return map.put(key, Pair.of(object, Instant.now()));
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }
    
    public CMSTemplateManager getTemplateManager() {
        return templateManager;
    }
    
    public void setTemplateManager(CMSTemplateManager templateManager) {
        this.templateManager = templateManager;
    }
}
