package com.faforever.api.email;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailData {
  private String fromEmail;
  private String fromName;
  private String toEmail;
  private String subject;
  private String content;
}
