<template>
  <div>
    <v-card class="ma-12 mt-n6" variant="outlined" elevation="16" height="50vh">
      <v-card-title>Ostatnie transakcje</v-card-title>
      <v-card-text v-if="transactionStore.lastFiveTransactions !== undefined">
        <v-data-table
        class="rounded"
        v-model:sort-by="sortBy"
        :headers="headers"
        :items="userTransactions"
        no-data-text="Wykonaj swoją pierwszą transakcję!"
        hide-default-footer
      >
      </v-data-table>
      <div class="d-flex justify-center mt-6 ga-2">
        <v-btn variant="outlined" @click="navigateTo('/user/transaction-history')">Sprawdź pełną historię</v-btn>
        <v-btn variant="outlined" @click="generateReport()">Wygeneruj raport finansowy</v-btn>
      </div>
      </v-card-text>
      <v-card-text v-else>
        <v-skeleton-loader type="card"></v-skeleton-loader>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import reportService from '@/services/reportService';


const { snackbarSuccess, snackbarError, snackbarInfo, clearSnackbars } = useSnack();
const authStore = useAuthStore();
const transactionStore = useTransactionStore();
const generateReport = async () => {
  try {
    snackbarInfo('Trwa generowanie raportu...', 150000)
    const response = await reportService.fetchAllParentTransactions(authStore.user.id);

    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const pdfWindow = window.open();
    pdfWindow.location.href = url;
    clearSnackbars();
  } catch (error) {
    clearSnackbars();
    snackbarError('Wystąpił błąd podczas generowania raportu.')
    console.log(error);
  }
};

const userTransactions = computed(() => transactionStore.lastFiveTransactions);
onMounted(async () => {
  if(transactionStore.lastFiveTransactions.length === 0){
    await transactionStore.fetchLastTransactions();
  }
});

const headers = [
  { title: "Typ transakcji", value: "type" },
  { title: "Kwota", value: "amount" },
  { title: "Data operacji", value: "createdAt" },
];
const sortBy = [{ key: "createdAt", order: "desc" }];
</script>

<style scoped>
</style>