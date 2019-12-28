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
package io.goobi.viewer.managedbeans;

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

import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Region;
import de.unigoettingen.sub.commons.util.PathConverter;
import io.goobi.viewer.controller.Configuration;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.controller.StringTools;
import io.goobi.viewer.controller.imaging.IIIFPresentationAPIHandler;
import io.goobi.viewer.controller.imaging.IIIFUrlHandler;
import io.goobi.viewer.controller.imaging.ImageHandler;
import io.goobi.viewer.controller.imaging.MediaHandler;
import io.goobi.viewer.controller.imaging.Object3DHandler;
import io.goobi.viewer.controller.imaging.PdfHandler;
import io.goobi.viewer.controller.imaging.ThumbnailHandler;
import io.goobi.viewer.controller.imaging.WatermarkHandler;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.ViewerConfigurationException;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.model.viewer.PageType;
import io.goobi.viewer.model.viewer.PhysicalElement;
import io.goobi.viewer.model.viewer.StructElement;
import io.goobi.viewer.model.viewer.ViewManager;
import io.goobi.viewer.servlets.utils.ServletUtils;

/**
 * Provides methods for creation all urls for media delivery (images and other) Examples:
 * <ul>
 * <li>imageDelivery.thumbs.thumbnailUrl(pyhsicalElement[, width, height])</li>
 * <li>imageDelivery.thumbs.thumbnailUrl(structElement[, width, height])</li>
 * <li>imageDelivery.thumbs.thumbnailUrl(solrDocument[, width, height])</li>
 * <li>imageDelivery.thumbs.thumbnailUrl(cmsMediaItem[, width, height])</li>
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
    private Object3DHandler objects3d;
    private IIIFPresentationAPIHandler presentation;

    @PostConstruct
    private void init() {
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

    /**
     * Initialize for testing
     *
     * @param config a {@link io.goobi.viewer.controller.Configuration} object.
     * @param servletPath a {@link java.lang.String} object.
     */
    public void init(Configuration config, String servletPath) {
        this.staticImagesURI = getStaticImagesPath(servletPath, config.getTheme());
        this.cmsMediaPath =
                DataManager.getInstance().getConfiguration().getViewerHome() + DataManager.getInstance().getConfiguration().getCmsMediaFolder();
        iiif = new IIIFUrlHandler();
        images = new ImageHandler();
        objects3d = new Object3DHandler(config);
        footer = new WatermarkHandler(config, servletPath);
        thumbs = new ThumbnailHandler(iiif, config, this.staticImagesURI);
        pdf = new PdfHandler(footer, config);
        media = new MediaHandler(config);
        try {
            presentation = new IIIFPresentationAPIHandler(servletPath, config);
        } catch (URISyntaxException e) {
            logger.error("Failed to initalize presentation api handler", e.toString());
        }
    }

    private static Optional<PhysicalElement> getCurrentPageIfExists() {
        return Optional.ofNullable(BeanUtils.getActiveDocumentBean()).map(adb -> adb.getViewManager()).map(vm -> {
            try {
                return vm.getCurrentPage();
            } catch (IndexUnreachableException | DAOException e) {
                logger.error(e.toString());
                return null;
            }
        });
    }

    private static Optional<StructElement> getTopDocumentIfExists() {
        return Optional.ofNullable(BeanUtils.getActiveDocumentBean()).map(bean -> bean.getTopDocument());
    }

    private static Optional<StructElement> getCurrentDocumentIfExists() {
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
     * Returns the default size thumbnail url of the current top level document (according to the {@link io.goobi.viewer.model.viewer.ViewManager} Returns an empty string if no
     * current document exists
     *
     * @return The representative thumbnail for the current top docStruct element
     */
    public String getRepresentativeThumbnail() {
        return getTopDocumentIfExists().map(doc -> getThumbs().getThumbnailUrl(doc)).orElse("");
    }

    /**
     * Returns a thumbnail url of the current top level document (according to the {@link io.goobi.viewer.model.viewer.ViewManager} with the given width/height. Returns an empty
     * string if no current document exists
     *
     * @return The representative thumbnail for the current top docStruct element
     * @param width a int.
     * @param height a int.
     */
    public String getRepresentativeThumbnail(int width, int height) {
        return getTopDocumentIfExists().map(doc -> getThumbs().getThumbnailUrl(doc, width, height)).orElse("");
    }

    /**
     * Returns the default size thumbnail url of the current top level document (according to the {@link io.goobi.viewer.model.viewer.ViewManager} for a square thumbnail image.
     * Returns an empty string if no current document exists
     *
     * @return The representative thumbnail for the current top docStruct element
     */
    public String getRepresentativeSquareThumbnail() {
        return getTopDocumentIfExists().map(doc -> getThumbs().getSquareThumbnailUrl(doc)).orElse("");
    }

    /**
     * Returns a thumbnail url of the current top level document (according to the {@link io.goobi.viewer.model.viewer.ViewManager} for a square thumbnail image of the given size.
     * Returns an empty string if no current document exists
     *
     * @return The representative thumbnail for the current top docStruct element
     * @param size a int.
     */
    public String getRepresentativeSquareThumbnail(int size) {
        return getTopDocumentIfExists().map(doc -> {
            return getThumbs().getSquareThumbnailUrl(doc, size);
        }).orElse("");
    }

    /**
     * Returns the default size thumbnail url of the current page (according to the {@link io.goobi.viewer.model.viewer.ViewManager}. Returns an empty string if no current page
     * exists
     *
     * @return The thumbnail of the current page
     */
    public String getCurrentPageThumbnail() {
        return getCurrentPageIfExists().map(page -> {
            return getThumbs().getThumbnailUrl(page);
        }).orElse("");
    }

    /**
     * Returns a thumbnail url of the current page (according to the {@link io.goobi.viewer.model.viewer.ViewManager} of the given width/height. Returns an empty string if no
     * current page exists
     *
     * @return The thumbnail of the current page
     * @param width a int.
     * @param height a int.
     */
    public String getCurrentPageThumbnail(int width, int height) {
        return getCurrentPageIfExists().map(page -> getThumbs().getThumbnailUrl(page, width, height)).orElse("");
    }

    /**
     * Returns the default size thumbnail url of the current page (according to the {@link io.goobi.viewer.model.viewer.ViewManager} for a square thumbnail image. Returns an empty
     * string if no current page exists
     *
     * @return The thumbnail of the current page
     */
    public String getCurrentPageSquareThumbnail() {
        return getCurrentPageIfExists().map(page -> getThumbs().getSquareThumbnailUrl(page)).orElse("");
    }

    /**
     * Returns a thumbnail url of the current page (according to the {@link io.goobi.viewer.model.viewer.ViewManager} for a square thumbnail image of the given size. Returns an
     * empty string if no current page exists
     *
     * @return The thumbnail of the current page
     * @param size a int.
     */
    public String getCurrentPageSquareThumbnail(int size) {
        return getCurrentPageIfExists().map(page -> getThumbs().getSquareThumbnailUrl(page, size)).orElse("");
    }

    /**
     * Returns the url to the image of the current page: Either a IIIF manifest if available, or a full image url For image size and tiling
     * ingormation {@link io.goobi.viewer.model.viewer.PageType#viewImage} is assumed
     *
     * @return The url to the image of the current page: Either a IIIF manifest if available, or a full image url
     */
    public String getCurrentImage() {
        return getCurrentPageIfExists().map(page -> getImages().getImageUrl(page)).orElse("");
    }

    /**
     * Returns the url to the image of the current page: Either a IIIF image information if available, or a full image url The given pageType affects
     * image size and tiling suggestions for IIIF image information. If the given pageType does not match any known pageType, an empty String is
     * returned
     *
     * @return The url to the image of the current page for the current pageType: Either a IIIF manifest if available, or a full image url
     * @param pageType a {@link java.lang.String} object.
     */
    public String getCurrentImage(String pageType) {
        PageType type = PageType.getByName(pageType);
        if (type == null) {
            return "";
        }
        return getCurrentPageIfExists().map(page -> getImages().getImageUrl(page, type)).orElse("");
    }

    /**
     * returns The media url for the current page in the fist available format
     *
     * @return The media url for the current page in the fist available format
     */
    public String getCurrentMedia() {
        return getCurrentPageIfExists().map(page -> getMedia().getMediaUrl(page.getMimeType(), page.getPi(), page.getFilename())).orElse("");
    }

    /**
     * returns The media url for the current page in the given format
     *
     * @param format The requested format. Format is the part of the Solr field name after the underscore, generally the second part of the mime type
     * @return The media url for the current page in the given format
     */
    public String getCurrentMedia(String format) {
        return getCurrentPageIfExists().map(page -> getMedia().getMediaUrl(page.getMimeType(), page.getPi(), page.getFileNameForFormat(format)))
                .orElse("");
    }

    /**
     * Return the URL to the IIIF manifest of the current work, of it exists. Otherwise return empty String
     *
     * @return the manifest url
     */
    public String getIiifManifest() {
        return getCurrentDocumentIfExists().map(doc -> {
            try {
                return getPresentation().getManifestUrl(doc.getPi());
            } catch (URISyntaxException e) {
                logger.error(e.toString(), e);
                return null;
            }
        }).orElse("");
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.IIIFUrlHandler}, creates it if it doesn't exist yet
     *
     * @return the iiif
     * @throws io.goobi.viewer.exceptions.ViewerConfigurationException if any.
     */
    public IIIFUrlHandler getIiif() throws ViewerConfigurationException {
        if (iiif == null) {
            init();
        }
        return iiif;
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.WatermarkHandler}, creates it if it doesn't exist yet
     *
     * @return the footer
     */
    public WatermarkHandler getFooter() {
        if (footer == null) {
            init();
        }
        return footer;
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.ImageHandler}, creates it if it doesn't exist yet
     *
     * @return the images
     */
    public ImageHandler getImages() {
        if (images == null) {
            init();
        }
        return images;
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.PdfHandler}, creates it if it doesn't exist yet
     *
     * @return the pdf
     */
    public PdfHandler getPdf() {
        if (pdf == null) {
            init();
        }
        return pdf;
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.ThumbnailHandler}, creates it if it doesn't exist yet
     *
     * @return the thumbs
     */
    public ThumbnailHandler getThumbs() {
        if (thumbs == null) {
            init();
        }
        return thumbs;
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.MediaHandler}, creates it if it doesn't exist yet
     *
     * @return the media
     */
    public MediaHandler getMedia() {
        return media;
    }
    
    /**
     * <p>getObjects3D.</p>
     *
     * @return a {@link io.goobi.viewer.controller.imaging.Object3DHandler} object.
     */
    public Object3DHandler getObjects3D() {
        return objects3d;
    }

    /**
     * Retrieves the #{@link io.goobi.viewer.controller.imaging.IIIFPresentationAPIHandler}, creates it if it doesn't exist yet
     *
     * @return the iiif presentation handler
     */
    public IIIFPresentationAPIHandler getPresentation() {
        if (presentation == null) {
            throw new IllegalStateException("Presentation handler was not initialized");
        }
        return presentation;
    }

    /**
     * @return the servletPath
     * @throws ViewerConfigurationException
     */
    private String getServletPath() throws ViewerConfigurationException {
        if (servletPath == null) {
            init();
        }
        return servletPath;
    }

    /**
     * Returns true if the given String denotes an absolute url that is not a file url, false otherwise
     *
     * @return whether the given string denotes to an external url resource
     * @param urlString the string to test
     * @throws io.goobi.viewer.exceptions.ViewerConfigurationException if any.
     */
    public boolean isExternalUrl(String urlString) throws ViewerConfigurationException {
        try {
            URI uri = new URI(urlString);
            if (uri.isAbsolute() && !uri.getScheme().toLowerCase().startsWith("file")) {
                return !urlString.startsWith(getServletPath());
            }
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Retrieves the url path to the viewer image resource folder. Ith the theme is given, the image resource folder within the theme is returned
     *
     * @param theme The name of the theme housing the images. If this is null or empty, the images are taken from the viewer core
     * @return The url to the images folder in resources (possibly in the given theme)
     * @param servletPath a {@link java.lang.String} object.
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
     * Tests whether the given url points to a cms media file - i.e. any file within the configured cms media path
     *
     * @param url The url to test
     * @return true if the url points to a cms media file
     * @throws io.goobi.viewer.exceptions.ViewerConfigurationException if any.
     */
    public boolean isCmsUrl(String url) throws ViewerConfigurationException {
        URI uri;
        try {
            url = StringTools.decodeUrl(url);
            uri = PathConverter.toURI(url);
            Path path = PathConverter.getPath(uri);
            if (path.isAbsolute()) {
                path = path.normalize();
                Path cmsMediaPath = Paths.get(getCmsMediaPath());
                return path.startsWith(cmsMediaPath);
            }
        } catch (URISyntaxException e) {
            logger.trace(e.toString());
        }
        return false;
    }

    /**
     * Tests whether the given url points to a static image resource within the current theme
     *
     * @param url the url to test
     * @return true if the url points to a static image resource within the current theme
     * @throws io.goobi.viewer.exceptions.ViewerConfigurationException if any.
     */
    public boolean isStaticImageUrl(String url) throws ViewerConfigurationException {
        return url.startsWith(getStaticImagesURI());
    }

    /**
     * Returns the url path to the static images folder of the viewer theme (or the viewer itself if no theme is found
     *
     * @return the staticImagesURI
     * @throws io.goobi.viewer.exceptions.ViewerConfigurationException if any.
     */
    public String getStaticImagesURI() throws ViewerConfigurationException {
        if (staticImagesURI == null) {
            init();
        }
        // logger.trace("staticImagesURI: {}", staticImagesURI);
        return staticImagesURI;
    }

    /**
     * @return the cmsMediaPath
     * @throws ViewerConfigurationException
     */
    private String getCmsMediaPath() throws ViewerConfigurationException {
        if (cmsMediaPath == null) {
            init();
        }
        return cmsMediaPath;
    }

    /**
     * <p>getIfExists.</p>
     *
     * @return an optional containing the given String if it is non-empty, otherwise an empty optional
     * @param url a {@link java.lang.String} object.
     */
    public Optional<String> getIfExists(String url) {
        return Optional.of(url).map(string -> StringUtils.isNotBlank(string) ? string : null);
    }
    
    /**
     * <p>Setter for the field <code>thumbs</code>.</p>
     *
     * @param thumbs the thumbs to set
     */
    public void setThumbs(ThumbnailHandler thumbs) {
        this.thumbs = thumbs;
    }
    
    /**
     * <p>Setter for the field <code>images</code>.</p>
     *
     * @param images the images to set
     */
    public void setImages(ImageHandler images) {
        this.images = images;
    }
    
    /**
     * <p>Setter for the field <code>pdf</code>.</p>
     *
     * @param pdf the pdf to set
     */
    public void setPdf(PdfHandler pdf) {
        this.pdf = pdf;
    }


}
