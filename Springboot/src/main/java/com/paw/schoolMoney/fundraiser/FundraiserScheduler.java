package com.paw.schoolMoney.fundraiser;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundraiserScheduler {

    private final FundraiserRepository fundraiserRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateFundraiserStatuses() {
        LocalDate today = LocalDate.now();

        // Otwórz zbiórki, które mają datę rozpoczęcia dzisiaj i są zaplanowane
        List<Fundraiser> fundraisersToOpen = fundraiserRepository.findAllByStartDateAndStatus(today, FundraiserStatus.PLANNED);
        for (Fundraiser fundraiser : fundraisersToOpen) {
            fundraiser.setStatus(FundraiserStatus.OPEN);
            fundraiserRepository.save(fundraiser);
        }

        // Zamknij zbiórki, które mają datę zakończenia przed dzisiejszą i są otwarte
        List<Fundraiser> fundraisersToClose = fundraiserRepository.findAllByEndDateBeforeAndStatus(today, FundraiserStatus.OPEN);
        for (Fundraiser fundraiser : fundraisersToClose) {
            fundraiser.setStatus(FundraiserStatus.CLOSED);
            fundraiserRepository.save(fundraiser);
        }
    }

    @PostConstruct
    public void checkFundraisersOnStartup() {
        updateFundraiserStatuses();
    }

}
