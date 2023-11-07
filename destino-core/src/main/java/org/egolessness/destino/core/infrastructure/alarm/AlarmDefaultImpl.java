/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egolessness.destino.core.infrastructure.alarm;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.enumration.ProjectMessages;
import org.egolessness.destino.core.fixedness.PropertiesFactory;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * implement of alarm
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AlarmDefaultImpl implements Alarm {

    private final Properties mailProperties = new Properties();

    private final boolean mailEnabled;

    @Inject
    public AlarmDefaultImpl(final PropertiesFactory propertiesFactory) {
        setMailProperties(propertiesFactory);
        this.mailEnabled = Boolean.parseBoolean(mailProperties.getProperty("mail.enabled"));
    }

    private void setMailProperties(PropertiesFactory propertiesFactory) {
        JsonNode destinoNode = propertiesFactory.path("destino");
        if (destinoNode.isMissingNode()) {
            return;
        }

        JsonNode alarmNode = destinoNode.path("alarm");
        if (alarmNode.isMissingNode()) {
            return;
        }

        JsonNode mailNode = alarmNode.path("mail");
        if (alarmNode.isMissingNode()) {
            return;
        }

        setMailProperties(mailNode, "mail");

        if (!mailProperties.containsKey("mail.smtp.auth")) {
            String user = mailProperties.getProperty("mail.smtp.user");
            String pass = mailProperties.getProperty("mail.smtp.pass");
            if (PredicateUtils.isNotBlank(user) && PredicateUtils.isNotBlank(pass)) {
                mailProperties.setProperty("mail.smtp.auth", "true");
            }
        }
    }

    private void setMailProperties(JsonNode jsonNode, String... paths) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode node = entry.getValue();

            String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[paths.length] = entry.getKey();

            if (node.isObject()) {
                setMailProperties(node, newPaths);
            } else if (node.isTextual()) {
                mailProperties.put(Mark.DOT.join(newPaths), node.asText());
            } else if (!node.isNull()) {
                mailProperties.put(Mark.DOT.join(newPaths), node.toPrettyString());
            }
        }
    }

    private MimeMessage getMimeMessage() {
        Session session = Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String user = mailProperties.getProperty("mail.smtp.user");
                String pass = mailProperties.getProperty("mail.smtp.pass");
                if (PredicateUtils.isNotBlank(user) && PredicateUtils.isNotBlank(pass)) {
                    return new PasswordAuthentication(user, pass);
                }
                return super.getPasswordAuthentication();
            }
        });

        return new MimeMessage(session);
    }

    @Override
    public void sendEmail(List<String> toMails, List<String> ccMails, String subject, String message) throws Exception {

        if (!mailEnabled || PredicateUtils.isEmpty(toMails)) {
            return;
        }

        InternetAddress[] toAddresses = new InternetAddress[toMails.size()];
        for (int i = 0, j = toMails.size(); i < j; i++) {
            toAddresses[i] = new InternetAddress(toMails.get(i));
        }

        MimeMessage mimeMessage = getMimeMessage();

        InternetAddress addresses = new InternetAddress(mailProperties.getProperty("mail.smtp.from"),
                ProjectMessages.FULL_NAME.getValue(), StandardCharsets.UTF_8.toString());
        mimeMessage.setFrom(addresses);

        mimeMessage.setRecipients(Message.RecipientType.TO, toAddresses);

        if (PredicateUtils.isNotEmpty(ccMails)) {
            InternetAddress[] ccAddresses = new InternetAddress[ccMails.size()];
            for (int i = 0, j = ccMails.size(); i < j; i++) {
                ccAddresses[i] = new InternetAddress(ccMails.get(i));
            }
            mimeMessage.setRecipients(Message.RecipientType.CC, ccAddresses);
        }

        mimeMessage.setSubject(subject);
        mimeMessage.setText(message);
        mimeMessage.setSentDate(new Date());

        Transport.send(mimeMessage);
    }

}
