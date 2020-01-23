package timeboard.core.internal;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Timeboard
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import timeboard.core.api.EncryptionService;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectAttributValue;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Component
public class EncryptionServiceImpl implements EncryptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionServiceImpl.class);


    private static final String SECRET_KEY = "738F26A3C1971235";

    @Override
    public String getProjectAttribute(final Project targetProject, final String attributeKey) {
        final ProjectAttributValue projectAttributValue = targetProject.getAttributes().get(attributeKey);
        String value = projectAttributValue.getValue();
        if (projectAttributValue.isEncrypted()) {
            try {
                final Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY.getBytes(), "AES"));
                value = new String(cipher.doFinal(Base64.getDecoder().decode(value)));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return value;
    }

    @Override
    public String encryptAttribute(final String strToEncrypt) {
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY.getBytes(), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }


}
