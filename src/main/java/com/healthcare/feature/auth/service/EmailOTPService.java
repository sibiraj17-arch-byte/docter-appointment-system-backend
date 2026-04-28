package com.healthcare.feature.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailOTPService {

    private static final Logger logger = LoggerFactory.getLogger(EmailOTPService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;
    private final String mailUsername;
    private final int otpExpirationMinutes;

    public EmailOTPService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String fromAddress,
            @Value("${app.mail.from-name:Medicore Healthcare}") String fromName,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${otp.expiration-minutes:5}") int otpExpirationMinutes
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.mailUsername = mailUsername;
        this.otpExpirationMinutes = otpExpirationMinutes;
    }

    public void sendLoginOtp(String recipientEmail, String recipientName, String otp) {
        if (recipientEmail == null || recipientEmail.isBlank() || !recipientEmail.contains("@")) {
            throw new IllegalArgumentException("Valid email is required to send OTP");
        }

        if (mailUsername == null || mailUsername.isBlank()) {
            logger.info("Mail username not configured. Dev OTP for {} is {}", recipientEmail, otp);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress, fromName);
            }

            helper.setTo(recipientEmail);
            helper.setSubject("Your Medicore OTP Code");
            helper.setText(buildOtpEmail(recipientName, otp), true);
            mailSender.send(message);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException ex) {
            logger.error("Failed to send OTP email to {}", recipientEmail, ex);
            throw new IllegalStateException("Unable to send OTP email right now. Please try again.", ex);
        }
    }

    private String buildOtpEmail(String recipientName, String otp) {
        String safeName = (recipientName == null || recipientName.isBlank()) ? "User" : recipientName;
        int expirySeconds = otpExpirationMinutes * 60;
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                  <style>
                    @media (prefers-color-scheme: dark) {
                      .bg-body { background:#071726 !important; }
                      .card { background:#10283d !important; border-color:#1f425f !important; }
                      .text-main { color:#f8fbff !important; }
                      .text-sub { color:#d4dfeb !important; }
                      .otp-box { background:#0b2a4a !important; border-color:#1f425f !important; color:#f8fbff !important; }
                      .brand-chip { background:#123b6e !important; color:#7de2d5 !important; }
                    }
                  </style>
                </head>
                <body style="margin:0; padding:0;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-body" style="background:#f4f7fc; padding:24px 10px;">
                    <tr>
                      <td align="center">
                        <table cellpadding="0" cellspacing="0" border="0" class="card" style="width:100%%; max-width:460px; background:#ffffff; border-radius:22px; border:1px solid #dae4f0; box-shadow:0 18px 40px rgba(11,42,74,0.08);">
                          <tr>
                            <td style="padding:32px 24px; font-family:Arial, sans-serif; text-align:center;">
                              <div class="brand-chip" style="display:inline-block; padding:8px 14px; border-radius:999px; background:#e0f0fa; color:#2a9d8f; font-size:12px; font-weight:700; letter-spacing:0.12em; text-transform:uppercase; margin-bottom:16px;">
                                Medicore Secure Access
                              </div>
                              <div class="text-main" style="font-size:28px; font-weight:800; color:#0b2a4a; margin-bottom:8px;">
                                Verify your login
                              </div>
                              <div class="text-sub" style="font-size:14px; color:#486f94; line-height:1.7; margin-bottom:18px;">
                                Hello %s, use the one-time password below to continue signing in to your doctor appointment system.
                              </div>
                              <table align="center" cellpadding="0" cellspacing="0" class="otp-box" style="border:1px solid #dae4f0; border-radius:16px; background:#eff6ff; margin-bottom:18px;">
                                <tr>
                                  <td style="font-size:30px; font-weight:800; letter-spacing:8px; padding:16px 22px; color:#0b2a4a;">
                                    %s
                                  </td>
                                </tr>
                              </table>
                              <div class="text-sub" style="font-size:13px; color:#5f7d9c; margin-bottom:8px;">
                                This code expires in <b>%d seconds</b>
                              </div>
                              <div class="text-sub" style="font-size:12px; color:#94a3b8; margin-bottom:20px;">
                                Never share this code with anyone, including support staff.
                              </div>
                              <div style="height:1px; background:linear-gradient(to right, transparent, #dae4f0, transparent); margin:20px 0;"></div>
                              <div class="text-sub" style="font-size:12px; color:#94a3b8; line-height:1.7;">
                                If you did not request this OTP, you can safely ignore this email.
                              </div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, otp, expirySeconds);
    }
}
