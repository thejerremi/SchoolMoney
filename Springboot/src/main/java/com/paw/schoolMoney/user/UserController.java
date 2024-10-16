package com.paw.schoolMoney.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
          @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update-details")
    public ResponseEntity<?> updateDetails(
            @RequestBody UserDTO request,
            Principal connectedUser
    ) {
        service.updateDetails(request, connectedUser);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/search-parents")
    public ResponseEntity<List<UserResponse>> searchParents(
            @RequestParam String search
    ) {
        List<UserResponse> parents = service.searchParents(search);
        return ResponseEntity.ok(parents);
    }
    @GetMapping("/search-parents/by-class")
    public ResponseEntity<List<UserResponse>> searchParentsByClass(
            @RequestParam String search,
            @RequestParam int classId
    ) {
        List<UserResponse> parents = service.searchParentsByClass(search, classId);
        return ResponseEntity.ok(parents);
    }
}
