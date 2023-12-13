package io.goobi.viewer.controller.shell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessOutputReader implements Runnable {

    private static final Logger logger = LogManager.getLogger(ProcessOutputReader.class);

    private InputStream inputStream;
    private StringBuffer sb = new StringBuffer();
    private Charset charset = StandardCharsets.UTF_8;
    private boolean keepOutput = true;

    /**
     * 
     * @param inputStream
     */
    public ProcessOutputReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (this.inputStream != null) {
                int read = inputStream.read(buffer.array(), 0, 1024);
                if (read <= 0) {
                    logger.trace("input finished");
                    break;
                }
                if (keepOutput) {
                    char[] chars = decodeWithCharset(Arrays.copyOf(buffer.array(), read), charset);
                    sb.append(chars);
                }
                buffer.clear();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.trace("interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException e) {
            logger.warn("Input finished with error: {}", e.getMessage());
        }

    }

    public static char[] decodeWithCharset(byte[] origBytes, Charset charset) throws CharacterCodingException {
        CharsetDecoder decoder = charset.newDecoder();

        ByteBuffer byteBuffer = ByteBuffer.wrap(origBytes);
        CharBuffer charBuffer = decoder.decode(byteBuffer);

        return charBuffer.array();
    }

    public String getOutput() {
        return sb.toString();
    }

    public void writeToFile(File file) throws IOException {
        getFileFromString(getOutput(), file, false);
    }

    private static File getFileFromString(String string, File file, boolean append) throws IOException {
        try (FileWriter writer = new FileWriter(file, append)) {
            writer.write(string);
        }
        return file;
    }

    public boolean isKeepOutput() {
        return keepOutput;
    }

    public void setKeepOutput(boolean keepOutput) {
        this.keepOutput = keepOutput;
    }

}
