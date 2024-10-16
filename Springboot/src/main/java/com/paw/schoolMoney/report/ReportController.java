package com.paw.schoolMoney.report;

import com.paw.schoolMoney._class._Class;
import com.paw.schoolMoney._class._ClassRepository;
import com.paw.schoolMoney.auth.AuthenticationService;
import com.paw.schoolMoney.fundraiser.Fundraiser;
import com.paw.schoolMoney.fundraiser.FundraiserRepository;
import com.paw.schoolMoney.transaction.Transaction;
import com.paw.schoolMoney.transaction.TransactionRepository;
import com.paw.schoolMoney.user.Role;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final TransactionRepository transactionRepository;
    private final FundraiserRepository fundraiserRepository;
    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final _ClassRepository classRepository;

    /**
     * Check if the user is in the class associated with the fundraiser
     */
    private boolean userHasAccessToFundraiser(int classId, User user) {
        if(user.getRole() == Role.ADMIN)
            return true;
        return user.getClasses().stream().anyMatch(_class -> _class.getId() == classId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fundraiser/{fundraiserId}")
    public ResponseEntity<?> generateFundraiserReport(@RequestHeader("Authorization") String token,
                                                      @PathVariable int fundraiserId) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        // Check if the fundraiser exists and if the user is in the class associated with the fundraiser
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId).orElse(null);
        if (fundraiser == null) {
            return ResponseEntity.badRequest().body("Fundraiser not found.");
        }

        if (!userHasAccessToFundraiser(fundraiser.get_class().getId(), user.get())) {
            return ResponseEntity.status(403).body("You do not have access to this fundraiser.");
        }

        // Generate the report
        List<Transaction> transactions = transactionRepository.findByFundraiserId(fundraiserId);
        String reportTitle = "Raport finansowy dla zbiórki: " + fundraiser.getShortDescription();
        byte[] pdfContent = reportService.createPdfReport(transactions, reportTitle);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fundraiser_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fundraiser/{fundraiserId}/parent/{parentId}")
    public ResponseEntity<?> generateFundraiserReportForParent(@RequestHeader("Authorization") String token,
                                                               @PathVariable int fundraiserId, @PathVariable int parentId) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        // Check if the user has access to the fundraiser
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId).orElse(null);
        if (fundraiser == null) {
            return ResponseEntity.badRequest().body("Fundraiser not found.");
        }

        if (!userHasAccessToFundraiser(fundraiser.get_class().getId(), user.get())) {
            return ResponseEntity.status(403).body("You do not have access to this fundraiser.");
        }

        // Generate report for specific parent in the fundraiser
        List<Transaction> transactions = transactionRepository.findByFundraiserIdAndParentId(fundraiserId, parentId);
        Optional<User> parent = userRepository.findById(parentId);

        // Tworzenie tytułu raportu
        String reportTitle = "Raport finansowy dla zbiórki: " + fundraiser.getShortDescription();

        if (parent.isPresent()) {
            reportTitle += " dla rodzica: " + parent.get().getFirstname() + " " + parent.get().getLastname();
        } else {
            reportTitle += " dla rodzica o ID: " + parentId;
        }

        byte[] pdfContent = reportService.createPdfReport(transactions, reportTitle);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fundraiser_parent_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/class/{classId}/fundraisers/parent/{parentId}")
    public ResponseEntity<?> generateClassFundraisersReportForParent(@RequestHeader("Authorization") String token,
                                                                     @PathVariable int classId, @PathVariable int parentId) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        // Check if the user has access to the class
        if (!userHasAccessToFundraiser(classId, user.get())) {
            return ResponseEntity.status(403).body("You do not have access to this class.");
        }

        // Generate report for all fundraisers in the class for the given parent
        List<Transaction> transactions = transactionRepository.findByClassIdAndParentId(classId, parentId);
        Optional<_Class> _class = classRepository.findById(classId);
        Optional<User> parent = userRepository.findById(parentId);

        // Tworzenie tytułu raportu
        String reportTitle = "Raport finansowy ze wszystkich zbiórek ";

        if (_class.isPresent()) {
            reportTitle += "w klasie: " + _class.get().getClassName();
        } else {
            reportTitle += "dla klasy o ID: " + classId;
        }

        if (parent.isPresent()) {
            reportTitle += " dla rodzica: " + parent.get().getFirstname() + " " + parent.get().getLastname();
        } else {
            reportTitle += " dla rodzica o ID: " + parentId;
        }
        byte[] pdfContent = reportService.createPdfReport(transactions, reportTitle);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_parent_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/class/{classId}/fundraisers")
    public ResponseEntity<?> generateClassFundraisersReportForAllParents(@RequestHeader("Authorization") String token,
                                                                         @PathVariable int classId) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        // Check if the user has access to the class
        if (!userHasAccessToFundraiser(classId, user.get())) {
            return ResponseEntity.status(403).body("You do not have access to this class.");
        }

        // Generate report for all fundraisers in the class for all parents
        List<Transaction> transactions = transactionRepository.findByClassId(classId);
        Optional<_Class> _class = classRepository.findById(classId);
        String reportTitle;
        reportTitle = _class.map(value -> "Raport finansowy ze zbiórek w klasie: " + value.getClassName()).orElseGet(() -> "Raport finansowy ze zbiórek w klasie o ID: " + classId);

        byte[] pdfContent = reportService.createPdfReport(transactions, reportTitle);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<?> generateParentReport(@RequestHeader("Authorization") String token,
                                                  @PathVariable int parentId) {
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User wasn't found or access token is invalid.");
        }

        // Ensure the requesting user is the owner of the account
        if (!user.get().getId().equals(parentId) && user.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("You do not have access to this account.");
        }
        // Generate report for the given parent
        List<Transaction> transactions = transactionRepository.findByParentId(parentId);
        Optional<User> parent = userRepository.findById(parentId);
        String reportTitle;
        reportTitle = parent.map(value -> "Raport finansowy z konta rodzica: " + value.getFirstname() + " " + value.getLastname()).orElseGet(() -> "Raport finansowy z konta rodzica o ID: " + parentId);

        byte[] pdfContent = reportService.createPdfReport(transactions, reportTitle);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=parent_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}
