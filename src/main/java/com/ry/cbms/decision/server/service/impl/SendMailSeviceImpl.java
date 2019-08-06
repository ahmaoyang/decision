package com.ry.cbms.decision.server.service.impl;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.ry.cbms.decision.server.service.SendMailSevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * @Author maoYang
 * @Date 2019/5/20 9:50
 * @Description 发送邮件服务
 */
@Service
@Slf4j
public class SendMailSeviceImpl implements SendMailSevice {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String serverMail;

    @Override
    public void sendMail(List<String> toUser, String subject, String text) {
        MimeMessage message = javaMailSender.createMimeMessage ();

        try {
            MimeMessageHelper helper = new MimeMessageHelper (message, true);
            helper.setFrom (serverMail);
            helper.setTo (toUser.toArray (new String[toUser.size ()]));
            helper.setSubject (subject);
            helper.setText (text, true);
            javaMailSender.send (message);
        } catch (MessagingException e) {
            log.error ("发送邮件失败{}", e);
        }

    }

    @Override
    public void sendMail(String toUser, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage ();

        MimeMessageHelper helper = new MimeMessageHelper (message, true);
        helper.setFrom (serverMail);
        helper.setTo (toUser);
        helper.setSubject (subject);
        helper.setText (text, true);
        javaMailSender.send (message);
    }
}
