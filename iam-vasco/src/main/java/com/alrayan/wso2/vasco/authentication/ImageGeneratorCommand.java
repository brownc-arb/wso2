package com.alrayan.wso2.vasco.authentication;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.vasco.VASCOCommand;
import com.alrayan.wso2.vasco.VASCOException;
import com.vasco.image.exception.ImageGeneratorSDKException;
import com.vasco.image.generator.ImageGeneratorSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

/**
 * Command for generating the VASCO CRONTO image.
 *
 * @since 1.0.0
 */
public class ImageGeneratorCommand implements VASCOCommand<BufferedImage> {

    private static final Logger log = LoggerFactory.getLogger(ImageGeneratorCommand.class);
    private String credentialFieldRequestMessage;

    /**
     * Constructs an instance of {@link ImageGeneratorCommand}.
     *
     * @param credentialFieldRequestMessage credential field request message (string used to generate the CRONTO image)
     */
    public ImageGeneratorCommand(String credentialFieldRequestMessage) {
        this.credentialFieldRequestMessage = credentialFieldRequestMessage;
    }

    @Override
    public BufferedImage execute() throws VASCOException {
        int imageSize = 0;
        try {
            imageSize = Integer.parseInt(AlRayanConfiguration.VASCO_CRONTO_IMAGE_SIZE.getValue());
            return ImageGeneratorSDK.generateDynamicCrontoImage(imageSize, credentialFieldRequestMessage, true);
        } catch (ImageGeneratorSDKException e) {
            log.error(AlRayanError.ERROR_ON_GENERATING_VASCO_IMAGE.getErrorMessageWithCode() + " - {" +
                      "image size: " + imageSize + "," +
                      "credential field request message: " + credentialFieldRequestMessage +
                      "}", e);
            throw new VASCOException(AlRayanError.ERROR_ON_GENERATING_VASCO_IMAGE.getErrorMessageWithCode(), e);
        }
    }
}
