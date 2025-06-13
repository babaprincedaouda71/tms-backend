package org.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AccessRight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String page;

    private String action;

    private boolean allowed;
}