package com.paw.schoolMoney.child;

import com.paw.schoolMoney.auth.AuthenticationService;
import com.paw.schoolMoney.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/child")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService service;
    private final AuthenticationService authService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping()
    public List<ChildResponse> getChildren(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = authService.getUserIdFromEmail(userDetails.getUsername());
        return service.getChildren(userId);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public ResponseEntity<?> addChild(@RequestHeader("Authorization") String token, @RequestBody ChildRequest request){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.addChild(user.get(), request);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/edit")
    public ResponseEntity<?> editChild(@RequestHeader("Authorization") String token, @RequestBody ChildRequest request){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.editChild(user.get(), request);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteChild(@RequestHeader("Authorization") String token, @RequestParam int id){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.deleteChild(user.get(), id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/by-class/{classId}")
    public ResponseEntity<?> getChildrenByClassId(@RequestHeader("Authorization") String token ,
                                                                    @PathVariable Integer classId) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.getChildrenByClassId(user.get(), classId);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/assign-class")
    public ResponseEntity<?> assignClassToChild(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer childId,
            @RequestParam Integer classId) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.assignClassToChild(user.get(), childId, classId);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/remove-class")
    public ResponseEntity<?> removeClassFromChild(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer childId) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.removeClassFromChild(user.get(), childId);
    }
}
