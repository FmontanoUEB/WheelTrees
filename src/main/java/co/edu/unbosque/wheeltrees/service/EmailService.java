package co.edu.unbosque.wheeltrees.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;

	@Async
	public void enviarOtpVerificacion(String destinatario, String nombre, String otp) {
		log.info("📧 Preparando envío SMTP hacia: {}", destinatario);

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom("noreply@wheeltrees.site");
			helper.setTo(destinatario);
			helper.setSubject("Código de verificación — Wheel-Trees");
			helper.setText(construirTextoPlano(nombre, otp), construirHtmlOtp(nombre, otp));

			mailSender.send(message);
			log.info("✅ Correo enviado exitosamente a: {}", destinatario);

		} catch (MessagingException e) {
			log.error("❌ Error enviando correo con SMTP: {}", e.getMessage());
		}
	}

	private String construirTextoPlano(String nombre, String otp) {
		return "Hola " + nombre + ". Tu código de verificación para Wheel-Trees es: " + otp
				+ ". Este código expira en 15 minutos.";
	}

	private String construirHtmlOtp(String nombre, String otp) {
		return """
				<!DOCTYPE html>
				<html lang="es">
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <title>Verificación Wheel-Trees</title>
				</head>
				<body style="margin:0;padding:0;background-color:#f0f2f0;font-family:Helvetica,Arial,sans-serif;">
				  <table width="100%%" border="0" cellpadding="0" cellspacing="0" style="padding:40px 20px;">
				    <tr>
				      <td align="center">
				        <table width="100%%" style="max-width:480px;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e0e0e0;">

				          <!-- HEADER -->
				          <tr>
				            <td style="background:#1a3a2a;padding:32px 28px;text-align:center;">
				              <table border="0" cellpadding="0" cellspacing="0" style="margin:0 auto 6px;">
				                <tr>
				                  <td style="padding-right:10px;vertical-align:middle;">
				                    <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
				                      <circle cx="14" cy="14" r="14" fill="#2d6a4f"/>
				                      <path d="M8 18 Q14 8 20 18" stroke="#74c69d" stroke-width="2.2" fill="none" stroke-linecap="round"/>
				                      <circle cx="14" cy="10" r="2.5" fill="#74c69d"/>
				                    </svg>
				                  </td>
				                  <td style="vertical-align:middle;">
				                    <span style="color:#ffffff;font-size:22px;font-weight:600;letter-spacing:0.5px;">Wheel-Trees</span>
				                  </td>
				                </tr>
				              </table>
				              <p style="color:#74c69d;margin:0;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Universidad El Bosque</p>
				            </td>
				          </tr>

				          <!-- BODY -->
				          <tr>
				            <td style="padding:36px 32px 28px;">
				              <p style="font-size:15px;color:#333333;margin:0 0 6px;">
				                Hola, <strong style="color:#1a3a2a;">%s</strong>.
				              </p>
				              <p style="font-size:14px;color:#666666;line-height:1.7;margin:0 0 28px;">
				                Recibimos una solicitud de acceso a la plataforma de transporte compartido.
				                Usa el código de abajo para verificar tu identidad.
				              </p>

				              <!-- CÓDIGO OTP -->
				              <table width="100%%" border="0" cellpadding="0" cellspacing="0"
				                     style="background:#f4faf6;border:2px dashed #2d6a4f;border-radius:12px;margin-bottom:24px;">
				                <tr>
				                  <td style="padding:24px;text-align:center;">
				                    <p style="font-size:11px;color:#5a9a74;letter-spacing:2px;text-transform:uppercase;margin:0 0 10px;">
				                      Tu código de verificación
				                    </p>
				                    <span style="font-size:36px;font-weight:700;letter-spacing:10px;color:#1a3a2a;font-family:'Courier New',Courier,monospace;">
				                      %s
				                    </span>
				                  </td>
				                </tr>
				              </table>

				              <!-- ADVERTENCIA -->
				              <table width="100%%" border="0" cellpadding="0" cellspacing="0"
				                     style="background:#fff8e6;border-radius:8px;">
				                <tr>
				                  <td style="padding:10px 14px;">
				                    <p style="font-size:12px;color:#9a6820;margin:0;line-height:1.6;">
				                      &#9432; Este código expira en <strong>15 minutos</strong>. No lo compartas con nadie.
				                    </p>
				                  </td>
				                </tr>
				              </table>
				            </td>
				          </tr>

				          <!-- FOOTER -->
				          <tr>
				            <td style="border-top:1px solid #eeeeee;background:#fafafa;padding:18px 32px;text-align:center;">
				              <p style="font-size:11px;color:#aaaaaa;margin:0;line-height:1.8;">
				                Si no solicitaste este código, ignora este mensaje de forma segura.<br>
				                &copy; 2026 Wheel-Trees &middot; Universidad El Bosque, Bogot&aacute;
				              </p>
				            </td>
				          </tr>

				        </table>
				      </td>
				    </tr>
				  </table>
				</body>
				</html>
				"""
				.formatted(nombre, otp);
	}
}