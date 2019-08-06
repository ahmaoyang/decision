package com.ry.cbms.decision.server.service.impl;

import java.util.Date;
import java.util.List;

import com.ry.cbms.decision.server.model.Mail;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.service.MailService;
import com.ry.cbms.decision.server.service.SendMailSevice;
import com.ry.cbms.decision.server.utils.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ry.cbms.decision.server.dao.MailDao;

/**
 * @Author maoYang
 * @Date 2019/5/21 19:50
 * @Description 邮件服务
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger ("adminLogger");

    @Autowired
    private SendMailSevice sendMailSevice;
    @Autowired
    private MailDao mailDao;

    @Override
    @Transactional
    public void save(Mail mail, List<String> toUser) {
        SysUser sysUser = UserUtil.getLoginUser ();
        if (null != sysUser) {
            mail.setUserId (UserUtil.getLoginUser ().getId ()); //这个后期去掉
        }
        Date currDate = new Date ();
        mail.setCreateTime (currDate);
        mail.setUpdateTime (currDate);
        mailDao.save (mail);
        toUser.forEach (u -> {
            int status = 1;
            try {
                sendMailSevice.sendMail (u, mail.getSubject (), mail.getContent ());
            } catch (Exception e) {
                log.error ("发送邮件失败{}", e);
                status = 0;
            }

            mailDao.saveToUser (mail.getId (), u, status);
        });

    }

}
