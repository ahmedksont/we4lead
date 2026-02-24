package com.we4lead.backend.service;

import com.we4lead.backend.dto.DemandeResponse;
import com.we4lead.backend.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.logo-path:logo.png}")
    private String logoPath;

    @Value("${app.email.logo-cid:we4leadLogo}")
    private String logoCid;

    @Value("${app.email.logo-type:image/png}")
    private String logoType;

    private static final String PLATFORM_NAME = "We4Lead – Soutien aux étudiants et professeurs";
    private static final String SUPPORT_EMAIL = "support@we4lead.com";
    private static final String WEBSITE_URL   = "https://www.we4lead.com";

    // Formatteur de date français élégant et lisible
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd MMMM yyyy 'à' HH 'h' mm", Locale.FRENCH);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String getEmailFooterHtml() {
        return """
            <hr style="border:0; border-top:1px solid #e0e0e0; margin:24px 0 16px;">
            <div style="font-size:13px; color:#666; text-align:center; line-height:1.5;">
                %s<br>
                Contact : %s | <a href="%s" style="color:#0066cc; text-decoration:none;">%s</a><br><br>
                <em>Ce message est confidentiel et destiné exclusivement à son destinataire.<br>
                Il peut contenir des informations protégées par le secret médical ou professionnel.<br>
                Si vous n'êtes pas le destinataire prévu, merci de supprimer ce message et de nous en informer immédiatement.</em>
            </div>
            <p style="font-size:12px; color:#888; text-align:center; margin-top:12px;">
                Merci de votre confiance.
            </p>
            """.formatted(PLATFORM_NAME, SUPPORT_EMAIL, WEBSITE_URL, WEBSITE_URL);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        try {
            Resource logoResource = new ClassPathResource(logoPath);
            if (logoResource.exists()) {
                helper.addInline(logoCid, logoResource, logoType);
            } else {
                log.warn("Logo non trouvé à l'emplacement : {}", logoPath);
            }
        } catch (Exception e) {
            log.warn("Impossible d'embarquer le logo : {}", e.getMessage());
        }

        mailSender.send(mimeMessage);
    }

    /**
     * Envoie un email au médecin avec les informations de la demande
     * @param medecin Le médecin destinataire
     * @param demande La demande
     * @param userInfo Les informations formatées de l'utilisateur (étudiant ou professeur)
     * @param universiteInfo Les informations de l'université
     */
    public void sendDemandeToMedecin(User medecin, DemandeResponse demande, String userInfo, String universiteInfo) {
        String lieu = demande.getLieuPrincipal() != null ? demande.getLieuPrincipal() : "Non précisé";

        // Formatage de la date
        String dateSoumission = demande.getDateCreation()
                .format(DATE_FORMATTER);

        String htmlBody = """
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5;">
                <div style="max-width: 620px; margin: 20px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
                    
                    <!-- Header avec logo -->
                    <div style="background: #f8f9fa; padding: 24px 32px; text-align: center; border-bottom: 1px solid #eee;">
                        <img src="cid:%s" alt="We4Lead" style="max-width: 180px; height: auto; display: block; margin: 0 auto;">
                    </div>
                    
                    <!-- Contenu principal -->
                    <div style="padding: 32px 32px 24px;">
                        <h2 style="color: #1a3c5e; margin-top: 0; font-size: 22px;">Cher Docteur %s %s,</h2>
                        
                        <p>Une demande d'accompagnement vous a été adressée via notre plateforme de soutien.</p>
                        
                        <h3 style="color: #2c3e50; margin: 28px 0 12px; font-size: 18px;">Détails de la demande</h3>
                        <table style="width:100%%; border-collapse: collapse;">
                            <tr><td style="padding:6px 0; font-weight:bold; width:180px;">Type de situation :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Description :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Lieu principal :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Période concernée :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Date de soumission :</td><td>%s</td></tr>
                        </table>
                        
                        <h3 style="color: #2c3e50; margin: 28px 0 12px; font-size: 18px;">Informations concernant la personne</h3>
                        <p style="margin: 0 0 16px;">%s</p>
                        
                        <h3 style="color: #2c3e50; margin: 28px 0 12px; font-size: 18px;">Établissement universitaire</h3>
                        <p style="margin: 0 0 24px;">%s</p>
                        
                        <p>Nous vous remercions par avance pour l’attention que vous porterez à cette situation et pour tout contact que vous pourrez établir avec cette personne.</p>
                        
                        <p style="margin-top: 24px;">Restant à votre disposition pour toute précision.</p>
                        
                        <p style="margin-top: 32px;">Avec nos salutations distinguées,<br>
                        <strong>L’équipe de soutien médical et psychologique</strong></p>
                    </div>
                    
                    <!-- Footer -->
                    %s
                </div>
            </body>
            </html>
            """.formatted(
                logoCid,
                medecin.getPrenom(), medecin.getNom(),
                demande.getTypeSituation(),
                demande.getDescription().replace("\n", "<br>"),
                lieu,
                demande.getPeriode(),
                dateSoumission,
                userInfo.replace("\n", "<br>"),
                universiteInfo.replace("\n", "<br>"),
                getEmailFooterHtml()
        );

        String subject = "Nouvelle demande d'accompagnement – " + demande.getTypeSituation();

        try {
            sendHtmlEmail(medecin.getEmail(), subject, htmlBody);
            log.info("Email envoyé au médecin : {}", medecin.getEmail());
        } catch (MessagingException e) {
            log.error("Échec envoi email au médecin {} : {}", medecin.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Envoie un email de confirmation à l'utilisateur (étudiant ou professeur)
     * @param userEmail L'email de l'utilisateur
     * @param demande La demande
     * @param medecin Le médecin concerné (peut être null)
     * @param userTypeLabel Le libellé du type d'utilisateur ("Étudiant" ou "Professeur")
     */
    public void sendDemandeConfirmationToUser(String userEmail, DemandeResponse demande, User medecin, String userTypeLabel) {
        String lieu = demande.getLieuPrincipal() != null ? demande.getLieuPrincipal() : "Non précisé";

        String medecinInfo = medecin != null
                ? "Docteur %s %s ".formatted(medecin.getPrenom(), medecin.getNom())
                : "Un professionnel de santé sera désigné prochainement";

        // Formatage de la date
        String dateSoumission = demande.getDateCreation()
                .format(DATE_FORMATTER);

        // Adapter le message selon le type d'utilisateur
        String roleMessage = "ETUDIANT".equalsIgnoreCase(userTypeLabel)
                ? "Votre dossier sera traité dans le respect de la confidentialité étudiante."
                : "Votre demande en tant que membre du corps professoral sera traitée avec la considération appropriée.";

        String htmlBody = """
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5;">
                <div style="max-width: 620px; margin: 20px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
                    
                    <!-- Header avec logo -->
                    <div style="background: #f8f9fa; padding: 24px 32px; text-align: center; border-bottom: 1px solid #eee;">
                        <img src="cid:%s" alt="We4Lead" style="max-width: 180px; height: auto; display: block; margin: 0 auto;">
                    </div>
                    
                    <!-- Contenu principal -->
                    <div style="padding: 32px 32px 24px;">
                        <h2 style="color: #1a3c5e; margin-top: 0; font-size: 22px;">Cher(ère) %s %s,</h2>
                        
                        <p>Nous avons bien reçu votre demande concernant une situation de type « %s ».</p>
                        
                        <h3 style="color: #2c3e50; margin: 28px 0 12px; font-size: 18px;">Récapitulatif de votre demande</h3>
                        <table style="width:100%%; border-collapse: collapse;">
                            <tr><td style="padding:6px 0; font-weight:bold; width:180px;">Type de situation :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Description :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Lieu principal :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Période concernée :</td><td>%s</td></tr>
                            <tr><td style="padding:6px 0; font-weight:bold;">Date de soumission :</td><td>%s</td></tr>
                        </table>
                        
                        <h3 style="color: #2c3e50; margin: 28px 0 12px; font-size: 18px;">Professionnel destinataire</h3>
                        <p style="margin: 0 0 24px;">%s</p>
                        
                        <p style="font-style: italic; color: #555; background: #f8f9fa; padding: 12px 16px; border-left: 4px solid #0066cc; margin: 20px 0;">
                            %s
                        </p>
                        
                        <p>Le professionnel de santé concerné devrait prendre contact avec vous directement par email dans les meilleurs délais.</p>
                        
                        <p>N’hésitez pas à nous contacter pour toute question ou précision supplémentaire.</p>
                        
                        <p style="margin-top: 32px;">Veuillez agréer, cher(ère) %s, l’expression de nos salutations attentives.</p>
                    </div>
                    
                    <!-- Footer -->
                    %s
                </div>
            </body>
            </html>
            """.formatted(
                logoCid,
                demande.getUserPrenom(), demande.getUserNom(),
                demande.getTypeSituation(),
                demande.getTypeSituation(),
                demande.getDescription().replace("\n", "<br>"),
                lieu,
                demande.getPeriode(),
                dateSoumission,
                medecinInfo,
                roleMessage,
                userTypeLabel.toLowerCase(),
                getEmailFooterHtml()
        );

        String subject = "Confirmation de votre demande – " + demande.getTypeSituation();

        try {
            sendHtmlEmail(userEmail, subject, htmlBody);
            log.info("Email de confirmation envoyé à {} ({})", userEmail, userTypeLabel);
        } catch (MessagingException e) {
            log.error("Échec envoi email de confirmation à {} : {}", userEmail, e.getMessage(), e);
        }
    }

    // Méthode de rétrocompatibilité pour les étudiants uniquement
    public void sendDemandeConfirmationToEtudiant(String etudiantEmail, DemandeResponse demande, User medecin) {
        sendDemandeConfirmationToUser(etudiantEmail, demande, medecin, "Étudiant");
    }
}