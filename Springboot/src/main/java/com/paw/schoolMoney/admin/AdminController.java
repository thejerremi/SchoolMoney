package com.paw.schoolMoney.admin;

import com.paw.schoolMoney._class._ClassResponse;
import com.paw.schoolMoney.fundraiser.FundraiserResponse;
import com.paw.schoolMoney.user.UserAdminDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService service;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('admin:read')")
    public List<UserAdminDTO> findAllUsers(){ return service.findAllUsers(); }

    @PostMapping("/lock-user/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> lockUser(@PathVariable("id") Integer id){
        return service.lockUser(id);
    }

    @PostMapping("/unlock-user/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> unlockUser(@PathVariable("id") Integer id){
        return service.unlockUser(id);
    }

    @GetMapping("/classes")
    @PreAuthorize("hasAuthority('admin:read')")
    public List<_ClassResponse> findAllClasses(){ return service.findAllClasses(); }

    @GetMapping("/fundraisers")
    @PreAuthorize("hasAuthority('admin:read')")
    public List<FundraiserResponse> findAllFundraisers(){ return service.findAllFundraisers(); }

    @PostMapping("/lock-fundraiser/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> lockFundraiser(@PathVariable("id") Integer id){
        return service.lockFundraiser(id);
    }
}
