package org.example.trainingservice;

import org.example.trainingservice.entity.OCF;
import org.example.trainingservice.entity.plan.Plan;
import org.example.trainingservice.enums.PlanStatusEnum;
import org.example.trainingservice.repository.OCFRepository;
import org.example.trainingservice.repository.plan.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TrainingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            OCFRepository ocfRepository,
            PlanRepository planRepository
    ) {
        return args -> {
            OCF ocf = OCF.builder()
                    .corporateName("GALAXY SOLUTIONS")
                    .emailMainContact("babaprince71@gmail.com")
                    .companyId(1L)
                    .build();
            ocfRepository.save(ocf);

            Plan plan = Plan.builder()
                    .title("Plan 2025")
                    .companyId(1L)
                    .isCSFPlan(true)
                    .isOFPPTValidation(false)
                    .year(2025)
                    .startDate(LocalDate.of(2025, 1, 1))
                    .endDate(LocalDate.of(2025, 12, 31))
                    .estimatedBudget(BigDecimal.valueOf(65000))
                    .status(PlanStatusEnum.Non_Planifié)
                    .build();
            planRepository.save(plan);
        };
    }

}