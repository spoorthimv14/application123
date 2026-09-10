package com.smarturban.repository;

import com.smarturban.entity.Complaint;
import com.smarturban.entity.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {

    List<ComplaintStatusHistory> findByComplaintOrderByCreatedAtAsc(Complaint complaint);
}
