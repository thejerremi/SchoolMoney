package com.paw.schoolMoney.admin;

import com.paw.schoolMoney._class._ClassRepository;
import com.paw.schoolMoney._class._ClassResponse;
import com.paw.schoolMoney._class._ClassService;
import com.paw.schoolMoney.fundraiser.Fundraiser;
import com.paw.schoolMoney.fundraiser.FundraiserRepository;
import com.paw.schoolMoney.fundraiser.FundraiserResponse;
import com.paw.schoolMoney.fundraiser.FundraiserService;
import com.paw.schoolMoney.transaction.Transaction;
import com.paw.schoolMoney.transaction.TransactionRepository;
import com.paw.schoolMoney.transaction.TransactionType;
import com.paw.schoolMoney.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final FundraiserService fundraiserService;
    private final _ClassService classService;
    private final _ClassRepository classRepository;
    private final FundraiserRepository fundraiserRepository;

    public UserAdminDTO convertToUserDTO(User user) {
        List<_ClassResponse> treasurerOfClasses = classService.findAllClassesByTreasurer(user.getId());
        return new UserAdminDTO(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPesel(),
                user.getDob(),
                user.getBalance(),
                user.getAccountNumber(),
                user.getRole(),
                user.getUsername(),
                user.isAccountNonLocked(),
                treasurerOfClasses
        );
    }

    public List<UserAdminDTO> findAllUsers() {
        return userRepository.findAll().stream().map(this::convertToUserDTO).collect(Collectors.toList());
    }

    public ResponseEntity<?> lockUser(Integer id) {
        return userService.lockUserAccount(id);
    }
    public ResponseEntity<?> unlockUser(Integer id) {
        return userService.unlockUserAccount(id);
    }

    List<_ClassResponse> findAllClasses() {
        return classRepository.findAll().stream().map(classService::convertToResponse).collect(Collectors.toList());
    }

    List<FundraiserResponse> findAllFundraisers(){
        return fundraiserRepository.findAll().stream().map(fundraiserService::convertToResponse).collect(Collectors.toList());
    }

    public ResponseEntity<?> lockFundraiser(Integer id) {
        return fundraiserService.blockFundraiser(id);
    }
}
