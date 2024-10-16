package com.paw.schoolMoney._class;

import com.paw.schoolMoney.auth.AuthenticationService;
import com.paw.schoolMoney.child.ChildRequest;
import com.paw.schoolMoney.child.ChildService;
import com.paw.schoolMoney.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class _ClassController {
    private final _ClassService service;
    private final ChildService childService;
    private final AuthenticationService authService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/by-parent")
    public ResponseEntity<?> getClassesByParentId(@RequestHeader("Authorization") String token) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.getClassesByParentId(user.get());
    }

    @GetMapping()
    public ResponseEntity<?> getClassById(@RequestParam int id) {
        return service.getClassById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public ResponseEntity<?> addClass(@RequestHeader("Authorization") String token, @RequestBody _ClassRequest request){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.addClass(user.get(), request);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add-parent")
    public ResponseEntity<?> addParentToClass(@RequestHeader("Authorization") String token, @RequestParam int parentId, @RequestParam int classId){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.addParentToClass(user.get(), parentId, classId);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update-name")
    public ResponseEntity<?> updateClassName(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> requestBody){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.updateClassName(user.get(), requestBody.get("id"), requestBody.get("newClassName"));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update-treasurer")
    public ResponseEntity<?> updateClassTreasurer(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> requestBody){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.updateClassTreasurer(user.get(), requestBody.get("classId"), requestBody.get("newTreasurerId"));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete-parent")
    public ResponseEntity<?> deleteParent(@RequestHeader("Authorization") String token, @RequestParam int parentId, @RequestParam int classId) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.deleteParent(user.get(), parentId, classId);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteClass(@RequestHeader("Authorization") String token, @RequestParam int id) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.deleteClass(user.get(), id);
    }
}