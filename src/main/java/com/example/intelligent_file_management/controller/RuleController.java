package com.example.intelligent_file_management.controller;

import com.example.intelligent_file_management.model.Rule;
import com.example.intelligent_file_management.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleRepository ruleRepository;

    @GetMapping
    public ResponseEntity<List<Rule>> getAllRules() {
        List<Rule> rules = ruleRepository.findAll();
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    public ResponseEntity<Rule> createRule(@RequestBody Rule rule) {
        Rule savedRule = ruleRepository.save(rule);
        return ResponseEntity.ok(savedRule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> updateRule(@PathVariable Long id, @RequestBody Rule updatedRule) {
        Optional<Rule> existingRule = ruleRepository.findById(id);
        if (existingRule.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Rule rule = existingRule.get();
        rule.setName(updatedRule.getName());
        rule.setDescription(updatedRule.getDescription());
        rule.setFileExtension(updatedRule.getFileExtension());
        rule.setOperationType(updatedRule.getOperationType());
        rule.setTargetDirectory(updatedRule.getTargetDirectory());
        rule.setRenamePattern(updatedRule.getRenamePattern());
        Rule saved = ruleRepository.save(rule);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (!ruleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ruleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> getRuleById(@PathVariable Long id) {
        Optional<Rule> rule = ruleRepository.findById(id);
        return rule.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
