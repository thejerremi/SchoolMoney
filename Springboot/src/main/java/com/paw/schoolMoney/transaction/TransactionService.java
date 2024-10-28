package com.paw.schoolMoney.transaction;

import com.paw.schoolMoney._class._Class;
import com.paw.schoolMoney._class._ClassRepository;
import com.paw.schoolMoney.child.Child;
import com.paw.schoolMoney.child.ChildRepository;
import com.paw.schoolMoney.child.ChildService;
import com.paw.schoolMoney.fundraiser.Fundraiser;
import com.paw.schoolMoney.fundraiser.FundraiserRepository;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney.user.UserRepository;
import com.paw.schoolMoney.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.paw.schoolMoney.transaction.TransactionType.*;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final UserRepository userRepository;
    private final FundraiserRepository fundraiserRepository;
    private final UserService userService;
    private final ChildService childService;
    private final ChildRepository childRepository;
    private final _ClassRepository classRepository;

    public List<TransactionResponse> getLast5Transactions(Integer userId) {
        return repository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<TransactionResponse> getTransactionsWithPagination(Integer userId, int page, int size) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::convertToResponse);
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .recipient(transaction.getRecipient())
                .description(transaction.getDescription())
                .build();
    }

    public void deposit(Transaction request, User user){
        var deposit = Transaction.builder()
                .type(DEPOSIT)
                .amount(request.getAmount())
                .user(user)
                .createdAt(LocalDateTime.now())
                .description("Wpłacono " + request.getAmount() + " złotych.")
                .build();
        userService.addBalance(user, request.getAmount());
        repository.save(deposit);
    }

    public void atmDeposit(Transaction request, User user){
        var deposit = Transaction.builder()
                .type(ATM_DEPOSIT)
                .amount(request.getAmount())
                .user(user)
                .createdAt(LocalDateTime.now())
                .description("Wpłacono " + request.getAmount() + " złotych.")
                .build();
        userService.addBalance(user, request.getAmount());
        repository.save(deposit);
    }

    public ResponseEntity<?> transfer(User user, TransferRequest request) {
        if(user.getBalance().compareTo(request.getAmount()) < 0){
            return ResponseEntity.badRequest().body("Insufficient balance.");
        }
        if(request.getAccountNumberDest().length() != 26){
            return ResponseEntity.badRequest().body("Incorrect acount number.");
        }
        Optional<User> userDestination = userRepository.findByAccountNumber(request.getAccountNumberDest());
        userDestination.ifPresent(value -> userService.addBalance(value, request.getAmount()));
        userService.subtractBalance(user, request.getAmount());

        String recipientDescription = userDestination
                .map(dest -> dest.getFirstname() + " " + dest.getLastname() + ", numer konta: " + dest.getAccountNumber())
                .orElse("numer konta: " + request.getAccountNumberDest());

        var transferSent = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .user(user)
                .createdAt(LocalDateTime.now())
                .recipient(recipientDescription)
                .description("Przelano " + request.getAmount() + " złotych na konto: " + request.getAccountNumberDest())
                .build();

        if(userDestination.isPresent()){
            var transferReceive = Transaction.builder()
                    .type(TransactionType.USER_TRANSFER)
                    .amount(request.getAmount())
                    .user(userDestination.get())
                    .createdAt(LocalDateTime.now())
                    .description("Otrzymano " + request.getAmount() + " złotych od "
                            + user.getFirstname() + " " + user.getLastname() + ", numer konta:" + user.getAccountNumber())
                    .build();
            repository.save(transferReceive);
        }
        repository.save(transferSent);
        return ResponseEntity.ok().build();
    }

    public void depositForFundraiser(TransferForFundraiser request, User user) {
        Optional<Fundraiser> fundraiser = fundraiserRepository.findById(request.getFundraiserId());
        if (fundraiser.isEmpty()) {
            throw new IllegalArgumentException("Fundraiser not found.");
        }

        // Sprawdzenie, czy użytkownik jest członkiem klasy, której dotyczy zbiórka
        if (!isUserInClass(user, fundraiser.get().get_class())) {
            throw new SecurityException("You are not a member of this class.");
        }
        Optional<Child> child = childRepository.findById(request.getChildrenId());

        // Sprawdzenie, czy użytkownik ma wystarczający balans
        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        var deposit = Transaction.builder()
                .type(TransactionType.FUNDRAISE_DEPOSIT)
                .amount(request.getAmount())
                .user(user)
                .fundraiser(fundraiser.get())
                .createdAt(LocalDateTime.now())
                .children(child.orElse(null))
                .build();
        if(child.isPresent()){
            deposit.setDescription("Wpłata " + request.getAmount() + "PLN  na zbiórkę: " + fundraiser.get().getShortDescription() +
                    " w klasie "+ fundraiser.get().get_class().getClassName() + " za dziecko " + child.orElse(null).getFirstname() + " " + child.get().getLastname());
        }else{
            deposit.setDescription("Wpłata " + request.getAmount() + "PLN  na zbiórkę: " + fundraiser.get().getShortDescription() +
                    " w klasie "+ fundraiser.get().get_class().getClassName());

        }
        user.setBalance(user.getBalance().subtract(request.getAmount()));
        // Aktualizujemy zebrane środki w zbiórce
        fundraiser.get().addToCurrentAmount(request.getAmount());
        fundraiser.get().addToAvailableFunds(request.getAmount());

        // Zapisujemy transakcję
        repository.save(deposit);

        // Zapisujemy zaktualizowaną zbiórkę
        fundraiserRepository.save(fundraiser.get());
    }
    private boolean isUserInClass(User user, _Class _class) {
        return _class.getParents().contains(user) || _class.getTreasurer().getId().equals(user.getId());
    }

    public ResponseEntity<?> getFundraiserHistory(int fundraiserId) {
        Optional<Fundraiser> fundraiser = fundraiserRepository.findById(fundraiserId);
        if (fundraiser.isEmpty()) {
            return ResponseEntity.badRequest().body("Fundraiser not found.");
        }

        List<Transaction> history = repository.findAllByFundraiserId(fundraiserId);

        List<TransactionFundraiserResponse> responseList = history.stream()
                .map(transaction -> TransactionFundraiserResponse.builder()
                        .type(transaction.getType())
                        .amount(transaction.getAmount())
                        .createdAt(transaction.getCreatedAt())
                        .description(transaction.getDescription())
                        .child(transaction.getChildren() != null ? childService.getChildFullName(transaction.getChildren()) : null)
                        .childId(transaction.getChildren() != null ? transaction.getChildren().getId() : -1)
                        .fundraiserId(transaction.getFundraiser().getId())
                        .parent(userService.getParentFullNameWithEmail(transaction.getUser()))
                        .parentId(transaction.getUser().getId())
                        .build())
                .toList();

        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<?> withdrawFromFundraiser(BigDecimal amount, int fundraiserId, User user) {
        // Znalezienie zbiórki
        Optional<Fundraiser> fundraiser = fundraiserRepository.findById(fundraiserId);
        if (fundraiser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fundraiser not found.");
        }

        // Sprawdzenie, czy użytkownik jest autorem zbiórki
        if (!fundraiser.get().getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the author of the fundraiser can withdraw funds.");
        }

        // Sprawdzenie, czy kwota wypłaty jest mniejsza lub równa dostępnej kwocie
        if (amount.compareTo(fundraiser.get().getAvailableFunds()) > 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Amount exceeds available funds.");
        }

        // Tworzenie transakcji wypłaty
        var withdrawal = Transaction.builder()
                .type(TransactionType.FUNDRAISE_WITHDRAW)
                .amount(amount)
                .user(user)
                .fundraiser(fundraiser.get())
                .createdAt(LocalDateTime.now())
                .description("Wypłata " + amount + " PLN ze zbiórki " + fundraiser.get().getShortDescription()
                        + " w klasie " + fundraiser.get().get_class().getClassName())
                .build();

        // Aktualizacja dostępnych środków w zbiórce
        fundraiser.get().setAvailableFunds(fundraiser.get().getAvailableFunds().subtract(amount));

        // Dodanie wypłaconej kwoty do balansu użytkownika
        user.setBalance(user.getBalance().add(amount));

        // Zapisanie transakcji wypłaty i aktualizacja zbiórki oraz użytkownika
        repository.save(withdrawal);
        fundraiserRepository.save(fundraiser.get());
        userRepository.save(user);
        return ResponseEntity.ok().body("Withdrawed from fundraiser successfully.");
    }

}