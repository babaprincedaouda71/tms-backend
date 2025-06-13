package org.example.trainingservice.repository.plan;

import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingGroupeRepository extends JpaRepository<TrainingGroupe, Long> {
}