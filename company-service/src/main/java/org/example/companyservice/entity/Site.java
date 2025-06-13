package org.example.companyservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.companyservice.enums.TrainingRoomEnum;

import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Site {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long companyId;
    private String code;
    private String label;
    private String address;
    private String city;
    private String phone;
    @Enumerated(EnumType.STRING)
    private TrainingRoomEnum trainingRoom;
    private int size;

    // Relation One-to-Many avec Department
    // mappedBy="site" indique le nom de l'attribut dans la classe Department qui gère la relation
    // cascade=CascadeType.ALL signifie que les opérations (persister, supprimer, etc.) sur Site se propageront aux Department associés
    // orphanRemoval=true signifie que si un Department est retiré de la liste des départements d'un Site, il sera supprimé de la base de données
//    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Department> departments;
}