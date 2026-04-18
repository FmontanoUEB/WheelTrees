WheelTrees — Backend

Plataforma de transporte compartido universitario. Universidad El Bosque, Bogotá.

## Tecnologías
- Java 21 + Spring Boot 3
- H2 (desarrollo) 
- JWT para autenticación
- Brevo SMTP para correos
- Swagger UI
- Lombok Uso de Getters, Setters y constructores Automaticos 

## Configuración

1. Copia el archivo de ejemplo:
   cp src/main/resources/application.properties.example src/main/resources/application.properties

2. Edita application.properties con tus credenciales reales.

3. Ejecuta el proyecto desde Eclipse o con:
   ./mvnw spring-boot:run

## Documentación API
Una vez corriendo, accede a:
http://localhost:8080/swagger-ui.html