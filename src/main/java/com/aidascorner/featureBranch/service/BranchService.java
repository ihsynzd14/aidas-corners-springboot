package com.aidascorner.featureBranch.service;

import org.springframework.stereotype.Service;

import com.aidascorner.featureBranch.model.Branch;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BranchService {
        private final Firestore firestore;
        
        public BranchService(Firestore firestore) {
            this.firestore = firestore;
        }

        public List<Branch> getAllBranches() throws ExecutionException, InterruptedException {
            List<Branch> branches = new ArrayList<>();

            ApiFuture<QuerySnapshot> future = firestore.collection("branchs").get();
        
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            for (QueryDocumentSnapshot document : documents) {
                String id = document.getId();
                String name = document.getString("name");

                Branch branch = new Branch(id, name);
                branches.add(branch);
            }
            return branches;
        }
}
