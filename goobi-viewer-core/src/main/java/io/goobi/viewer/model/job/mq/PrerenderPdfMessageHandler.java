package io.goobi.viewer.model.job.mq;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetPdfAction;
import de.unigoettingen.sub.commons.contentlib.servlet.model.ContentServerConfiguration;
import de.unigoettingen.sub.commons.contentlib.servlet.model.SinglePdfRequest;
import de.unigoettingen.sub.commons.util.PathConverter;
import io.goobi.viewer.controller.FileTools;
import io.goobi.viewer.controller.ProcessDataResolver;
import io.goobi.viewer.controller.mq.MessageHandler;
import io.goobi.viewer.controller.mq.MessageStatus;
import io.goobi.viewer.controller.mq.ViewerMessage;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;
import io.goobi.viewer.model.job.TaskType;

public class PrerenderPdfMessageHandler implements MessageHandler<MessageStatus> {

    private static final Logger logger = LogManager.getLogger(PrerenderPdfMessageHandler.class);
    private static final String PDF = "pdf";
    private static final String MEDIA = "media";
    private static final String ALTO = "alto";

    private final ProcessDataResolver processDataResolver;
    private final ContentServerConfiguration contentServerConfiguration;

    public PrerenderPdfMessageHandler() {
        this.processDataResolver = new ProcessDataResolver();
        this.contentServerConfiguration = ContentServerConfiguration.getInstance();
    }

    public PrerenderPdfMessageHandler(ProcessDataResolver processDataResolver, ContentServerConfiguration contentServerConfiguration) {
        this.processDataResolver = processDataResolver;
        this.contentServerConfiguration = contentServerConfiguration;
    }

    @Override
    public MessageStatus call(ViewerMessage ticket) {

        String pi = ticket.getProperties().get("pi");
        String configVariant = ticket.getProperties().get("config");
        boolean force = Boolean.parseBoolean(ticket.getProperties().get("force"));

        if (StringUtils.isNotBlank(pi)) {
            logger.trace("Starting task to prerender pdf files for PI {}, using config {}; force = {}", pi, this.contentServerConfiguration, force);
            try {
                if(!createPdfFiles(pi, configVariant, force)) {
                    return MessageStatus.ERROR;
                }
            } catch (IndexUnreachableException | PresentationException e) {
                logger.error("Failed to get data folders for PI {}. Reason: {}", pi, e.toString());
                return MessageStatus.ERROR;
            }
        }
        return MessageStatus.FINISH;
    }

    private boolean createPdfFiles(String pi, String configVariant, boolean force) throws PresentationException, IndexUnreachableException {
        Map<String, Path> dataFolders = processDataResolver.getDataFolders(pi, MEDIA, PDF, ALTO);
        Path imageFolder = dataFolders.get(MEDIA);
        Path pdfFolder = dataFolders.get(PDF);
        Path altoFolder =dataFolders.get(ALTO);
        if (imageFolder != null && pdfFolder != null && Files.exists(imageFolder)) {
            List<Path> imageFiles = FileTools.listFiles(imageFolder, FileTools.imageNameFilter);
            List<Path> pdfFiles = FileTools.listFiles(pdfFolder, FileTools.pdfNameFilter);
            if (imageFiles.isEmpty()) {
                logger.trace("No images in {}. Abandoning task", imageFolder);
            } else if (imageFiles.size() == pdfFiles.size() && !force) {
                logger.trace("PDF files already exist. Abandoning task");
            } else {
                if(!Files.exists(pdfFolder)) {
                    try {
                        Files.createDirectories(pdfFolder);
                    } catch (IOException e) {
                        logger.error("Cannot create pdf directory: {}", e.toString());
                        return false;
                    }
                }
                for (Path imagePath : imageFiles) {
                    if (!createPdfFile(imagePath, pdfFolder, altoFolder, configVariant)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean createPdfFile(Path imagePath, Path pdfFolder, Path altoFolder, String configVariant) {
        Map<String, String> params = Map.of(
                "config", configVariant,
                "ignoreCache", "true",
                "altoSource", Optional.ofNullable(altoFolder).map(f ->  PathConverter.toURI(f.toAbsolutePath()).toString()).orElse(""),
                "imageSource", PathConverter.toURI(imagePath.getParent().toAbsolutePath()).toString());
        Path pdfPath = pdfFolder.resolve(FileTools.replaceExtension(imagePath.getFileName(), "pdf"));
        try (OutputStream out = Files.newOutputStream(pdfPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            SinglePdfRequest request = new SinglePdfRequest(imagePath.toString(), params);
            new GetPdfAction().writePdf(request, this.contentServerConfiguration, out);
        } catch (ContentLibException | IOException | URISyntaxException e) {
            logger.error("Failed to create pdf file {} from {}. Reason: {}", pdfPath, imagePath, e.toString());
            return false;
        }
        return true;
    }

    @Override
    public String getMessageHandlerName() {
        return TaskType.PRERENDER_PDF.name();
    }

}
