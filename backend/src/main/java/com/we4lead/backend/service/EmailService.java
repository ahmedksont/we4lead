package com.we4lead.backend.service;

import com.we4lead.backend.dto.DemandeResponse;
import com.we4lead.backend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDemandeToMedecin(User medecin, DemandeResponse demande, String etudiantInfo, String universiteInfo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(medecin.getEmail());
        message.setSubject("Nouvelle demande d'étudiant - " + demande.getTypeSituation());

        String emailBody = String.format("""
            Bonjour Dr %s %s,
            
            Un étudiant a soumis une nouvelle demande vous concernant.
            
            📋 DÉTAILS DE LA DEMANDE :
            ------------------------
            Type de situation : %s
            Description : %s
            Lieu : %s
            Période : %s
            Date de soumission : %s
            
            👤 INFORMATIONS DE L'ÉTUDIANT :
            -----------------------------
            %s
            
            📚 UNIVERSITÉ DE L'ÉTUDIANT :
            ---------------------------
            %s
            
            ⚠️ Note : Cette demande vous est transmise indépendamment de votre université d'appartenance.
            
            Veuillez prendre contact avec l'étudiant pour faire un suivi de cette situation.
            
            Cordialement,
            L'équipe de soutien aux étudiants
            """,
                medecin.getPrenom(),
                medecin.getNom(),
                demande.getTypeSituation(),
                demande.getDescription(),
                demande.getLieuPrincipal() != null ? demande.getLieuPrincipal() : "Non spécifié",
                demande.getPeriode(),
                demande.getDateCreation().toString(),
                etudiantInfo,
                universiteInfo
        );

        message.setText(emailBody);
        mailSender.send(message);
    }

    public void sendDemandeConfirmationToEtudiant(String etudiantEmail, DemandeResponse demande, User medecin) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(etudiantEmail);
        message.setSubject("Confirmation de votre demande - " + demande.getTypeSituation());

        String medecinInfo = medecin != null
                ? String.format("Dr %s %s (%s)", medecin.getPrenom(), medecin.getNom(), medecin.getEmail())
                : "Un médecin sera assigné prochainement";

        String emailBody = String.format("""
            Bonjour %s %s,
            
            Nous avons bien reçu votre demande concernant une situation de %s.
            
            RÉCAPITULATIF DE VOTRE DEMANDE :
            --------------------------------
            Type : %s
            Description : %s
            Lieu : %s
            Période : %s
            Date de soumission : %s
            
            MÉDECIN CONCERNÉ :
            -----------------
            %s
            
            Votre demande a été transmise au médecin indiqué.
            Il vous contactera directement sur cette adresse email.
            
            Cordialement,
            L'équipe de soutien aux étudiants
            """,
                demande.getEtudiantPrenom(),
                demande.getEtudiantNom(),
                demande.getTypeSituation(),
                demande.getTypeSituation(),
                demande.getDescription(),
                demande.getLieuPrincipal() != null ? demande.getLieuPrincipal() : "Non spécifié",
                demande.getPeriode(),
                demande.getDateCreation().toString(),
                medecinInfo
        );

        message.setText(emailBody);
        mailSender.send(message);
    }
}