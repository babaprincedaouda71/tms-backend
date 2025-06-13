package org.example.companyservice;

import org.example.companyservice.entity.*;
import org.example.companyservice.enums.TrainingRoomEnum;
import org.example.companyservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CompanyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            CompanyRepository companyRepository,
            StrategicAxesRepository strategicAxesRepository,
            SiteRepository siteRepository,
            DepartmentRepository departmentRepository,
            DomainRepository domainRepository,
            QualificationRepository qualificationRepository
    ) {
        return args -> {
            Company company01 = Company.builder()
                    .name("GALAXY SOLUTIONS")
                    .registrationCompleted(true)
                    .cnssNumber("0123456987")
                    .employees("0 - 10")
                    .iceNumber("987654321")
                    .mainContactFirstName("Baba")
                    .mainContactLastName("Prince")
                    .mainContactEmail("babaprince71@gmail.com")
                    .mainContactPhone("+212693823094")
                    .mainContactRole("DG")
                    .legalContactRole("DG")
                    .sector("Services")
                    .status("Accepte")
                    .legalContactFirstName("Baba")
                    .legalContactLastName("Prince")
                    .build();
            Company company02 = Company.builder()
                    .name("ALTRAZ")
                    .registrationCompleted(true)
                    .cnssNumber("0123456987")
                    .employees("0 - 10")
                    .iceNumber("987654321")
                    .mainContactFirstName("Thomas")
                    .mainContactLastName("Jude Junior")
                    .mainContactEmail("thomasjudejunior@gmail.com")
                    .mainContactPhone("+212693823094")
                    .mainContactRole("CEO")
                    .legalContactRole("CEO")
                    .sector("Services")
                    .status("Accepte")
                    .legalContactFirstName("Thomas")
                    .legalContactLastName("Jude Junior")
                    .build();
            companyRepository.save(company01);
            companyRepository.save(company02);

            Random random = new Random();
            long companyId = 1L; // Exemple d'ID d'entreprise

            List<String> strategicAxisTitles = Arrays.asList("Expansion Marché", "Innovation Produit", "Optimisation Opérationnelle", "Engagement Client", "Développement Durable");
            List<String> siteLabels = Arrays.asList("Siège Social Casablanca", "Centre de Formation Rabat", "Antenne Commerciale Marrakech", "Unité de Production Tanger", "Plateforme Logistique Agadir");
            List<String> siteCodes = Arrays.asList("CASA-HQ", "RABAT-CF", "MARRA-CO", "TANGE-UP", "AGAD-PL");
            List<String> addresses = Arrays.asList("12 Rue des Nations Unies, Casablanca", "5 Avenue Hassan II, Rabat", "Quartier Industriel, Marrakech", "Zone Franche, Tanger", "Route d'Essaouira, Agadir");
            List<String> cities = Arrays.asList("Casablanca", "Rabat", "Marrakech", "Tanger", "Agadir");
            List<String> domainNames = Arrays.asList("Marketing Digital", "Gestion de Projet", "Ressources Humaines", "Finance d'Entreprise", "Développement Logiciel");
            List<String> domainCodes = Arrays.asList("MKTG", "PROJ", "RH", "FIN", "DEV");
            List<String> departmentNames = Arrays.asList("Service Commercial", "Département Technique", "Direction Administrative", "Pôle Innovation", "Service Client");
            List<String> departmentCodes = Arrays.asList("COM", "TECH", "ADMIN", "INNOV", "CLI");
            List<String> qualificationTypes = Arrays.asList("Certificat Professionnel", "Diplôme d'État", "Habilitation Métier", "Formation Continue", "Accréditation Sectorielle");
            List<String> qualificationCodes = Arrays.asList("CERT-PRO", "DIP-ETAT", "HAB-MET", "FORM-CONT", "ACCRED-SEC");

            // Génération de 5 StrategicAxes aléatoires
            List<StrategicAxes> strategicAxesList = IntStream.rangeClosed(0, 4)
                    .mapToObj(i -> StrategicAxes.builder()
                            .companyId(companyId)
                            .title(strategicAxisTitles.get(i))
                            .year(2024 + i)
                            .build())
                    .toList();
            strategicAxesRepository.saveAll(strategicAxesList);
            System.out.println("5 axes stratégiques réalistes générés.");

            // Génération de 5 Sites aléatoires
            List<Site> siteList = IntStream.rangeClosed(0, 4)
                    .mapToObj(i -> Site.builder()
                            .companyId(companyId)
                            .code(siteCodes.get(i))
                            .label(siteLabels.get(i))
                            .address(addresses.get(i))
                            .city(cities.get(i))
                            .phone("05" + String.format("%08d", random.nextInt(100000000)))
                            .trainingRoom(TrainingRoomEnum.values()[random.nextInt(TrainingRoomEnum.values().length)])
                            .size(30 + random.nextInt(120))
                            .build())
                    .toList();
            siteRepository.saveAll(siteList);
            System.out.println("5 sites réalistes générés.");

            // Génération de 5 Domains aléatoires
            List<Domain> domainList = IntStream.rangeClosed(0, 4)
                    .mapToObj(i -> Domain.builder()
                            .companyId(companyId)
                            .code(domainCodes.get(i))
                            .name(domainNames.get(i))
                            .build())
                    .toList();
            domainRepository.saveAll(domainList);
            System.out.println("5 domaines réalistes générés.");

            // Génération de 5 Departments aléatoires
            List<Department> departmentList = IntStream.rangeClosed(0, 4)
                    .mapToObj(i -> Department.builder()
                            .companyId(companyId)
                            .code(departmentCodes.get(i))
                            .name(departmentNames.get(i))
                            .build())
                    .toList();
            departmentRepository.saveAll(departmentList);
            System.out.println("5 départements réalistes générés.");

            // Génération de 5 Qualifications aléatoires
            List<Qualification> qualificationList = IntStream.rangeClosed(0, 4)
                    .mapToObj(i -> Qualification.builder()
                            .companyId(companyId)
                            .code(qualificationCodes.get(i))
                            .type(qualificationTypes.get(i))
                            .validityNumber(random.nextInt(5) + 1)
                            .validityUnit(Qualification.ValidityUnit.values()[random.nextInt(Qualification.ValidityUnit.values().length)])
                            .reminderNumber(random.nextInt(3) + 1)
                            .reminderUnit(Qualification.ReminderUnit.values()[random.nextInt(Qualification.ReminderUnit.values().length)])
                            .build())
                    .toList();
            qualificationRepository.saveAll(qualificationList);
            System.out.println("5 qualifications réalistes générées.");
        };
    }

}