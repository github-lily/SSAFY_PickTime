package com.b101.pickTime.db.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "stages")
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stageId;

    String description;

}
