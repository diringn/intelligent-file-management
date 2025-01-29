package com.example.intelligent_file_management.repository;

import com.example.intelligent_file_management.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

    List<Rule> findByFileExtension(String fileExtension);
}