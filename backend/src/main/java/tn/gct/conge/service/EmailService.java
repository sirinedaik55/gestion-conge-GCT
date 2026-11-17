package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("portail-conge@gct.tn");
            message.setTo(destinataire);
            message.setSubject("[GCT Congés] " + sujet);
            message.setText(contenu);
            mailSender.send(message);
            log.info("Email envoyé avec succès à {}", destinataire);
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email à {} (mode démo ou SMTP non configuré) : {}", destinataire, e.getMessage());
        }
    }
}
