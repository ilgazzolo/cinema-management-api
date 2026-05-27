package com.api.boleteria.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RewardClaimDetailDTO {

    private Long id;
    private String username;
    private String rewardName;
    private String code;
}