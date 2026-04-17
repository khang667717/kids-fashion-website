package com.example.kidsfashion.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Dịch vụ gửi email OTP qua Gmail SMTP.
 * Dùng HTML email để trình bày đẹp hơn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi email xác thực OTP tới địa chỉ email người dùng.
     *
     * @param toEmail  địa chỉ email người nhận
     * @param otpPlain mã OTP 6 số (plaintext, chưa hash)
     * @throws MailSendException nếu gửi thất bại → sẽ rollback transaction register
     */
    public void sendOtpEmail(String toEmail, String otpPlain) {
        log.info("[EmailService] Sending OTP email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔐 Mã xác thực tài khoản Kids Fashion");
            helper.setText(buildOtpHtml(otpPlain), true); // true = isHtml

            mailSender.send(message);
            log.info("[EmailService] OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("[EmailService] Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new MailSendException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo nội dung HTML đẹp cho email OTP.
     */
    private String buildOtpHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
                        .container { max-width: 480px; margin: 0 auto; background: #ffffff; border-radius: 16px;
                                     box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                                   padding: 32px; text-align: center; }
                        .header h1 { color: #fff; margin: 0; font-size: 24px; font-weight: 700; }
                        .header p { color: rgba(255,255,255,0.85); margin: 8px 0 0; font-size: 14px; }
                        .body { padding: 32px; text-align: center; }
                        .otp-box { display: inline-block; background: #f0f4ff; border: 2px dashed #667eea;
                                    border-radius: 12px; padding: 16px 40px; margin: 24px 0; }
                        .otp-code { font-size: 42px; font-weight: 900; letter-spacing: 12px;
                                    color: #4f3cc9; font-family: 'Courier New', monospace; }
                        .note { color: #888; font-size: 13px; margin-top: 8px; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px 16px;
                                   border-radius: 4px; margin: 20px 0; text-align: left; font-size: 13px; color: #856404; }
                        .footer { background: #f8f9fa; padding: 20px 32px; text-align: center;
                                   font-size: 12px; color: #aaa; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Kids Fashion</h1>
                            <p>Xác thực địa chỉ email của bạn</p>
                        </div>
                        <div class="body">
                            <p style="color:#555; font-size:15px;">Chào bạn! Đây là mã xác thực OTP của bạn:</p>
                            <div class="otp-box">
                                <div class="otp-code">%s</div>
                                <div class="note">⏱️ Mã có hiệu lực trong <strong>5 phút</strong></div>
                            </div>
                            <div class="warning">
                                ⚠️ <strong>Lưu ý bảo mật:</strong> Không chia sẻ mã này với bất kỳ ai.
                                Kids Fashion sẽ không bao giờ yêu cầu mã OTP qua điện thoại.
                            </div>
                            <p style="color:#888; font-size:13px;">Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.</p>
                        </div>
                        <div class="footer">
                            © 2025 Kids Fashion Vietnam · Thời trang trẻ em cao cấp
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(otp);
    }
}
