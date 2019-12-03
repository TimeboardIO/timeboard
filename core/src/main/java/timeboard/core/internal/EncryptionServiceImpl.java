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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import timeboard.core.api.EncryptionService;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectAttributValue;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Component(
        service = EncryptionService.class,
        immediate = true
)
@org.springframework.stereotype.Component
public class EncryptionServiceImpl implements EncryptionService {

    @Reference
    private LogService logService;

    private static final String SECRET_KEY = "738F26A3C1971235"; //TODO replace by a configuration

    @Override
    public String getProjectAttribute(Project targetProject, String attributeKey) {
        ProjectAttributValue projectAttributValue = targetProject.getAttributes().get(attributeKey);
        String value = projectAttributValue.getValue();
        if (projectAttributValue.isEncrypted()) {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY.getBytes(), "AES"));
                value = new String(cipher.doFinal(Base64.getDecoder().decode(value)));
            } catch (Exception  e) {
                // Catch error empty
            }
        }
        return value;
    }

    @Override
    public String encryptAttribute(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY.getBytes(), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            return null;
        }
    }

    /*
    public static void main(String[] args) {
        String key = "test";
        EncryptionService encryptionService = new EncryptionServiceImpl();
        key = encryptionService.encryptAttribute(key);
        Project p = new Project();
        p.getAttributes().put("TEST", new ProjectAttributValue(key, true));
        System.out.println(key);
        key = encryptionService.getProjectAttribute(p, "TEST");
        System.out.println(key);
    }*/
}
