package com.paw.schoolMoney.fundraiser;

import com.paw.schoolMoney.auth.AuthenticationService;
import com.paw.schoolMoney.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fundraisers")
@RequiredArgsConstructor
public class FundraiserController {

    private final FundraiserService fundraiserService;
    private final AuthenticationService authService;

    // Katalog, w którym będą zapisywane pliki
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<?> createFundraiser(
            @RequestParam("goal") BigDecimal goal,
            @RequestParam("shortDescription") String shortDescription,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam("logo") MultipartFile logo,
            @RequestParam("classId") int classId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User author = (User) userDetails;

        Fundraiser fundraiser = Fundraiser.builder()
                .goal(BigDecimal.valueOf(goal.doubleValue()))
                .currentAmount(BigDecimal.ZERO)
                .shortDescription(shortDescription)
                .startDate(startDate)
                .endDate(endDate)
                .logoPath(null)
                .build();

        fundraiserService.createFundraiser(fundraiser, classId, author.getId(), logo);

        return ResponseEntity.ok("Zbiórka została utworzona.");
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateFundraiser(
            @RequestHeader("Authorization") String token,
            @PathVariable int id,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) BigDecimal goal,
            @RequestParam(required = false) String shortDescription,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        return fundraiserService.updateFundraiser(id, logo, goal, shortDescription, startDate, endDate, user.get().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update-owner/{fundraiserId}")
    public ResponseEntity<?> updateFundraiserOwner(
            @RequestHeader("Authorization") String token,
            @PathVariable int fundraiserId,
            @RequestParam int newOwnerId) {

        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return fundraiserService.updateFundraiserOwner(user.get(), fundraiserId, newOwnerId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteFundraiser(
            @PathVariable int id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User author = (User) userDetails;
        fundraiserService.deleteFundraiser(id, author.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<FundraiserResponse>> getFundraisersByClass(@PathVariable int classId) {
        List<FundraiserResponse> fundraisers = fundraiserService.getFundraisersByClass(classId);
        return ResponseEntity.ok(fundraisers);
    }

    @GetMapping("/details/{fundraiserId}")
    public ResponseEntity<FundraiserResponse> getFundraiserDetails(@PathVariable int fundraiserId) {
        FundraiserResponse fundraiser = fundraiserService.getFundraiserDetails(fundraiserId);
        return ResponseEntity.ok(fundraiser);
    }

    @GetMapping("/author")
    public ResponseEntity<List<FundraiserResponse>> getFundraisersByAuthor(@AuthenticationPrincipal UserDetails userDetails) {
        User author = (User) userDetails;
        List<FundraiserResponse> fundraisers = fundraiserService.getFundraisersByAuthor(author.getId());
        return ResponseEntity.ok(fundraisers);
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelFundraiser(@RequestHeader("Authorization") String token, @RequestParam int fundraiserId) {
        Optional<User> user = authService.findUserByToken(token);
        if(user.isEmpty())
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        return fundraiserService.cancelFundraiser(user.get(), fundraiserId);
    }
}
