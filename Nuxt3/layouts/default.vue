<template>
  <v-app>
    <headerComponent />
    <v-main>
      <slot />
      <NuxtSnackbar />
    </v-main>
  </v-app>
</template>

<script setup>
import { useAuthStore } from "~/stores/AuthStore";
import { useTransactionStore } from "~/stores/TransactionStore";
import { useSnack } from "@/composables/useSnack";
import { useDate } from "vuetify";

const { snackbarSuccess, snackbarError } = useSnack();
const authStore = useAuthStore();
const transactionStore = useTransactionStore();
const date = useDate();

onMounted(async () => {
  if (localStorage.getItem("token") !== null) {
    await authStore
      .getUserByToken()
      .then(() => {
        snackbarSuccess("Zalogowano pomyślnie.");
      })
      .catch(() => {
        snackbarError("Twoja sesja wygasła. Zaloguj się ponownie.");
      });
  }
});
watch(
  () => authStore.user,
  (user) => {
    if (user === null) {
      navigateTo("/");
    }
  }
);

watch(
  () => transactionStore.lastFiveTransactions,
  (newValue) => {
    newValue.forEach((transaction) => {
      // Przekształć typ transakcji na odpowiednią nazwę
      transaction.type = transactionTypes[transaction.type];

      // Dodaj minus dla wypłaty, przelewu, wpłaty na zbiórkę i korekcji balansu
      if (
        transaction.type === transactionTypes.WITHDRAW ||
        transaction.type === transactionTypes.TRANSFER ||
        transaction.type === transactionTypes.FUNDRAISE_DEPOSIT ||
        transaction.type === transactionTypes.FUNDRAISE_WITHDRAW_CORRECTION
      ) {
        transaction.amount = `-${transaction.amount} PLN`;
      } else {
        transaction.amount = `${transaction.amount} PLN`;
      }

      // Formatowanie daty
      transaction.createdAt = date.format(
        transaction.createdAt,
        "fullDateTime24h"
      );
    });
  }
);

const transactionTypes = {
  ATM_DEPOSIT: "Wpłatomat",
  DEPOSIT: "Depozyt",
  WITHDRAW: "Wypłata",
  USER_TRANSFER: "Wpływ od innego użytkownika",
  TRANSFER: "Przelew",
  LOAN: "Pożyczka",
  INTEREST: "Odsetki",
  MONTHLY_RATE: "Miesięczna rata",
  LOAN_PAYMENT: "Spłata pożyczki",
  FUNDRAISE_DEPOSIT: "Wpłata na zbiórkę",
  FUNDRAISE_WITHDRAW: "Wypłata ze zbiórki",
  FUNDRAISE_REFUND: "Zwrot z anulowanej zbiórki",
  FUNDRAISE_WITHDRAW_CORRECTION: "Korekcja balansu z anulowanej zbiórki",
};
</script>

<style></style>
