<template>
  <div v-if="history">
    <v-data-table v-model:sort-by="sortBy" :items="history" :headers="headers" no-data-text="Brak wpłat/wypłat!"></v-data-table>
  </div>
</template>

<script setup>
const route = useRoute();
const fundraiserStore = useFundraiserStore();
const history = ref([]);
const transactionTypes = {
  FUNDRAISE_DEPOSIT: "Wpłata na zbiórkę",
  FUNDRAISE_WITHDRAW: "Wypłata ze zbiórki",
  FUNDRAISE_REFUND: "Zwrot z anulowanej zbiórki",
  FUNDRAISE_WITHDRAW_CORRECTION: "Korekcja balansu z anulowanej zbiórki"
};

onMounted(async () => {
  history.value = await fundraiserStore.fetchFundraiserHistory(route.params.id);
  history.value = history.value.map(item => ({
    ...item,
    amount: (item.type === "FUNDRAISE_WITHDRAW" || item.type === "FUNDRAISE_REFUND")
      ? `-${item.amount} PLN`
      : `${item.amount} PLN`,
    createdAt: formatDate(item.createdAt),
    action: transactionTypes[item.type] || 'Nieznana akcja',
    parent: item.parent,
    child: item.child || 'Nie dotyczy'
  }));
});

const sortBy = [{ key: 'createdAt', order: 'desc' }];
const headers = [
  { title: 'Akcja', key: 'action', value: 'action' },
  { title: 'Data', key: 'createdAt', value: 'createdAt', align: 'start', sortable: true },
  { title: 'Kwota', value: 'amount' },
  { title: 'Rodzic', value: 'parent' },
  { title: 'Dziecko', value: 'child' }
];



const formatDate = (date) => {
  const newDate = new Date(date);
  return newDate.toLocaleDateString('pl-PL', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}
</script>

<style>

</style>