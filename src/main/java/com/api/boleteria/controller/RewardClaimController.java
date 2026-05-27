package com.api.boleteria.controller;

import com.api.boleteria.dto.detail.RewardClaimDetailDTO;
import com.api.boleteria.dto.request.RewardClaimRequestDTO;
import com.api.boleteria.service.RewardClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardClaimController {

    private final RewardClaimService rewardClaimService;

    @PostMapping("/claim")
    public ResponseEntity<RewardClaimDetailDTO> claimReward(
            @RequestBody RewardClaimRequestDTO request) {

        return ResponseEntity.ok(
                rewardClaimService.claimReward(request)
        );
    }

}
