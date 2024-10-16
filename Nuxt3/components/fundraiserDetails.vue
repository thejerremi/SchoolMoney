<template>
    <v-row class="w-100">
    <v-col cols="9">
      <h3 class="text-center">{{ selectedFundraiser.shortDescription }} - {{ fundraiserStatusString }}</h3>
      <v-list>
        <v-list-item title="Klasa" 
        :subtitle="classStore.selectedClass.className">
      </v-list-item>
        <v-list-item title="Czas trwania zbiórki" 
        :subtitle="selectedFundraiser.startDate + ' - ' + selectedFundraiser.endDate">
      </v-list-item>
      <v-list-item title="Autor zbiórki" 
        :subtitle="selectedFundraiser.author.firstname + ' ' + selectedFundraiser.author.lastname + ' - ' + selectedFundraiser.author.email">
      </v-list-item>
      <v-list-item title="Dostępne środki" 
        :subtitle="`${selectedFundraiser.availableFunds} PLN - (wypłacono ${selectedFundraiser.currentAmount - selectedFundraiser.availableFunds} PLN)`">
      </v-list-item>
        <v-list-item>
        <v-card class="text-center" variant="outlined">
            <v-card-title class="text-overline">
              Zebrane fundusze
              <div class="text-green-darken-3 text-h3 font-weight-bold">{{ ((selectedFundraiser.currentAmount / selectedFundraiser.goal) * 100).toFixed(2) }}%
              </div>
              <div class="text-h6 text-medium-emphasis font-weight-regular">
                {{ (selectedFundraiser.goal - selectedFundraiser.currentAmount) > 0 ? (selectedFundraiser.goal - selectedFundraiser.currentAmount) : 0 }} PLN do osiągnięcia celu
              </div>
            </v-card-title>
            <v-card-text>
              <v-progress-linear color="green-darken-3" height="22"
                :model-value="(selectedFundraiser.currentAmount / selectedFundraiser.goal) * 100" rounded="lg">
              </v-progress-linear>

              <div class="d-flex justify-space-between py-3">
                <span class="text-green-darken-3 font-weight-medium">
                  Wpłacono: {{ selectedFundraiser.currentAmount }} PLN
                </span>

                <span class="text-medium-emphasis"> Cel: {{ selectedFundraiser.goal }} PLN</span>
              </div>
            </v-card-text>
          </v-card>
        </v-list-item>
      </v-list>
    </v-col>
    <v-col cols="3">
      <v-img max-height="350" max-width="350" aspect-ratio="16/9" cover :src="`/${selectedFundraiser.logoPath}`"></v-img>
    </v-col>
  </v-row>
  <v-card variant="outlined" class="mx-4 mt-n5" style="position: relative">
  <v-card-title class="text-center">Wpłać za dziecko</v-card-title>
  
  <v-overlay
    v-if="fundraiser.status !== 'OPEN'"
    v-model="depositDisabled"
    contained
    :persistent="true"
    opacity="0.75"
    class="d-flex align-center justify-center"
  >
    <v-card-text class="text-h6 text-center text-red text-wrap">
      Nie możesz wpłacić na tę zbiórkę, ponieważ {{ depositDisabledReason }}.
    </v-card-text>
  </v-overlay>

  <v-card-text>
    Twój balans: {{ authStore.user.balance.toFixed(2) }} PLN
    <v-form v-model="isDepositFormValid">
      <v-select
      :disabled="fundraiser.status !== 'OPEN'"
        class="mt-3"
        label="Wybierz dziecko za które chcesz zapłacić"
        :items="childrenList"
        v-model="depositData.childrenId"
        :item-title="getChildFullname"
        item-value="id"
        no-data-text="Nie posiadasz dzieci przypisanych do tej klasy"
        :rules="[rules.required]"
      ></v-select>
      
      <v-text-field
      :disabled="fundraiser.status !== 'OPEN'"
        label="Kwota"
        type="number"
        v-model="depositData.amount"
        :rules="[rules.required, rules.minValue(0.01), rules.maxValue(authStore.user.balance)]"
      />
      <div class="d-flex justify-center">
        <v-btn :disabled="!isDepositFormValid || fundraiser.status !== 'OPEN'" color="green" @click="deposit()">Wpłać</v-btn>
      </div>
    </v-form>
  </v-card-text>
</v-card>

</template>

<script setup>
const { snackbarSuccess, snackbarError } = useSnack();
const route = useRoute();
const authStore = useAuthStore();
const childStore = useChildStore();
const classStore = useClassStore();
const fundraiserStore = useFundraiserStore();
const transactionStore = useTransactionStore();
const childrenList = ref([]);
const props = defineProps({
  selectedFundraiser: {
    type: Object,
    required: true
  },
})
const emit = defineEmits(['refresh-fundraiser']);

const fundraiser = ref({...props.selectedFundraiser});
watch(
  () => props.selectedFundraiser,
  (newFundraiser) => {
    if (newFundraiser) {
      fundraiser.value = { ...newFundraiser };
      loadData();
    }
  },
  { deep: true }
);
const fundraiserStatusString = ref('');
const depositDisabled = ref(true);
const depositDisabledReason = ref('');
const loadData = async () => {
  if(childStore.childrenCount === 0){
    await childStore.fetchChildren();
  }

  childrenList.value = classStore.selectedClass.children;
  

  if(fundraiser.value.status && fundraiserStatuses[fundraiser.value.status]){
    fundraiserStatusString.value = fundraiserStatuses[fundraiser.value.status];
    if(fundraiser.value.status == "OPEN")
      depositDisabled.value = false;
    if(fundraiser.value.status == "PLANNED")
      depositDisabledReason.value = "zbiórka jeszcze się nie rozpoczęła"
    if(fundraiser.value.status == "CLOSED")
      depositDisabledReason.value = "zbiórka już się zakończyła"
    if(fundraiser.value.status == "CANCELLED")
      depositDisabledReason.value = "zbiórka została anulowana i wszystkie wpłacone środki zostały zwrócone"
     if(fundraiser.value.status == "BLOCKED")
      depositDisabledReason.value = "zbiórka została zablokowana przez administratora. Wszystkie wpłacone środki zostały zwrócone"
  }
};

const fundraiserStatuses = {
  PLANNED: "Zaplanowana",
  OPEN: "Otwarta",
  CLOSED: "Zakończona",
  CANCELLED: "Anulowana",
  BLOCKED: "Zablokowana"
};

onMounted(async () => {
  await loadData();
});
const rules = {
  required: value => !!value || 'To pole jest wymagane',
  minValue: min => value => value >= min || `Kwota nie może być mniejsza niż ${min} PLN`,
  maxValue: max => value => value <= max || `Kwota nie może przekraczać Twojego salda (${max} PLN)`
};
const isDepositFormValid = ref(true);
const depositData = reactive({
  fundraiserId: route.params.id,
  childrenId: null,
  classId: fundraiser.value.classId,
  amount: 0
})
const getChildFullname = (child) => {
  // Sprawdzenie, czy dziecko nie należy do aktualnie zalogowanego użytkownika
  if (child.parentId !== authStore.user.id) {
    // Znalezienie rodzica w classStore.selectedClass.parents na podstawie parentId
    const parent = classStore.selectedClass.parents.find(p => p.id === child.parentId);

    // Jeśli rodzic został znaleziony, dodaj jego imię i nazwisko do wyniku
    if (parent) {
      return `${child.firstName} ${child.lastName} (Dziecko ${parent.firstname} ${parent.lastname})`;
    }
  }

  // Jeżeli dziecko należy do zalogowanego użytkownika, zwróć tylko imię i nazwisko dziecka
  return `${child.firstName} ${child.lastName}`;
};

const deposit = async () => {
  try{
    await fundraiserStore.depositForFundraiser(depositData)
    await fundraiserStore.fetchFundraiserDetails(route.params.id)
    await authStore.getUserByToken();
    await transactionStore.reset();
    emit('refresh-fundraiser');
    snackbarSuccess(`Poprawnie wpłacono ${depositData.amount} PLN na zbiórkę`)
  }catch(error){
    snackbarError('Wystąpił błąd podczas wpłacania na zbiórkę.')
  }
}
</script>

<style scoped>
</style>