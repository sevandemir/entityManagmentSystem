package org.eventhub.eventhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EntityScan("org.eventhub.eventhub.entity")
@EnableJpaRepositories("org.eventhub.eventhub.repo")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class EventHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventHubApplication.class, args);

    }

}
