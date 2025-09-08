package com.war.game.war_backend.model;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "territory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Territory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String continent;

    @OneToMany(mappedBy = "territoryA")
    private List<TerritoryBorder> bordersA;

    @OneToMany(mappedBy = "territoryB")
    private List<TerritoryBorder> bordersB;

    public Set<TerritoryBorder> getAllBorders() {
        Set<TerritoryBorder> all = new HashSet<>();
        if (bordersA != null) all.addAll(bordersA);
        if (bordersB != null) all.addAll(bordersB);
        return all;
    }
}