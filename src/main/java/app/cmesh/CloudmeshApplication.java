package app.cmesh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudmeshApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudmeshApplication.class, args);
	}

}
