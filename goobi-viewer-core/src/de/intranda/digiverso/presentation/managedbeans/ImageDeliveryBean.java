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
package de.intranda.digiverso.presentation.managedbeans;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.intranda.digiverso.presentation.controller.Configuration;
import de.intranda.digiverso.presentation.controller.DataManager;
import de.intranda.digiverso.presentation.controller.Helper;
import de.intranda.digiverso.presentation.controller.imaging.IIIFUrlHandler;
import de.intranda.digiverso.presentation.controller.imaging.ImageHandler;
import de.intranda.digiverso.presentation.controller.imaging.MediaHandler;
import de.intranda.digiverso.presentation.controller.imaging.PdfHandler;
import de.intranda.digiverso.presentation.controller.imaging.ThumbnailHandler;
import de.intranda.digiverso.presentation.controller.imaging.WatermarkHandler;
import de.intranda.digiverso.presentation.exceptions.DAOException;
import de.intranda.digiverso.presentation.exceptions.IndexUnreachableException;
import de.intranda.digiverso.presentation.managedbeans.utils.BeanUtils;
import de.intranda.digiverso.presentation.model.viewer.PhysicalElement;
import de.intranda.digiverso.presentation.model.viewer.StructElement;
import de.intranda.digiverso.presentation.servlets.utils.ServletUtils;

/**
 * Provides methods for creation all urls for media delivery (images and other) Examples:
 * <ul>
 * <li>imageDelivery.thumbs.thumbnailUrl(pyhsicalElement[, width, height])</li>
 * <li>imageDelivery.thumbs.thumbnailUrl(structElement[, width, height])</li>
 * <li>imageDelivery.thumbs.thumbnailUrl(solrDocument[, width, height])</li>
 * <li>imageDelivery.thumbs.squareThumbnailUrl(pyhsicalElement[, size])</li>
 * <li>imageDelivery.thumbs.squareThumbnailUrl(structElement[, size])</li>
 * <li>imageDelivery.thumbs.squareThumbnailUrl(solrDocument[, size])</li>
 * </ul>
 * <ul>
 * <li>imageDelivery.images.imageUrl(pyhsicalElement[, pageType])</li>
 * <li>imageDelivery.pdf.pdfUrl(structElement[, pyhsicalElement[, more physicalElements...]])</li>
 * <li>imageDelivery.media.mediaUrl(mimeType, pi, filename)</li>
 * </ul>
 * 
 * @author Florian Alpers
 *
 */
@Named("imageDelivery")
@SessionScoped
public class ImageDeliveryBean implements Serializable {

    private static final long serialVersionUID = -7128779942549718191L;

    private static final Logger logger = LoggerFactory.getLogger(ImageDeliveryBean.class);

    @Inject
    private HttpServletRequest servletRequest;

    private String servletPath;
    private String staticImagesURI;
    private String cmsMediaPath;
    private ImageHandler images;
    private ThumbnailHandler thumbs;
    private PdfHandler pdf;
    private WatermarkHandler footer;
    private IIIFUrlHandler iiif;
    private MediaHandler media;

    @PostConstruct
    public void init() {
        try {
            Configuration config = DataManager.getInstance().getConfiguration();
            if (servletRequest != null) {
                this.servletPath = ServletUtils.getServletPathWithHostAsUrlFromRequest(servletRequest);
            } else if (BeanUtils.hasJsfContext()) {
                this.servletPath = BeanUtils.getServletPathWithHostAsUrlFromJsfContext();
            } else {
                logger.error("Failed to initialize ImageDeliveryBean: No servlet request and no jsf context found");
                servletPath = "";
            }
            init(config, servletPath);
        } catch (NullPointerException e) {
            logger.error("Failed to initialize ImageDeliveryBean: Resources misssing");
        }
    }

    public void init(Configuration config, String servletPath) {
        this.staticImagesURI = getStaticImagesPath(servletPath, config.getTheme());
        this.cmsMediaPath =
                DataManager.getInstance().getConfiguration().getViewerHome() + DataManager.getInstance().getConfiguration().getCmsMediaFolder();
        iiif = new IIIFUrlHandler();
        images = new ImageHandler();
        footer = new WatermarkHandler(config, servletPath);
        thumbs = new ThumbnailHandler(iiif, config, this.staticImagesURI);
        pdf = new PdfHandler(footer, config);
        media = new MediaHandler(config);
    }

    private Optional<PhysicalElement> getCurrentPageIfExists() {
        return Optional.ofNullable(BeanUtils.getActiveDocumentBean()).map(adb -> adb.getViewManager()).map(vm -> {
            try {
                return vm.getCurrentPage();
            } catch (IndexUnreachableException | DAOException e) {
                logger.error(e.toString());
                return null;
            }
        });
    }

    private Optional<StructElement> getTopDocumentIfExists() {
        return Optional.ofNullable(BeanUtils.getActiveDocumentBean()).map(bean -> bean.getTopDocument());
    }

    public Optional<StructElement> getCurrentDocumentIfExists() {
        return Optional.ofNullable(BeanUtils.getActiveDocumentBean()).map(bean -> {
            try {
                return bean.getCurrentElement();
            } catch (IndexUnreachableException e) {
                logger.error(e.toString());
                return null;
            }
        });
    }

    /**
     * @return The representative thumbnail for the current top docStruct element
     */
    public String getRepresentativeThumbnail() {
        return getTopDocumentIfExists().map(doc -> getThumb().getThumbnailUrl(doc)).orElse("");
    }

    /**
     * 
     * @param page
     * @return The thumbnail of the current page
     */
    public String getCurrentPageThumbnail() {
        return getCurrentPageIfExists().map(page -> getThumb().getThumbnailUrl(page)).orElse("");
    }

    /**
     * @return the servletRequest
     */
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    /**
     * @param servletRequest the servletRequest to set
     */
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    /**
     * @return the iiif
     */
    public IIIFUrlHandler getIiif() {
        if (iiif == null) {
            init();
        }
        return iiif;
    }

    /**
     * @return the footer
     */
    public WatermarkHandler getFooter() {
        if (footer == null) {
            init();
        }
        return footer;
    }

    /**
     * @return the image
     */
    public ImageHandler getImage() {
        if (images == null) {
            init();
        }
        return images;
    }

    /**
     * @return the pdf
     */
    public PdfHandler getPdf() {
        if (pdf == null) {
            init();
        }
        return pdf;
    }

    /**
     * @return the thumb
     */
    public ThumbnailHandler getThumb() {
        if (thumbs == null) {
            init();
        }
        return thumbs;
    }

    /**
     * @return the servletPath
     */
    public String getServletPath() {
        if (servletPath == null) {
            init();
        }
        return servletPath;
    }

    /**
     * @param decode
     */
    public boolean isExternalUrl(String urlString) {
        try {
            URI uri = new URI(urlString);
            if (uri.isAbsolute() && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                return !urlString.startsWith(getServletPath());
            } else {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * @param theme The name of the theme housing the images. If this is null or empty, the images are taken from the viewer core
     * @return The url to the images folder in resources (possibly in the given theme)
     */
    public static String getStaticImagesPath(String servletPath, String theme) {
        StringBuilder sb = new StringBuilder(servletPath);
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
     * @param decode
     * @return
     */
    public boolean isCmsUrl(String url) {
        URI uri;
        try {
            url = Helper.decodeUrl(url);
            uri = new URI(url);
            Path path = Paths.get(uri.getPath());
            if (path.isAbsolute()) {
                path = path.normalize();
                return path.startsWith(getCmsMediaPath());
            }
        } catch (URISyntaxException e) {
            logger.trace(e.toString());
        }
        return false;
    }

    /**
     * @param decode
     * @return
     */
    public boolean isStaticImageUrl(String url) {
        return url.startsWith(getStaticImagesURI());
    }

    /**
     * @return the staticImagesURI
     */
    public String getStaticImagesURI() {
        if (staticImagesURI == null) {
            init();
        }
        return staticImagesURI;
    }

    /**
     * @return the cmsMediaPath
     */
    public String getCmsMediaPath() {
        if (cmsMediaPath == null) {
            init();
        }
        return cmsMediaPath;
    }

    /**
     * @return the media
     */
    public MediaHandler getMedia() {
        return media;
    }
}
