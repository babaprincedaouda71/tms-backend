package org.example.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainersDto {
    private Long id;

    private String name;
}