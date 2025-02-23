package com.aidascorner.featureBranch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aidascorner.featureBranch.model.Branch;
import com.aidascorner.featureBranch.service.BranchService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }
    
    /**
     * GET /api/branches - Get all branches
     */
    @GetMapping("/branches")
    public ResponseEntity<List<Branch>> getAllBranches(){
        try {
            List<Branch> branches = branchService.getAllBranches();
            return ResponseEntity.ok(branches);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
