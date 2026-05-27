package com.api.boleteria.repository;

import com.api.boleteria.model.RewardClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRewardClaimRepository
        extends JpaRepository<RewardClaim, Long> {

    List<RewardClaim> findByUserId(Long userId);

}