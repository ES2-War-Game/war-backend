package com.war.game.war_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private CardType type;

  @ManyToOne
  @JoinColumn(name = "territory_id", referencedColumnName = "pk_id")
  private Territory territory;
  
  @Transient
  public String getTerritoryName() {
    return territory != null ? territory.getName() : null;
  }
}