package com.paw.schoolMoney.user;

import com.paw.schoolMoney.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final AuthenticationService authenticationService;

    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);
    }
    public void updateDetails(UserDTO request, Principal connectedUser){
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if(request.getFirstname() != null)
            user.setFirstname(request.getFirstname());
        if(request.getLastname() != null)
            user.setLastname(request.getLastname());
        if(request.getDob() != null)
            user.setDob(request.getDob());
        repository.save(user);
    }
    public void addBalance(User user, BigDecimal amount){
        user.setBalance(user.getBalance().add(amount));
        repository.save(user);
    }
    public void subtractBalance(User user, BigDecimal amount){
        user.setBalance(user.getBalance().subtract(amount));
        repository.save(user);
    }
    public UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .accountNumber(user.getAccountNumber())
                .username(user.getUsername())
                .build();
    }
    public List<UserResponse> convertToResponse(List<User> users) {
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .accountNumber(user.getAccountNumber())
                        .username(user.getUsername())
                        .build())
                .collect(Collectors.toList());
    }
    public List<UserResponse> searchParents(String search) {
        List<User> parents = repository.findParentsBySearchTerm(search);
        return parents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    public String getParentFullName(User parent) {
        if (parent == null) {
            return "Rodzic nieznany";
        }
        return parent.getFirstname() + " " + parent.getLastname();
    }
    public String getParentFullNameWithEmail(User parent) {
        if (parent == null) {
            return "Rodzic nieznany";
        }
        return parent.getFirstname() + " " + parent.getLastname() + " - " + parent.getEmail();
    }

    public List<UserResponse> searchParentsByClass(String search, int classId) {
        List<User> parents = repository.findParentsBySearchTermAndClass(search, classId);
        return parents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    public ResponseEntity<?> lockUserAccount(Integer userId) {
        Optional<User> user = repository.findById(userId);
        if(user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        user.get().setAccountNonLocked(false);
        authenticationService.revokeAllUserTokens(user.get());
        repository.save(user.get());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> unlockUserAccount(Integer userId) {
        Optional<User> user = repository.findById(userId);
        if(user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        user.get().setAccountNonLocked(true);
        repository.save(user.get());
        return ResponseEntity.ok().build();
    }
}
