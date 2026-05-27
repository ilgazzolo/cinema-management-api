package com.api.boleteria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reward_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String rewardName;

    private LocalDateTime claimDate;

   private Integer pointsRequired;
   private String code;
}
