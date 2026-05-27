package com.api.boleteria.service;

import com.api.boleteria.dto.detail.RewardClaimDetailDTO;
import com.api.boleteria.dto.request.RewardClaimRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.model.RewardClaim;
import com.api.boleteria.model.User;
import com.api.boleteria.repository.IRewardClaimRepository;
import com.api.boleteria.repository.IUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardClaimService {

    private final IUserRepository userRepository;
    private final IRewardClaimRepository rewardClaimRepository;
    private final UserService userService;

    @Transactional
    public RewardClaimDetailDTO claimReward(RewardClaimRequestDTO request) {

        User user = userService.findAuthenticatedUser();

        if (user.getPoints() < request.getPointsRequired()) {
            throw new BadRequestException("No tienes suficientes puntos.");
        }

        // Descontar puntos
        user.setPoints(user.getPoints() - request.getPointsRequired());
        userRepository.save(user);

        // Crear código único
        String code = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        // Crear canje
        RewardClaim claim = new RewardClaim();
        claim.setUser(user);
        claim.setRewardName(request.getRewardName());
        claim.setPointsRequired(request.getPointsRequired());
        claim.setCode(code);

        RewardClaim saved = rewardClaimRepository.save(claim);

        // Respuesta al frontend
        return new RewardClaimDetailDTO(
                saved.getId(),
                user.getUsername(),
                saved.getRewardName(),
                saved.getCode()
        );
    }



}