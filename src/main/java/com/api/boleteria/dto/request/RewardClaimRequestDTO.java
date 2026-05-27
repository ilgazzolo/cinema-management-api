package com.api.boleteria.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardClaimRequestDTO {

    private String rewardName;

    private Integer pointsRequired;
}