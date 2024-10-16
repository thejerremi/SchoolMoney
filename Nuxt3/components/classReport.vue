<template>
  <div class="d-flex flex-column justify-center align-center text-center">
    <h2>Wygeneruj raporty finansowe ze wszystkich zbiórek dla wszystkich rodziców</h2>
    <v-btn class="mt-3" variant="outlined" @click="generateAll">Wygeneruj</v-btn>
    <div class="mt-6">
      <h2>Wygeneruj raporty finansowe ze wszystkich zbiórek dla konkretnego rodzica</h2>
      <v-select v-model="selectedParent" :items="classStore.selectedClass.parents" :item-title="getParentFullName" item-value="id"></v-select>
      <v-btn variant="outlined" :disabled="!selectedParent" @click="generateForParent">Wygeneruj</v-btn>
    </div>
  </div>
</template>

<script setup>
const { snackbarSuccess, snackbarError, snackbarInfo, clearSnackbars } = useSnack();
import reportService from '@/services/reportService';
const route = useRoute();
const classStore = useClassStore();
const generateAll = async () => {
  try {
    snackbarInfo('Trwa generowanie raportu...', 150000)
    const response = await reportService.fetchAllTransactionsFromClassFundraisers(route.params.id);

    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const pdfWindow = window.open();
    pdfWindow.location.href = url;
    clearSnackbars();
  } catch (error) {
    clearSnackbars();
    snackbarError('Wystąpił błąd podczas generowania raportu.')
    console.log(error);
  }
}

const getParentFullName = (parent) => {
  if(parent.role === 'ADMIN')
    return "Administrator";
  return `${parent.firstname} ${parent.lastname} - ${parent.email}`;
};
const selectedParent = ref();
const generateForParent = async () => {
  try {
    snackbarInfo('Trwa generowanie raportu...', 150000)
    const response = await reportService.fetchParentTransactionsFromClassFundraisers(route.params.id, selectedParent.value);

    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const pdfWindow = window.open();
    pdfWindow.location.href = url;
    clearSnackbars();
  } catch (error) {
    clearSnackbars();
    snackbarError('Wystąpił błąd podczas generowania raportu.')
    console.log(error);
  }
}
</script>

<style>

</style>