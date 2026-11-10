package com.smarturban.backend.repository;

import com.smarturban.backend.entity.ComplaintCompletionPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintCompletionPhotoRepository extends JpaRepository<ComplaintCompletionPhoto, Long> {
    List<ComplaintCompletionPhoto> findByComplaintIdOrderByUploadedAtAsc(Long complaintId);
    void deleteByComplaintId(Long complaintId);
}
