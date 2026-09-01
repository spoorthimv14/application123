package com.smarturban.repository;

import com.smarturban.entity.Complaint;
import com.smarturban.entity.ComplaintStatus;
import com.smarturban.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserOrderByCreatedAtDesc(User user);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    long countByUser(User user);

    long countByUserAndStatus(User user, ComplaintStatus status);
}
