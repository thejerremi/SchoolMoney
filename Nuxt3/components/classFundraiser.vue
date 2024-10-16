<template>
  <div class="ma-1">
    <v-dialog max-width="500">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn append-icon="mdi-plus-circle" variant="outlined" v-bind="activatorProps">Utwórz zbiórkę</v-btn>

      </template>

      <template v-slot:default="{ isActive }">
        <v-card title="Utwórz zbiórkę">
          <v-card-text>
            <v-form v-model="isAddFormValid" ref="form">
              <v-text-field label="Cel (Oczekiwana kwota zbiórki)" v-model="createdFundraiser.goal"
                :rules="[rules.required, rules.positiveNumber]" type="number"></v-text-field>

              <v-text-field label="Krótki opis" v-model="createdFundraiser.shortDescription"
                :rules="[rules.required]"></v-text-field>

              <v-date-input label="Data rozpoczęcia" v-model="createdFundraiser.startDate"
                :rules="[rules.required, rules.date, rules.notPastDate]" placeholder="DD.MM.RRRR"
                required></v-date-input>

              <v-date-input v-model="createdFundraiser.endDate" label="Data zakończenia"
                :disabled="createdFundraiser.startDate == null"
                :rules="[rules.required, rules.date, rules.notBeforeStart]" placeholder="DD.MM.RRRR"
                required></v-date-input>
              <v-file-input label="Wybierz logo zbiórki" v-model="createdFundraiser.logo"
                :rules="[rules.fileRequired, rules.isImage, rules.maxFileSize]"
                accept="image/png, image/jpeg, image/jpg, image/gif" prepend-icon="mdi-camera" outlined dense required
                show-size></v-file-input>
            </v-form> </v-card-text>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn @click="submitFundraiser(); isActive.value = false" :disabled="!isAddFormValid">Utwórz Zbiórkę</v-btn>

            <v-btn text="Anuluj" @click="isActive.value = false"></v-btn>
          </v-card-actions>
        </v-card>
      </template>
    </v-dialog>
  </div>

  <div>
    <v-expansion-panels variant="accordion" class="border-xl rounded-sm">
      <v-expansion-panel v-for="fundraiser in classFundraisers" :key="fundraiser"
        :title="fundraiser.shortDescription || 'Brak opisu'">
        <v-expansion-panel-text>
          <v-card>
            <v-card-title class="text-overline">
              Zebrane fundusze
              <div class="text-green-darken-3 text-h3 font-weight-bold">{{ ((fundraiser.currentAmount / fundraiser.goal) * 100).toFixed(2) }}%
              </div>
              <div class="text-h6 text-medium-emphasis font-weight-regular">
                {{ (fundraiser.goal - fundraiser.currentAmount) > 0 ? (fundraiser.goal - fundraiser.currentAmount) : 0 }} PLN do osiągnięcia celu
              </div>
            </v-card-title>
            <v-card-text>
              <v-progress-linear color="green-darken-3" height="22"
                :model-value="(fundraiser.currentAmount / fundraiser.goal) * 100" rounded="lg">
              </v-progress-linear>

              <div class="d-flex justify-space-between py-3">
                <span class="text-green-darken-3 font-weight-medium">
                  Wpłacono: {{ fundraiser.currentAmount }} PLN
                </span>

                <span class="text-medium-emphasis"> Cel: {{ fundraiser.goal }} PLN</span>
              </div>
            </v-card-text>

            <v-divider></v-divider>

            <v-list-item append-icon="mdi-chevron-right" lines="one" subtitle="Szczegóły zbiórki" class="text-black"
              link @click="navigateTo(`fundraiser/${fundraiser.id}`)"></v-list-item>
          </v-card>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
  </div>
</template>

<script setup>
const { snackbarSuccess, snackbarError } = useSnack();
const fundraiserStore = useFundraiserStore();
const route = useRoute();
const date = useDate();
const classFundraisers = ref([]);
onMounted(async () => {
  await fundraiserStore.fetchFundraiserByClassId(route.params.id)
  classFundraisers.value = fundraiserStore.classFundraisers;
})


const createdFundraiser = ref({
  goal: 0,
  shortDescription: "",
  startDate: null,
  endDate: null,
  logo: null,
  classId: route.params.id
});
const today = new Date();
const isAddFormValid = ref(false);
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
    return enteredDate >= date.formatDateWithoutTimezone(createdFundraiser.value.startDate) || 'Data zakończenia nie może być wcześniejsza niż data startu';
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
const submitFundraiser = async () => {
  try{
    const formData = new FormData();
  formData.append('goal', createdFundraiser.value.goal);
  formData.append('shortDescription', createdFundraiser.value.shortDescription);
  formData.append('startDate', date.formatDateWithoutTimezone(createdFundraiser.value.startDate));
  formData.append('endDate', date.formatDateWithoutTimezone(createdFundraiser.value.endDate));
  formData.append('classId', createdFundraiser.value.classId);

  if (createdFundraiser.value.logo) {
    formData.append('logo', createdFundraiser.value.logo);
  }
  await fundraiserStore.createFundraiser(formData, route.params.id);
  await fundraiserStore.fetchFundraiserByClassId(route.params.id)
  classFundraisers.value = fundraiserStore.classFundraisers;
  snackbarSuccess("Pomyślnie utworzono zbiórkę.")
  }catch(error){
    snackbarError('Wystąpił błąd podczas tworzenia zbiórki.');
  }
};
</script>

<style></style>