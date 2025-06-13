package org.example.trainingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OCF {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    //    @NotBlank(message = "Le nom de l'entreprise ne peut pas être vide")
//    @Column(name = "corporate_name", nullable = false)
    private String corporateName;

    //    @NotBlank(message = "L'adresse ne peut pas être vide")
//    @Column(nullable = false)
    private String address;

    //    @NotBlank(message = "Le téléphone ne peut pas être vide")
//    @Pattern(regexp = "^[0-9]{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres")
//    @Column(nullable = false)
    private String phone;

    //    @NotBlank(message = "L'email ne peut pas être vide")
//    @Email(message = "L'email doit être valide")
//    @Column(nullable = false)
    private String email;

    //    @Column
    private String website;

    //    @Column
    private String staff;

    //    @Column(name = "creation_date")
    private LocalDate creationDate;

    //    @Column(name = "legal_form")
    private String legalForm;

    //    @NotBlank(message = "L'ICE ne peut pas être vide")
//    @Column(name = "ice", unique = true, nullable = false)
    private String ice;

    //    @Column(name = "rc")
    private String rc;

    //    @Column(name = "patent")
    private String patent;

    //    @Column(name = "if")
    private String ifValue; // 'if' est un mot réservé en Java, donc on renomme

    //    @Column(name = "cnss")
    private String cnss;

    //    @Column(name = "permanent_staff")
    private Integer permanentStaff;

    // Informations sur le représentant légal
//    @Column(name = "name_legal_representant")
    private String nameLegalRepresentant;

    //    @Column(name = "position_legal_representant")
    private String positionLegalRepresentant;

    //    @Column(name = "phone_legal_representant")
    private String phoneLegalRepresentant;

    //    @Column(name = "email_legal_representant")
    private String emailLegalRepresentant;

    // Informations sur le contact principal
//    @Column(name = "name_main_contact")
    private String nameMainContact;

    //    @Column(name = "position_main_contact")
    private String positionMainContact;

    //    @Column(name = "phone_main_contact")
    private String phoneMainContact;

    //    @Column(name = "email_main_contact")
    private String emailMainContact;
}