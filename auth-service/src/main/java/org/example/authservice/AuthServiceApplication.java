package org.example.authservice;

import org.example.authservice.entity.Groupe;
import org.example.authservice.entity.User;
import org.example.authservice.repository.AccessRightRepository;
import org.example.authservice.repository.GroupeRepository;
import org.example.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@SpringBootApplication
@EnableFeignClients
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            GroupeRepository groupeRepository,
            AccessRightRepository accessRightRepository) {
        return args -> {
            // create access_right
//            AccessRight accessRight01 = AccessRight.builder()
//                    .page("Utilisateurs")
//                    .action("Consulter_Utilisateurs")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight02 = AccessRight.builder()
//                    .page("Utilisateurs")
//                    .action("Modifier_Utilisateurs")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight03 = AccessRight.builder()
//                    .page("Utilisateurs")
//                    .action("Supprimer_Utilisateurs")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight04 = AccessRight.builder()
//                    .page("Formations")
//                    .action("Consulter_Formations")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight05 = AccessRight.builder()
//                    .page("Formations")
//                    .action("Modifier_Formations")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight06 = AccessRight.builder()
//                    .page("Formations")
//                    .action("Supprimer_Formations")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight07 = AccessRight.builder()
//                    .page("Plan")
//                    .action("Consulter_Plan")
//                    .allowed(true)
//                    .build();
//            AccessRight accessRight08 = AccessRight.builder()
//                    .page("Plan")
//                    .action("Supprimer_Plan")
//                    .allowed(true)
//                    .build();
//            accessRightRepository.save(accessRight01);
//            accessRightRepository.save(accessRight02);
//            accessRightRepository.save(accessRight03);
//            accessRightRepository.save(accessRight04);
//            accessRightRepository.save(accessRight05);
//            accessRightRepository.save(accessRight06);
//            accessRightRepository.save(accessRight07);
//            accessRightRepository.save(accessRight08);

            // create roles
            Groupe groupe01 = Groupe.builder()
                    .companyId(1L)
                    .name("Formateur")
                    .description("Formateur")
//                    .accessRights(Set.of(accessRight04, accessRight07))
                    .build();
            Groupe groupe02 = Groupe.builder()
                    .companyId(1L)
                    .name("Admin")
                    .description("Admin")
//                    .accessRights(Set.of(accessRight01, accessRight02, accessRight03))
                    .build();
            Groupe groupe03 = Groupe.builder()
                    .companyId(1L)
                    .name("Collaborateur")
                    .description("Collaborateur")
//                    .accessRights(Set.of(accessRight04, accessRight07))
                    .build();
            Groupe groupe04 = Groupe.builder()
                    .companyId(1L)
                    .name("Manager")
                    .description("Manager")
//                    .accessRights(Set.of(accessRight01, accessRight04, accessRight05, accessRight07))
                    .build();
            Groupe groupe05 = Groupe.builder()
                    .companyId(2L)
                    .name("Admin")
                    .description("Admin")
//                    .accessRights(Set.of(accessRight01, accessRight04, accessRight05, accessRight07))
                    .build();

            Groupe groupe06 = Groupe.builder()
                    .companyId(1L)
                    .name("Employé")
                    .description("Employé")
//                    .accessRights(Set.of(accessRight01, accessRight04, accessRight05, accessRight07))
                    .build();
            groupeRepository.save(groupe01);
            groupeRepository.save(groupe02);
            groupeRepository.save(groupe03);
            groupeRepository.save(groupe04);
            groupeRepository.save(groupe05);
            groupeRepository.save(groupe06);

            // create users
            User user = User.builder()
                    .email("iambabaprince@gmail.com")
                    .firstName("Prince")
                    .lastName("Coulibaly")
                    .password(passwordEncoder.encode("0112"))
                    .creationDate(LocalDate.now().toString())
                    .active(true)
                    .status("Actif")
                    .role("Collaborateur")
                    .managerId(2L)
                    .groupe(groupe03)
                    .department("Département Technique")
                    .companyId(1L)
                    .firstLogin(true)
                    .build();
            User user2 = User.builder()
                    .email("thomasjudejunior@gmail.com")
                    .firstName("Thomas Junior")
                    .lastName("Jude")
                    .department("Direction Administrative")
                    .password(passwordEncoder.encode("0112"))
                    .creationDate(LocalDate.now().toString())
                    .active(true)
                    .status("Actif")
                    .role("Manager")
                    .groupe(groupe04)
                    .firstLogin(true)
                    .companyId(1L)
                    .build();
            User user3 = User.builder()
                    .email("babaprince71@gmail.com")
                    .firstName("Baba Daouda")
                    .lastName("Prince")
                    .address("Apt1 GH1 Imm 16 Andalous, Mohammedia, Maroc")
                    .birthDate(LocalDate.now().toString())
                    .phoneNumber("+212693823094")
                    .cin("BK12273Z")
                    .gender("Homme")
                    .hiringDate(LocalDate.now().toString())
                    .socialSecurityNumber("58746320145")
                    .collaboratorCode("GS-022151")
                    .position("DG")
                    .department("Direction Administrative")
                    .companyId(1L)
                    .password(passwordEncoder.encode("0112"))
                    .creationDate(LocalDate.now().toString())
                    .active(true)
                    .firstLogin(true)
                    .status("Actif")
                    .role("Admin")
                    .groupe(groupe02)
                    .build();
            User user4 = User.builder()
                    .email("sandra@gmail.com")
                    .firstName("Sandra")
                    .lastName("Aka")
                    .address("Apt1 GH1 Imm 16 Andalous, Mohammedia, Maroc")
                    .birthDate(LocalDate.now().toString())
                    .phoneNumber("+212693823094")
                    .cin("BK12273Q")
                    .managerId(2L)
                    .gender("Femme")
                    .hiringDate(LocalDate.now().toString())
                    .socialSecurityNumber("58746320145")
                    .collaboratorCode("ATZ-022151")
                    .position("DRH")
                    .department("Service Commercial")
                    .companyId(1L)
                    .password(passwordEncoder.encode("0112"))
                    .creationDate(LocalDate.now().toString())
                    .active(true)
                    .firstLogin(true)
                    .status("Actif")
                    .role("Collaborateur")
                    .groupe(groupe03)
                    .build();

            User user5 = User.builder()
                    .email("boris@gmail.com")
                    .firstName("Boris")
                    .lastName("Samne")
                    .address("Boukhalef, Tanger, Maroc")
                    .birthDate("2023-05-01")
                    .phoneNumber("+212693823094")
                    .cin("BK12273Q")
                    .managerId(2L)
                    .gender("Homme")
                    .hiringDate(LocalDate.now().toString())
                    .socialSecurityNumber("58746320145")
                    .collaboratorCode("ATZ-022151")
                    .position("Agent Back office")
                    .department("Service Client")
                    .companyId(1L)
                    .password(passwordEncoder.encode("0112"))
                    .creationDate(LocalDate.now().toString())
                    .active(true)
                    .firstLogin(true)
                    .status("Actif")
                    .role("Employé")
                    .groupe(groupe06)
                    .build();
            userRepository.save(user);
            userRepository.save(user2);
            userRepository.save(user3);
            userRepository.save(user4);
            userRepository.save(user5);

            // create more collaborators
//            User collaborateur1 = User.builder()
//                    .email("collaborateur1@example.com")
//                    .firstName("Ali")
//                    .lastName("Ahmed")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Collaborateur")
//                    .groupe(groupe03)
//                    .companyId(1L)
//                    .managerId(9L)
//                    .firstLogin(true)
//                    .build();
//            User collaborateur2 = User.builder()
//                    .email("collaborateur2@example.com")
//                    .firstName("Fatima")
//                    .lastName("Zahra")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Collaborateur")
//                    .groupe(groupe03)
//                    .companyId(1L)
//                    .managerId(10L)
//                    .firstLogin(true)
//                    .build();
//            User collaborateur3 = User.builder()
//                    .email("collaborateur3@example.com")
//                    .firstName("Youssef")
//                    .lastName("Rachid")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Collaborateur")
//                    .groupe(groupe03)
//                    .companyId(1L)
//                    .managerId(11L)
//                    .firstLogin(true)
//                    .build();
//            User collaborateur4 = User.builder()
//                    .email("collaborateur4@example.com")
//                    .firstName("Khadija")
//                    .lastName("Omar")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Collaborateur")
//                    .groupe(groupe03)
//                    .companyId(12L)
//                    .firstLogin(true)
//                    .build();
//
//            userRepository.save(collaborateur1);
//            userRepository.save(collaborateur2);
//            userRepository.save(collaborateur3);
//            userRepository.save(collaborateur4);
//
//            // create more managers
//            User manager1 = User.builder()
//                    .email("manager1@example.com")
//                    .firstName("Said")
//                    .lastName("Karim")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Manager")
//                    .groupe(groupe04)
//                    .companyId(1L)
//                    .firstLogin(true)
//                    .build();
//            User manager2 = User.builder()
//                    .email("manager2@example.com")
//                    .firstName("Leila")
//                    .lastName("Fahd")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Manager")
//                    .groupe(groupe04)
//                    .companyId(1L)
//                    .firstLogin(true)
//                    .build();
//            User manager3 = User.builder()
//                    .email("manager3@example.com")
//                    .firstName("Hassan")
//                    .lastName("Jawad")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Manager")
//                    .groupe(groupe04)
//                    .companyId(1L)
//                    .firstLogin(true)
//                    .build();
//            User manager4 = User.builder()
//                    .email("manager4@example.com")
//                    .firstName("Nadia")
//                    .lastName("Kamal")
//                    .password(passwordEncoder.encode("0112"))
//                    .creationDate(LocalDate.now().toString())
//                    .active(true)
//                    .status("Actif")
//                    .role("Manager")
//                    .groupe(groupe04)
//                    .companyId(1L)
//                    .firstLogin(true)
//                    .build();
//
//            userRepository.save(manager1);
//            userRepository.save(manager2);
//            userRepository.save(manager3);
//            userRepository.save(manager4);
        };
    }

}