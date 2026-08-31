package co.edu.unbosque.wheeltrees;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import co.edu.unbosque.wheeltrees.service.AuthService;
import co.edu.unbosque.wheeltrees.service.EmailService;

@SpringBootApplication
public class EmailTestApplication {

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(EmailTestApplication.class, args);
		 
		EmailService emailService = context.getBean(EmailService.class);

//		emailService.enviarOtpVerificacion("fabianandrey05@gmail.com", "Fabian", "123456");
		emailService.enviarOtpVerificacion("fmontano@unbosque.edu.co", "Fabian", "111111");
		emailService.enviarOtpVerificacion("fcipamocha@unbosque.edu.co", "Fabian", "222222");
		emailService.enviarOtpVerificacion("jvcontreras@unbosque.edu.co", "Fabian", "333333");
		emailService.enviarOtpVerificacion("cbedoyal@unbosque.edu.co", "Fabian", "444444");
		emailService.enviarOtpVerificacion("atortizm@unbosque.edu.co", "Fabian", "555555");
//		emailService.enviarOtpVerificacion("fabianandrey05@outlook.com", "Alejandro", "918189");
//		emailService.enviarOtpVerificacion("cbeltranhe@unbosque.edu.co", "Fabian", "918189");
//		emailService.enviarOtpVerificacion("fabianandrey05@outlook.com", "Fabian", "123456");

		System.out.println("Correo enviado (si no hay error)");
	}
}