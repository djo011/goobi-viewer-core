/**
 * This file is part of the Goobi viewer - a content presentation and management application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goobi.viewer.managedbeans.utils;

import java.util.Locale;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.managedbeans.ActiveDocumentBean;
import io.goobi.viewer.managedbeans.BrowseBean;
import io.goobi.viewer.managedbeans.CalendarBean;
import io.goobi.viewer.managedbeans.CmsBean;
import io.goobi.viewer.managedbeans.CmsCollectionsBean;
import io.goobi.viewer.managedbeans.CmsMediaBean;
import io.goobi.viewer.managedbeans.ImageDeliveryBean;
import io.goobi.viewer.managedbeans.MetadataBean;
import io.goobi.viewer.managedbeans.NavigationHelper;
import io.goobi.viewer.managedbeans.SearchBean;
import io.goobi.viewer.managedbeans.UserBean;
import io.goobi.viewer.model.security.user.User;
import io.goobi.viewer.servlets.utils.ServletUtils;

/**
 * Utility class for methods that use the FacesContext.
 */
public class BeanUtils {

    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    public static final String SLASH_REPLACEMENT = "U002F";
    public static final String BACKSLASH_REPLACEMENT = "U005C";
    public static final String PIPE_REPLACEMENT = "U007C";
    public static final String QUESTION_MARK_REPLACEMENT = "U003F";
    public static final String PERCENT_REPLACEMENT = "U0025";
    
    /**
     * Gets the current Request from the faces context
     * 
     * @return
     */
    public static HttpServletRequest getRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        return getRequest(context);
    }

    public static HttpServletRequest getRequest(FacesContext context) {
        if (context != null && context.getExternalContext() != null) {
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            return request;
        }

        return null;
    }

    /**
     * retrieve complete Servlet url from servlet context, including Url, Port, Servletname etc. call this method only from jsf context
     *
     * @return complete url as string
     */
    public static String getServletPathWithHostAsUrlFromJsfContext() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request != null) {
                return ServletUtils.getServletPathWithHostAsUrlFromRequest(request);
            }
        }

        return "";
    }

    public static boolean hasJsfContext() {
        return FacesContext.getCurrentInstance() != null;
    }

    public static String getServletImagesPathFromRequest(HttpServletRequest request, String theme) {
        StringBuilder sb = new StringBuilder(ServletUtils.getServletPathWithHostAsUrlFromRequest(request));
        if (!sb.toString().endsWith("/")) {
            sb.append("/");
        }
        sb.append("resources").append("/");
        if (StringUtils.isNotBlank(theme)) {
            sb.append("themes").append("/").append(theme).append("/");
        }
        sb.append("images").append("/");
        return sb.toString();
    }

    /**
     * 
     * @return
     */
    public static ServletContext getServletContext() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            return (ServletContext) context.getExternalContext().getContext();
        }
        return null;
    }

    /**
     * 
     * @return
     */
    public static Locale getLocale() {
        NavigationHelper nh = BeanUtils.getNavigationHelper();
        if (nh != null) {
            return nh.getLocale();
        }

        return Locale.ENGLISH;
    }
    
    public static Locale getDefaultLocale() {
        NavigationHelper nh = BeanUtils.getNavigationHelper();
        if (nh != null) {
            return nh.getDefaultLocale();
        }

        return Locale.ENGLISH;
    }

    private static BeanManager getBeanManager() {
        BeanManager ret = null;

        // Via CDI
        try {
            ret = CDI.current().getBeanManager();
            if (ret != null) {
                return ret;
            }
        } catch (IllegalStateException e) {
        }
        // Via FacesContext
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getExternalContext().getContext() != null) {
            ret = (BeanManager) ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext())
                    .getAttribute("javax.enterprise.inject.spi.BeanManager");
            if (ret != null) {
                return ret;
            }
        }
        // Via JNDI
        try {
            InitialContext initialContext = new InitialContext();
            return (BeanManager) initialContext.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            logger.warn("Couldn't get BeanManager through JNDI: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getBeanByName(String name, Class clazz) {
        BeanManager bm = getBeanManager();
        if (bm != null && bm.getBeans(name).iterator().hasNext()) {
            Bean bean = bm.getBeans(name).iterator().next();
            CreationalContext ctx = bm.createCreationalContext(bean);
            return bm.getReference(bean, clazz, ctx);
        }

        return null;
    }

    /**
     * 
     * @return
     */
    public static NavigationHelper getNavigationHelper() {
        NavigationHelper navigationHelper = (NavigationHelper) getBeanByName("navigationHelper", NavigationHelper.class);
        if (navigationHelper != null) {
            try {
                navigationHelper.getCurrentPage();
            } catch (ContextNotActiveException e) {
                navigationHelper = new NavigationHelper();
            }
        }
        return navigationHelper;
    }


    /**
     * 
     * @return
     */
    public static ActiveDocumentBean getActiveDocumentBean() {
        return (ActiveDocumentBean) getBeanByName("activeDocumentBean", ActiveDocumentBean.class);
    }

    /**
     * 
     * @return
     */
    public static SearchBean getSearchBean() {
        return (SearchBean) getBeanByName("searchBean", SearchBean.class);
    }

    public static CmsCollectionsBean getCMSCollectionsBean() {
        return (CmsCollectionsBean) getBeanByName("cmsCollectionsBean", CmsCollectionsBean.class);
    }
    
    public static MetadataBean getMetadataBean() {
        return (MetadataBean) getBeanByName("metadataBean", MetadataBean.class);
    }

    /**
     * 
     * @return
     */
    public static CmsBean getCmsBean() {
        return (CmsBean) getBeanByName("cmsBean", CmsBean.class);
    }

    /**
     * 
     * @return
     */
    public static CmsMediaBean getCmsMediaBean() {
        return (CmsMediaBean) getBeanByName("cmsMediaBean", CmsMediaBean.class);
    }

    
    /**
     * 
     * @return
     */
    public static CalendarBean getCalendarBean() {
        return (CalendarBean) getBeanByName("calendarBean", CalendarBean.class);
    }

    /**
     * 
     * @return
     */
    public static UserBean getUserBean() {
        return (UserBean) getBeanByName("userBean", UserBean.class);
    }

    public static ImageDeliveryBean getImageDeliveryBean() {
        ImageDeliveryBean bean = (ImageDeliveryBean) getBeanByName("imageDelivery", ImageDeliveryBean.class);
        if (bean == null) {
            bean = new ImageDeliveryBean();
            bean.init(DataManager.getInstance().getConfiguration(), DataManager.getInstance().getConfiguration().getRestApiUrl().replace("rest/", ""));
        } else {
            try {
                bean.getThumbs();
            } catch (ContextNotActiveException e) {
                bean = new ImageDeliveryBean();
                bean.init(DataManager.getInstance().getConfiguration(), DataManager.getInstance().getConfiguration().getRestApiUrl().replace("rest/", ""));
            }
        }
        return bean;
    }

    /**
     * 
     * @return
     */
    public static BrowseBean getBrowseBean() {
        return (BrowseBean) getBeanByName("browseBean", BrowseBean.class);
    }

    /**
     * 
     * @return
     */
    public static UserBean getUserBeanFromRequest(HttpServletRequest request) {
        if (request != null) {
            return (UserBean) request.getSession().getAttribute("userBean");
        }

        return null;
    }

    /**
     * 
     * @return
     */
    public static User getUserFromRequest(HttpServletRequest request) {
        UserBean ub = getUserBeanFromRequest(request);
        if (ub != null) {
            return ub.getUser();
        }

        return null;
    }

    public static String escapeCriticalUrlChracters(String value) {
        return escapeCriticalUrlChracters(value, false);
    }

    /**
     *
     * @param value
     * @return
     * @should replace characters correctly
     */
    public static String escapeCriticalUrlChracters(String value, boolean escapePercentCharacters) {
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }

        value = value.replace("/", SLASH_REPLACEMENT).replace("\\", BACKSLASH_REPLACEMENT).replace("|", PIPE_REPLACEMENT).replace("%7C", PIPE_REPLACEMENT).replace("?", QUESTION_MARK_REPLACEMENT);
        if (escapePercentCharacters) {
            value = value.replace("%", PERCENT_REPLACEMENT);
        }
        return value;
    }

    /**
     *
     * @param value
     * @return
     * @should replace characters correctly
     */
    public static String unescapeCriticalUrlChracters(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }

        return value.replace(SLASH_REPLACEMENT, "/")
                .replace(BACKSLASH_REPLACEMENT, "\\")
                .replace(PIPE_REPLACEMENT, "|")
                .replace(QUESTION_MARK_REPLACEMENT, "?")
                .replace(PERCENT_REPLACEMENT, "%");
    }

    /**
     * @return
     */
    public static HttpServletResponse getResponse() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {            
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            return response;
        } else {
            return null;
        }
    }
}
