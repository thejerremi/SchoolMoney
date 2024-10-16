package com.paw.schoolMoney.fundraiser;

public enum FundraiserStatus {
    PLANNED, // Otwarta zostanie dopiero gdy będzie dzień rozpoczęcia
    OPEN,
    CLOSED,
    CANCELLED, // Zbiórka została anulowana i wpłacone środki zostały zwrócone,
    BLOCKED
}