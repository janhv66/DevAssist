package com.devassist.repository;

import com.devassist.model.Finding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FindingRepository extends JpaRepository<Finding, Long> {

    List<Finding> findByReviewId(Long reviewId);
}