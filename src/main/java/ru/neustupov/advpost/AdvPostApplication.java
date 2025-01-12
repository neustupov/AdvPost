package ru.neustupov.advpost;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AdvPostApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvPostApplication.class, args);
    }

}
