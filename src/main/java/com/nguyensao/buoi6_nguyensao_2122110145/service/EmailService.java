package com.nguyensao.buoi6_nguyensao_2122110145.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.mailSender = javaMailSender;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực đăng ký");
            long otpTokenExpirationNew = (3 * 60) / 60;

            String htmlContent = "<html><body>"
                    + "<h2 style='color: #4CAF50;'>Mã xác thực đăng ký</h2>"
                    + "<p style='font-size: 16px;'>Mã xác thực của bạn là: <strong style='font-size: 20px; color: #FF5722;'>"
                    + code + "</strong></p>"
                    + "<p style='font-size: 14px;'>Mã này sẽ hết hạn sau " + otpTokenExpirationNew
                    + " phút. Vui lòng nhập mã trong thời gian quy định.</p>"
                    + "<hr>"
                    + "<p style='font-size: 12px; color: #888;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {

        }
    }
}