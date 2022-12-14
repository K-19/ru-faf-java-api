package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhpEmailService {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+$");
  private final DomainBlacklistRepository domainBlacklistRepository;
  private final FafApiProperties properties;
  private final EmailSender emailSender;
  private final MailBodyBuilder mailBodyBuilder;


  /**
   * Checks whether the specified email address as a valid format and its domain is not blacklisted.
   */
  @Transactional(readOnly = true)
  public void validateEmailAddress(String email) {
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw ApiException.of(ErrorCode.EMAIL_INVALID, email);
    }
    if (domainBlacklistRepository.existsByDomain(email.substring(email.lastIndexOf('@') + 1))) {
      throw ApiException.of(ErrorCode.EMAIL_BLACKLISTED, email);
    }
  }

  public void sendActivationMail(String username, String email, String activationUrl) throws IOException {
    final var mailBody = mailBodyBuilder.buildAccountActivationBody(username, activationUrl);

    sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      properties.getRegistration().getSubject(),
      mailBody
    );
  }

  public void sendWelcomeToFafMail(String username, String email) throws IOException {
    final var mailBody = mailBodyBuilder.buildWelcomeToFafBody(username);

    sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      properties.getRegistration().getWelcomeSubject(),
      mailBody
    );
  }

  public void sendPasswordResetMail(String username, String email, String passwordResetUrl) throws IOException {
    final var mailBody = mailBodyBuilder.buildPasswordResetBody(username, passwordResetUrl);

    sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      properties.getPasswordReset().getSubject(),
      mailBody
    );
  }

  private void sendMail(String fromEmail, String fromName, String toEmail, String subject, String content) throws IOException {
    URL url = new URL(properties.getMail().getPhpFileUrl());
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setDoOutput(true);

    EmailData data = new EmailData(fromEmail, fromName, toEmail, subject, content);
    ObjectMapper objectMapper = new ObjectMapper();
    log.info("Send mail to " + toEmail + " from " + fromName + " (" + fromEmail + "), subject: " + subject);
    String myJsonString = objectMapper.writeValueAsString(data);

    OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
    osw.write(myJsonString);
    osw.flush();

    int responseCode = connection.getResponseCode();
    log.info("Response Code : " + responseCode);

    osw.close();
  }
}
