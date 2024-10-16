package com.paw.schoolMoney.fundraiser;

import com.paw.schoolMoney._class._Class;
import com.paw.schoolMoney._class._ClassRepository;
import com.paw.schoolMoney.child.Child;
import com.paw.schoolMoney.transaction.Transaction;
import com.paw.schoolMoney.transaction.TransactionRepository;
import com.paw.schoolMoney.transaction.TransactionType;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney.user.UserRepository;
import com.paw.schoolMoney.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundraiserService {

    private final FundraiserRepository fundraiserRepository;
    private final _ClassRepository classRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;

    private static final String UPLOAD_DIR = "../Nuxt3/public";

    public Fundraiser createFundraiser(Fundraiser fundraiser, int classId, int authorId, MultipartFile logo) {
        var _class = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class was not found."));
        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author of the fundraiser was not found."));

        // Generujemy unikalną nazwę dla pliku logo
        String logoPath = saveLogo(logo, classId, author.getId());

        fundraiser.setLogoPath(logoPath);

        fundraiser.set_class(_class);
        fundraiser.setAuthor(author);
        fundraiser.setAvailableFunds(BigDecimal.ZERO);
        if (fundraiser.getStartDate().equals(LocalDate.now())) {
            fundraiser.setStatus(FundraiserStatus.OPEN);
        } else {
            fundraiser.setStatus(FundraiserStatus.PLANNED);
        }
        return fundraiserRepository.save(fundraiser);
    }

    public ResponseEntity<?> updateFundraiser(int id, MultipartFile logo, BigDecimal goal,
                                              String shortDescription, LocalDate startDate,
                                              LocalDate endDate, int authorId) {
        // Znalezienie istniejącej zbiórki
        Optional<Fundraiser> optFundraiser = fundraiserRepository.findById(id);
        if (optFundraiser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fundraiser not found.");
        }
        Fundraiser existingFundraiser = optFundraiser.get();

        // Sprawdzenie, czy tylko autor może edytować
        if (!existingFundraiser.getAuthor().getId().equals(authorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author of the fundraiser can edit.");
        }

        // Aktualizacja szczegółów zbiórki
        if(goal != null)
            existingFundraiser.setGoal(goal);
        if(shortDescription != null)
            existingFundraiser.setShortDescription(shortDescription);

        // Sprawdzenie statusu i umożliwienie edycji tylko odpowiednich pól
        if (existingFundraiser.getStatus() == FundraiserStatus.PLANNED) {
            if(startDate != null)
                existingFundraiser.setStartDate(startDate);
            if(endDate != null)
                existingFundraiser.setEndDate(endDate);
        } else if (existingFundraiser.getStatus() == FundraiserStatus.OPEN) {
            if(endDate != null)
                existingFundraiser.setEndDate(endDate);
        }

        // Usuwanie starego logo, jeśli użytkownik przesłał nowe
        if (logo != null && !logo.isEmpty()) {
            // Sprawdzenie, czy istnieje stare logo
            if (existingFundraiser.getLogoPath() != null) {
                // Usuwanie starego logo
                deleteOldLogo(existingFundraiser.getLogoPath());
            }

            // Zapis nowego logo
            String newLogoPath = saveLogo(logo, existingFundraiser.get_class().getId(), authorId);
            existingFundraiser.setLogoPath(newLogoPath);
        }

        fundraiserRepository.save(existingFundraiser);
        return ResponseEntity.ok("Fundraiser updated.");
    }

    public void deleteFundraiser(int id, int authorId) {
        var fundraiser = fundraiserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Zbiórka nie została znaleziona."));

        // Sprawdzenie, czy autor usuwanej zbiórki to użytkownik, który próbuje usunąć
        if (!fundraiser.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("Tylko autor może usunąć tę zbiórkę.");
        }

        fundraiserRepository.delete(fundraiser);
    }

    // Pobranie listy zbiórek dla danej klasy, z konwersją autora na UserResponse
    public List<FundraiserResponse> getFundraisersByClass(int classId) {
        List<Fundraiser> fundraisers = fundraiserRepository.findBy_class_Id(classId);
        return fundraisers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Pobranie szczegółów zbiórki, z konwersją autora na UserResponse
    public FundraiserResponse getFundraiserDetails(int fundraiserId) {
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId)
                .orElseThrow(() -> new IllegalArgumentException("Zbiórka nie została znaleziona."));
        return convertToResponse(fundraiser);
    }

    // Pobranie listy zbiórek danego autora
    public List<FundraiserResponse> getFundraisersByAuthor(int authorId) {
        List<Fundraiser> fundraisers = fundraiserRepository.findByAuthor_Id(authorId);
        return fundraisers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Metoda pomocnicza do konwersji Fundraiser na FundraiserResponse
    public FundraiserResponse convertToResponse(Fundraiser fundraiser) {
        return FundraiserResponse.builder()
                .id(fundraiser.getId())
                .status(fundraiser.getStatus())
                .goal(fundraiser.getGoal())
                .currentAmount(fundraiser.getCurrentAmount())
                .availableFunds(fundraiser.getAvailableFunds())
                .logoPath(fundraiser.getLogoPath())
                .shortDescription(fundraiser.getShortDescription())
                .startDate(fundraiser.getStartDate())
                .endDate(fundraiser.getEndDate())
                .classId(fundraiser.get_class().getId())
                .author(userService.convertToResponse(fundraiser.getAuthor()))
                .build();
    }

    public ResponseEntity<?> updateFundraiserOwner(User user, int fundraiserId, int newOwnerId) {
        try{
            Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId)
                    .orElseThrow(() -> new EntityNotFoundException("Fundraiser not found with id " + fundraiserId));
            if(!Objects.equals(fundraiser.getAuthor().getId(), user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to edit this fundraiser.");
            }
            Optional<User> newOwner = userRepository.findById(newOwnerId);
            if(newOwner.isPresent()) {
                fundraiser.setAuthor(newOwner.get());
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with id " + newOwnerId + " not found.");
            }
            fundraiserRepository.save(fundraiser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change fundraiser owner.");
        }
    }

    public ResponseEntity<?> cancelFundraiser(User user, int fundraiserId) {
        // Znalezienie zbiórki
        Optional<Fundraiser> fundraiserOpt = fundraiserRepository.findById(fundraiserId);
        if (fundraiserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fundraiser with id " + fundraiserId + " not found.");
        }

        Fundraiser fundraiser = fundraiserOpt.get();
        if(!fundraiser.getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this fundraiser.");
        }

        if(fundraiser.getStatus() == FundraiserStatus.CLOSED){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Fundraiser is already closed and cannot be cancelled.");
        }

        if(fundraiser.getStatus() == FundraiserStatus.CANCELLED){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Fundraiser is already cancelled.");
        }

        if(fundraiser.getStatus() == FundraiserStatus.PLANNED){
            fundraiser.setStatus(FundraiserStatus.CANCELLED);
            fundraiserRepository.save(fundraiser);
            return ResponseEntity.ok("Fundraiser has been cancelled.");
        }

        // Znalezienie wszystkich transakcji powiązanych ze zbiórką
        List<Transaction> transactions = transactionRepository.findAllByFundraiserId(fundraiserId);

        // Mapa trzymająca rodziców i sumy do zwrotu
        Map<Integer, BigDecimal> parentRefunds = new HashMap<>();

        // Mapa właścicieli zbiórek i sum wypłat do odjęcia
        Map<Integer, BigDecimal> ownerWithdrawals = new HashMap<>();

        // Przetwarzanie każdej transakcji
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.FUNDRAISE_DEPOSIT) {
                // Jeśli to jest wpłata, dodaj kwotę do sumy, którą należy zwrócić rodzicowi
                int parentId = transaction.getUser().getId();
                parentRefunds.put(parentId, parentRefunds.getOrDefault(parentId, BigDecimal.ZERO).add(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.FUNDRAISE_WITHDRAW) {
                // Jeśli to wypłata, odejmij kwotę od właściciela zbiórki
                int ownerId = transaction.getUser().getId();
                ownerWithdrawals.put(ownerId, ownerWithdrawals.getOrDefault(ownerId, BigDecimal.ZERO).add(transaction.getAmount()));
            }
        }

        // Zwracanie pieniędzy rodzicom
        for (Map.Entry<Integer, BigDecimal> entry : parentRefunds.entrySet()) {
            Integer parentId = entry.getKey();
            BigDecimal refundAmount = entry.getValue();
            User parent = userRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("Rodzic nie został znaleziony."));
            userService.addBalance(parent, refundAmount);  // Zwrócenie kwoty na konto rodzica

            // Tworzenie transakcji zwrotu
            Transaction refundTransaction = Transaction.builder()
                    .type(TransactionType.FUNDRAISE_REFUND)
                    .amount(refundAmount)
                    .user(parent)
                    .fundraiser(fundraiser)
                    .createdAt(LocalDateTime.now())
                    .description("Zwrot za anulowaną zbiórkę: " + fundraiser.getShortDescription() + " w klasie " + fundraiser.get_class().getClassName())
                    .build();

            transactionRepository.save(refundTransaction);
        }

        // Odejmowanie środków od właścicieli zbiórek za wypłaty
        for (Map.Entry<Integer, BigDecimal> entry : ownerWithdrawals.entrySet()) {
            Integer ownerId = entry.getKey();
            BigDecimal withdrawalAmount = entry.getValue();
            User owner = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("Właściciel zbiórki nie został znaleziony."));
            userService.subtractBalance(owner, withdrawalAmount);  // Odjęcie kwoty od właściciela

            // Tworzenie transakcji odejmującej środki
            Transaction withdrawCorrectionTransaction = Transaction.builder()
                    .type(TransactionType.FUNDRAISE_WITHDRAW_CORRECTION)
                    .amount(withdrawalAmount)
                    .user(owner)
                    .fundraiser(fundraiser)
                    .createdAt(LocalDateTime.now())
                    .description("Odjęcie środków za wypłaty po anulowanej zbiórce " + fundraiser.getShortDescription() + " w klasie " + fundraiser.get_class().getClassName())
                    .build();

            transactionRepository.save(withdrawCorrectionTransaction);
        }

        // Zaktualizowanie statusu zbiórki na anulowaną
        fundraiser.setStatus(FundraiserStatus.CANCELLED);
        fundraiser.setAvailableFunds(BigDecimal.ZERO);
        fundraiserRepository.save(fundraiser);
        return ResponseEntity.ok("Fundraiser has been cancelled.");
    }

    public ResponseEntity<?> blockFundraiser(int fundraiserId) {
        // Znalezienie zbiórki
        Optional<Fundraiser> fundraiserOpt = fundraiserRepository.findById(fundraiserId);
        if (fundraiserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fundraiser with id " + fundraiserId + " not found.");
        }

        Fundraiser fundraiser = fundraiserOpt.get();

        if(fundraiser.getStatus() == FundraiserStatus.BLOCKED){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Fundraiser is already cancelled.");
        }

        if(fundraiser.getStatus() == FundraiserStatus.PLANNED){
            fundraiser.setStatus(FundraiserStatus.BLOCKED);
            fundraiserRepository.save(fundraiser);
            return ResponseEntity.ok("Fundraiser has been blocked.");
        }

        // Znalezienie wszystkich transakcji powiązanych ze zbiórką
        List<Transaction> transactions = transactionRepository.findAllByFundraiserId(fundraiserId);

        // Mapa trzymająca rodziców i sumy do zwrotu
        Map<Integer, BigDecimal> parentRefunds = new HashMap<>();

        // Mapa właścicieli zbiórek i sum wypłat do odjęcia
        Map<Integer, BigDecimal> ownerWithdrawals = new HashMap<>();

        // Przetwarzanie każdej transakcji
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.FUNDRAISE_DEPOSIT) {
                // Jeśli to jest wpłata, dodaj kwotę do sumy, którą należy zwrócić rodzicowi
                int parentId = transaction.getUser().getId();
                parentRefunds.put(parentId, parentRefunds.getOrDefault(parentId, BigDecimal.ZERO).add(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.FUNDRAISE_WITHDRAW) {
                // Jeśli to wypłata, odejmij kwotę od właściciela zbiórki
                int ownerId = transaction.getUser().getId();
                ownerWithdrawals.put(ownerId, ownerWithdrawals.getOrDefault(ownerId, BigDecimal.ZERO).add(transaction.getAmount()));
            }
        }

        // Zwracanie pieniędzy rodzicom
        for (Map.Entry<Integer, BigDecimal> entry : parentRefunds.entrySet()) {
            Integer parentId = entry.getKey();
            BigDecimal refundAmount = entry.getValue();
            User parent = userRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("Rodzic nie został znaleziony."));
            userService.addBalance(parent, refundAmount);  // Zwrócenie kwoty na konto rodzica

            // Tworzenie transakcji zwrotu
            Transaction refundTransaction = Transaction.builder()
                    .type(TransactionType.FUNDRAISE_REFUND)
                    .amount(refundAmount)
                    .user(parent)
                    .fundraiser(fundraiser)
                    .createdAt(LocalDateTime.now())
                    .description("Zwrot za zablokowaną zbiórkę: " + fundraiser.getShortDescription() + " w klasie " + fundraiser.get_class().getClassName())
                    .build();

            transactionRepository.save(refundTransaction);
        }

        // Odejmowanie środków od właścicieli zbiórek za wypłaty
        for (Map.Entry<Integer, BigDecimal> entry : ownerWithdrawals.entrySet()) {
            Integer ownerId = entry.getKey();
            BigDecimal withdrawalAmount = entry.getValue();
            User owner = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("Właściciel zbiórki nie został znaleziony."));
            userService.subtractBalance(owner, withdrawalAmount);  // Odjęcie kwoty od właściciela

            // Tworzenie transakcji odejmującej środki
            Transaction withdrawCorrectionTransaction = Transaction.builder()
                    .type(TransactionType.FUNDRAISE_WITHDRAW_CORRECTION)
                    .amount(withdrawalAmount)
                    .user(owner)
                    .fundraiser(fundraiser)
                    .createdAt(LocalDateTime.now())
                    .description("Odjęcie środków za wypłaty po zablokowanej zbiórce " + fundraiser.getShortDescription() + " w klasie " + fundraiser.get_class().getClassName())
                    .build();

            transactionRepository.save(withdrawCorrectionTransaction);
        }

        // Zaktualizowanie statusu zbiórki na anulowaną
        fundraiser.setStatus(FundraiserStatus.BLOCKED);
        fundraiser.setAvailableFunds(BigDecimal.ZERO);
        fundraiserRepository.save(fundraiser);
        return ResponseEntity.ok("Fundraiser has been blocked.");
    }

    // Metoda do zapisywania pliku logo na serwerze z unikalną nazwą
    private String saveLogo(MultipartFile logo, int classId, int authorId) {
        if (logo.isEmpty()) {
            throw new IllegalArgumentException("Logo nie może być puste.");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // Tworzy katalog, jeśli nie istnieje
            }

            // Uzyskanie rozszerzenia pliku
            String originalFileName = logo.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Generowanie unikalnej nazwy pliku: klasa_author_timestamp.ext
            String uniqueFileName = "class_" + classId + "_author_" + authorId + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(uniqueFileName);

            // Zapis pliku na serwerze
            Files.copy(logo.getInputStream(), filePath);

            return uniqueFileName; // Zwracamy ścieżkę do zapisanego pliku

        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas zapisywania pliku.", e);
        }
    }
    // Usuwanie starego logo
    private void deleteOldLogo(String logoPath) {
        try {
            Path oldLogoPath = Paths.get(UPLOAD_DIR).resolve(logoPath);
            Files.deleteIfExists(oldLogoPath);  // Usunięcie starego pliku logo
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas usuwania starego logo.", e);
        }
    }

}
