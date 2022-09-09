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
package io.goobi.viewer.model.cms.content;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.weld.exceptions.IllegalArgumentException;

import io.goobi.viewer.model.cms.CMSPage;
import io.goobi.viewer.model.jsf.JsfComponent;

/**
 * Wraps a {@link CMSContent} within a {@link CMSPage}
 * @author florian
 *
 */
public class CMSContentItem {
    
    /**
     * Local identifier within the component. Used to reference this item within the component xhtml
     */
    private final String componentId;
    
    /**
     * The actual {@link CMSContent} wrapped in this item
     */
    private final CMSContent content;
    
    private final Map<String, Object> attributes = new HashMap<>(); 
    
    private final String label;
    
    private final String description;
    
    private final JsfComponent jsfComponent;

    /**
     * @param componentId
     * @param content
     */
    public CMSContentItem(String componentId, CMSContent content, String label, String description, JsfComponent jsfComponent) {
        super();
        if(StringUtils.isNotBlank(componentId)) {
            this.componentId = componentId;            
        } else {
            throw new IllegalArgumentException("ComponentId of CMSContentItem may not be blank");
        }
        if(content != null) {            
            this.content = content;
        } else {
            throw new IllegalArgumentException("CMSContent of COMSContentItem may not be null");
        }
        this.label = label;
        this.description = description;
        this.jsfComponent = jsfComponent;
    }
    
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
    
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }
    
    public String getComponentId() {
        return componentId;
    }
    
    public CMSContent getContent() {
        return content;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getDescription() {
        return description;
    }
    
    public JsfComponent getJsfComponent() {
        return jsfComponent;
    }
    
    @Override
    public int hashCode() {
        return componentId.hashCode();
    }
    
    /**
     * Two CMSContentItems are equal if their {@link #componentId}s are equal
     */
    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj.getClass().equals(this.getClass())) {
            return ((CMSContentItem)obj).componentId.equals(this.componentId);
        } else {
            return false;
        }
    }

}
