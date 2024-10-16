package com.paw.schoolMoney.transaction;

import com.paw.schoolMoney.auth.AuthenticationService;
import com.paw.schoolMoney.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;
    private final AuthenticationService authService;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/last5")
    public List<TransactionResponse> getLast5Transactions(@AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = authService.getUserIdFromEmail(userDetails.getUsername());
        return service.getLast5Transactions(userId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Page<TransactionResponse> getTransactionsWithPagination(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        Integer userId = authService.getUserIdFromEmail(userDetails.getUsername());
        return service.getTransactionsWithPagination(userId, page, size);
    }
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Transaction request, @RequestHeader("Authorization") String token){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");

        service.deposit(request, user.get());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/atm_deposit")
    public ResponseEntity<?> atmDeposit(@RequestBody Transaction request, @RequestHeader("Authorization") String token){
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");

        service.atmDeposit(request, user.get());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String token, @RequestBody TransferRequest request) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.transfer(user.get(), request);
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/fundraiser/deposit")
    public ResponseEntity<?> depositForFundraiser(@RequestBody TransferForFundraiser request,
                                                  @RequestHeader("Authorization") String token) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        service.depositForFundraiser(request, user.get());
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fundraiser/history")
    public ResponseEntity<?> getFundraiserHistory(@RequestHeader("Authorization") String token, @RequestParam int fundraiserId) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return service.getFundraiserHistory(fundraiserId);
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/fundraiser/withdraw")
    public ResponseEntity<?> withdrawFromFundraiser(@RequestParam BigDecimal amount,
                                                    @RequestParam int fundraiserId,
                                                    @RequestHeader("Authorization") String token) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        return service.withdrawFromFundraiser(amount, fundraiserId, user.get());
    }
}
