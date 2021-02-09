package com.azrul.ml.scorecard;

import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.annotation.Port;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@KubernetesApplication(ports = @Port(name = "scorecard", containerPort = 18081))
public class ScorecardApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScorecardApplication.class, args);
	}

}
