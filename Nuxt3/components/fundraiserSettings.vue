<template>
  <div class="d-flex flex-column">
    <h3 class="font-weight-bold">Zmień właściciela zbiórki</h3>
    <div class="d-flex ga-5">
      <v-autocomplete v-model="selectedParent" :items="parents" :loading="loading" v-model:search="search"
        :hide-no-data="true" label="Zmień właściciela zbiórki" :item-title="getParentFullName" item-value="id"
        hide-details :disabled="selectedFundraiser.status === 'CANCELLED' && authStore.user.role !== 'ADMIN'"></v-autocomplete>
      <v-btn v-if="selectedParent" :disabled="selectedFundraiser.status === 'CANCELLED' && authStore.user.role !== 'ADMIN'" class="align-self-center"
        color="green" @click="updateParent()">Zmień</v-btn>
    </div>
    <div class="pt-5">
      <h3 class="font-weight-bold">Wypłać pieniądze ze zbiórki na swoje konto (dostępne: {{
        selectedFundraiser.availableFunds }} PLN)</h3>
      <v-form v-model="isWithdrawFormValid">
        <div class="d-flex ga-5">
          <v-text-field label="Kwota" type="number" v-model="withdrawAmount"
            :rules="[withdrawRules.minValue(0.01), withdrawRules.maxValue(selectedFundraiser.availableFunds)]" />
          <v-btn v-if="isWithdrawFormValid" class="mt-3"
            :disabled="!isWithdrawFormValid || (selectedFundraiser.status !== 'OPEN' && selectedFundraiser.status !== 'CLOSED')"
            color="green" @click="withdraw()">Wypłać</v-btn>
        </div>
      </v-form>
    </div>
    <div v-if="authStore.user.role === 'ADMIN'">
      <h3 class="font-weight-bold">Zablokuj zbiórkę (pieniądze zostaną zwrócone do właścicieli) (Widzisz tę możliwość, ponieważ jesteś administratorem)</h3>
      <div class="d-flex flex-column justify-center">
        <v-btn color="red" @click="lockFundraiser()">
          Zablokuj
        </v-btn>
      </div>
    </div>
    <div class="pt-5" v-if="selectedFundraiser.status !== 'CANCELLED' || authStore.user.role === 'ADMIN'">
      <h3 class="font-weight-bold">Anuluj zbiórkę (pieniądze zostaną zwrócone do właścicieli)</h3>
      <div class="d-flex flex-column justify-center">
        <v-btn color="red" @click="showConfirmCancelButtons = true">
          Anuluj
        </v-btn>



        <v-slide-y-transition>
          <div v-if="showConfirmCancelButtons" class="d-flex justify-center mt-3">
            <v-btn color="green" @click="confirmCancel" @mouseenter="showWarning = true"
              @mouseleave="showWarning = false">
              Potwierdź
            </v-btn>
            <v-btn color="red" class="ml-2" @click="showConfirmCancelButtons = false;">
              Anuluj
            </v-btn>
          </div>
        </v-slide-y-transition>
        <v-slide-y-transition>
          <h3 v-if="showWarning" class="font-weight-bold text-center mt-3 text-red">
            UWAGA! Ta operacja jest permanentna i nie można jej cofnąć!
          </h3>
        </v-slide-y-transition>
      </div>
    </div>
    <div class="pt-5" v-if="selectedFundraiser.status !== 'CANCELLED' || authStore.user.role === 'ADMIN'">
      <h3 class="font-weight-bold">Edytuj zbiórkę</h3>
      <div class="d-flex flex-column">
        <v-card v-if="selectedFundraiser">
          <v-card-text>
            <v-form v-model="isEditFormValid" ref="form">
              <v-text-field label="Cel (Oczekiwana kwota zbiórki)" v-model="editedFundraiser.goal"
                :rules="[rules.required, rules.positiveNumber]" type="number"></v-text-field>

              <v-text-field label="Krótki opis" v-model="editedFundraiser.shortDescription"
                :rules="[rules.required]"></v-text-field>

              <div class="d-flex">

                <div class="w-50">
                  <v-date-input  v-bind="props" label="Data rozpoczęcia" v-model="editedFundraiser.startDate"
                  @mouseenter="test = true" :rules="[rules.required, rules.date, rules.notPastDate]"
                  persistent-hint hint="Datę rozpoczęcia można edytować tylko w zaplanowanych zbiórkach"
                  placeholder="DD.MM.RRRR" required :disabled="selectedFundraiser.status !== 'PLANNED'" />
                </div>
                <div class="w-50">
                  <v-date-input v-bind="props" v-model="editedFundraiser.endDate" label="Data zakończenia"
                  :disabled="editedFundraiser.startDate == null || selectedFundraiser.status === 'CLOSED' || selectedFundraiser.status === 'CANCELLED' || selectedFundraiser.status === 'BLOCKED'"
                  persistent-hint hint="Datę rozpoczęcia można edytować tylko w zaplanowanych i trwających zbiórkach"
                  :rules="[rules.required, rules.date, rules.notBeforeStart]" placeholder="DD.MM.RRRR"
                  required />
                </div>
                


              </div>
              <v-file-input class="mt-1" label="Wybierz logo zbiórki (niewymagane)" v-model="editedFundraiser.logo"
                :rules="[rules.isImage, rules.maxFileSize]" accept="image/png, image/jpeg, image/jpg, image/gif"
                prepend-icon="mdi-camera" outlined dense required show-size></v-file-input>
            </v-form> </v-card-text>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn @click="editFundraiser();" :disabled="!isEditFormValid || !isAnythingDirty()">Edytuj zbiórkę</v-btn>
          </v-card-actions>
        </v-card>
      </div>
    </div>
  </div>
</template>

<script setup>
const { snackbarSuccess, snackbarError } = useSnack();
const route = useRoute();
const authStore = useAuthStore();
const adminStore = useAdminStore();
const classStore = useClassStore();
const fundraiserStore = useFundraiserStore();
const transactionStore = useTransactionStore();
const date = useDate();
const props = defineProps({
  selectedFundraiser: {
    type: Object,
    required: true
  },
})
const emit = defineEmits(['refresh-fundraiser', 'change-tab']);
const selectedParent = ref(null);
const parents = ref([]);
const loading = ref(false);
const search = ref('');

watch(search, (newValue) => {
  if (newValue && !selectedParent.value) {
    fetchParents(newValue);
  }
});

const fetchParents = async (searchInput) => {
  if (!searchInput) return;
  console.log('Wywołanie fetchParents po klasie z tekstem: ', searchInput);
  loading.value = true;
  try {
    const data = await authStore.searchParentsByClass(searchInput, classStore.selectedClass.id);
    console.log(data)
    parents.value = data;
  } catch (error) {
    console.error("Błąd podczas pobierania rodziców:", error);
  } finally {
    loading.value = false;
  }
};

const updateParent = async () => {
  try {
    await fundraiserStore.updateFundraiserOwner(route.params.id, selectedParent.value);
    emit('refresh-fundraiser');
    emit('change-tab');
    snackbarSuccess('Pomyślnie zaktualizowano właściciela zbiórki.');
  } catch (error) {
    console.log(error)
    snackbarError('Wystąpił błąd podczas aktualizowania właściciela zbiórki.');
  }
};

const getParentFullName = (parent) => {
  if(parent.role === 'ADMIN')
    return "Administrator";
  return `${parent.firstname} ${parent.lastname} - ${parent.email}`;
};

const isWithdrawFormValid = ref(false);
const withdrawAmount = ref();
const withdrawRules = {
  minValue: min => value => value >= min || `Kwota nie może być mniejsza niż ${min} PLN`,
  maxValue: max => value => value <= max || `Kwota nie może przekraczać dostępnych środków zbiórki (${max} PLN)`
};
const withdraw = async () => {
  try {
    await fundraiserStore.withdrawFromFundraiser(withdrawAmount.value, route.params.id);
    await authStore.getUserByToken();
    emit('refresh-fundraiser');
    snackbarSuccess(`Pomyślnie zaktualizowano wypłacono ${withdrawAmount.value} PLN ze zbiórki.`);
  } catch (error) {
    snackbarError('Wystąpił błąd podczas wypłacania pieniędzy ze zbiórki.');
  }
}

const showConfirmCancelButtons = ref(false);
const showWarning = ref(false);

const confirmCancel = async () => {
  try {
    showConfirmCancelButtons.value = false;
    await fundraiserStore.cancelFundraiser(route.params.id)
    emit('refresh-fundraiser');
    emit('change-tab');
    await authStore.getUserByToken();
    await transactionStore.reset();
    snackbarSuccess('Pomyślnie anulowano zbiórkę.');
  } catch (error) {
    snackbarError('Wystąpił błąd podczas anulowania zbiórki.');
  }
};

const editedFundraiser = ref({
  goal: props.selectedFundraiser.goal,
  shortDescription: props.selectedFundraiser.shortDescription,
  startDate: new Date(props.selectedFundraiser.startDate),
  endDate: new Date(props.selectedFundraiser.endDate),
  logo: null,
  classId: route.params.id
});
const today = new Date();
const isEditFormValid = ref(false);
const MAX_FILE_SIZE = 5 * 1024 * 1024;
const rules = {
  required: value => !!value || 'To pole jest wymagane',
  positiveNumber: value => value > 0 || 'Kwota musi być większa niż 0',
  date: value => /^\d{2}\.\d{2}\.\d{4}$/.test(value) || 'Niepoprawny format daty (DD.MM.RRRR)',
  notPastDate: value => {
    const [day, month, year] = value.split('.');
    const enteredDate = date.formatDateWithoutTimezone(new Date(`${year}-${month}-${day}`));
    return enteredDate >= date.formatDateWithoutTimezone(today) || 'Data nie może być z przeszłości';
  },
  notBeforeStart: value => {
    const [day, month, year] = value.split('.');
    const enteredDate = date.formatDateWithoutTimezone(new Date(`${year}-${month}-${day}`));
    return enteredDate >= date.formatDateWithoutTimezone(editedFundraiser.value.startDate) || 'Data zakończenia nie może być wcześniejsza niż data startu';
  },
  isImage: value => {
    if (!value || !value.length) return true;
    const validExtensions = ['jpg', 'jpeg', 'png', 'gif'];
    const fileExtension = value[0].name.split('.').pop().toLowerCase();
    const isMimeTypeValid = value[0].type.startsWith('image/');

    return validExtensions.includes(fileExtension) && isMimeTypeValid
      || 'Plik musi być obrazem (jpg, jpeg, png, gif)';
  },

  // Sprawdzenie rozmiaru pliku
  maxFileSize: value => {
    return !value || !value.length || value[0].size < MAX_FILE_SIZE || `Logo nie może być większe niż 5MB!`
  },
  fileRequired: value => !!value[0] || 'Logo jest wymagane'
};
// Przechowywanie początkowych wartości zbiórki
const originalFundraiser = ref({ ...editedFundraiser.value });

// Funkcja do sprawdzenia, czy wartość została zmieniona
const isDirty = (field) => {
  return originalFundraiser.value[field] !== editedFundraiser.value[field];
};
// Funkcja do sprawdzenia, czy cokolwiek zostało zmienione
const isAnythingDirty = () => {
  return (
    isDirty('goal') ||
    isDirty('shortDescription') ||
    isDirty('startDate') ||
    isDirty('endDate') ||
    isDirty('classId') ||
    !!editedFundraiser.value.logo  // Sprawdzamy, czy logo zostało zmienione
  );
};
// Funkcja do edytowania zbiórki
const editFundraiser = async () => {
  try {
    const formData = new FormData();

    // Dodajemy tylko zmienione wartości
    if (isDirty('goal')) {
      formData.append('goal', editedFundraiser.value.goal);
    }
    if (isDirty('shortDescription')) {
      formData.append('shortDescription', editedFundraiser.value.shortDescription);
    }
    if (isDirty('startDate')) {
      formData.append('startDate', date.formatDateWithoutTimezone(editedFundraiser.value.startDate));
    }
    if (isDirty('endDate')) {
      formData.append('endDate', date.formatDateWithoutTimezone(editedFundraiser.value.endDate));
    }
    if (isDirty('classId')) {
      formData.append('classId', editedFundraiser.value.classId);
    }

    if (editedFundraiser.value.logo) {
      formData.append('logo', editedFundraiser.value.logo);
    }

    await fundraiserStore.updateFundraiser(route.params.id, formData);
    emit('refresh-fundraiser');

    originalFundraiser.value = { ...editedFundraiser.value };
    snackbarSuccess("Pomyślnie edytowano zbiórkę.");
  } catch (error) {
    snackbarError('Wystąpił błąd podczas edytowania zbiórki.');
  }
};

const lockFundraiser = async () => {
  await adminStore.lockFundraiser(route.params.id);
  emit('refresh-fundraiser');
  snackbarSuccess('Zablokowano zbiórkę.');
}
</script>

<style></style>