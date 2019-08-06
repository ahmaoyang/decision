package com.ry.cbms.decision.server.service;

import java.util.List;

import com.ry.cbms.decision.server.model.Mail;

public interface MailService {

	void save(Mail mail, List<String> toUser);
}
