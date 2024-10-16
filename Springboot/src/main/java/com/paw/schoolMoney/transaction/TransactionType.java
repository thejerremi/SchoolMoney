package com.paw.schoolMoney.transaction;

public enum TransactionType {
    DEPOSIT, // WPŁATA
    ATM_DEPOSIT, // WPŁATA Z WPŁATOMATU
    USER_TRANSFER, // WPŁYW OD INNEGO UŻYTKOWNIKA
    WITHDRAW, // WYPŁATA
    TRANSFER, // PRZELEW
    FUNDRAISE_DEPOSIT, // WPŁATA NA ZBIÓRKĘ
    FUNDRAISE_WITHDRAW, // WYPŁATA ZE ZBIÓRKI
    FUNDRAISE_REFUND, // ZWROT Z ANULOWANEJ ZBIÓRKI
    FUNDRAISE_WITHDRAW_CORRECTION // KOREKCJA BALANSU Z ANULOWANEJ ZBIÓRKI
}