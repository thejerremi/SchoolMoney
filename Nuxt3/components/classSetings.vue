<template>
  <div class="d-flex flex-column">
    <h1 class="text-center font-weight-bold" v-if="!isTreasurer">
      Ustawienia dostępne tylko dla skarbnika klasy
    </h1>
    <h2
      class="text-center font-weight-bold font-italic"
      v-if="authStore.user.role === 'ADMIN'"
    >
      Masz dostęp do ustawień, ponieważ jesteś administratorem
    </h2>
    <h3 class="font-weight-bold">Dodaj rodzica</h3>
    <div class="d-flex ga-5">
      <v-autocomplete
        :disabled="!isTreasurer && authStore.user.role !== 'ADMIN'"
        v-model="selectedParent"
        :items="parents"
        :loading="loading"
        v-model:search="search"
        :hide-no-data="true"
        label="Dodaj rodzica"
        :item-title="getParentFullName"
        item-value="id"
        hide-details
      ></v-autocomplete>
      <v-btn
        v-if="selectedParent"
        class="align-self-center"
        color="green"
        @click="addParent()"
        >Dodaj</v-btn
      >
    </div>
    <div class="mt-12">
      <h3 class="font-weight-bold">Zmień nazwę klasy</h3>
      <div class="d-flex ga-4">
        <v-text-field
          :disabled="!isTreasurer && authStore.user.role !== 'ADMIN'"
          class=""
          label="Nazwa klasy:"
          v-model="classDataCopy.className"
        />
        <v-btn
          v-if="classData.className !== classDataCopy.className"
          class="align-self-center mb-6"
          color="green"
          @click="updateClassName()"
          >Zapisz</v-btn
        >
      </div>
    </div>
    <div class="mt-12">
      <h3 class="font-weight-bold">Zmień skarbnika klasy</h3>
      <div class="d-flex ga-5">
        <v-autocomplete
          :disabled="!isTreasurer && authStore.user.role !== 'ADMIN'"
          v-model="selectedTreasurer"
          :items="classDataCopy.parents"
          :loading="loading"
          v-model:search="searchTreasurer"
          :hide-no-data="true"
          label="Zmień skarbnika"
          :item-title="getParentFullName"
          item-value="id"
          hide-details
        ></v-autocomplete>
        <v-btn
          v-if="
            selectedTreasurer &&
            selectedTreasurer !== classDataCopy.treasurer.id
          "
          class="align-self-center"
          color="green"
          @click="updateClassTreasurer()"
          >Zmień</v-btn
        >
      </div>
    </div>
    <div class="py-12">
      <h3 class="font-weight-bold">Usuń klasę</h3>
      <div class="d-flex flex-column justify-center">
        <v-btn
          :disabled="!isTreasurer && authStore.user.role !== 'ADMIN'"
          color="red"
          @click="showConfirmDeleteButtons = true"
        >
          USUŃ
        </v-btn>

        <v-slide-y-transition>
          <div
            v-if="showConfirmDeleteButtons"
            class="d-flex justify-center mt-3"
          >
            <v-btn
              color="green"
              @click="confirmDelete"
              @mouseenter="showWarning = true"
              @mouseleave="showWarning = false"
            >
              Potwierdź
            </v-btn>
            <v-btn
              color="red"
              class="ml-2"
              @click="showConfirmDeleteButtons = false"
            >
              Anuluj
            </v-btn>
          </div>
        </v-slide-y-transition>
        <v-slide-y-transition>
          <h3
            v-if="showWarning"
            class="font-weight-bold text-center mt-3 text-red"
          >
            UWAGA! Ta operacja jest permanentna i nie można jej cofnąć!
          </h3>
        </v-slide-y-transition>
      </div>
    </div>
  </div>
</template>

<script setup>
const authStore = useAuthStore();
const classStore = useClassStore();
const route = useRoute();
const { snackbarSuccess, snackbarError } = useSnack();

const props = defineProps({
  classData: {
    type: Object,
    required: true,
  },
  isTreasurer: {
    type: Boolean,
    required: true,
  },
});

const classDataCopy = ref({ ...props.classData });

watch(
  () => props.classData,
  (newClassData) => {
    classDataCopy.value = { ...newClassData };
  },
  { deep: true }
);

const loadClassData = async () => {
  try {
    await classStore.fetchClassById(route.params.id);
    classDataCopy.value = { ...classStore.selectedClass };
    emit("loadClass", classStore.selectedClass);
  } catch (error) {
    console.error("Błąd podczas odświeżania danych klasy:", error);
  }
};

const emit = defineEmits(["loadClass"]);

const selectedParent = ref(null);
const parents = ref([]);
const loading = ref(false);
const search = ref("");

watch(search, (newValue) => {
  if (newValue && !selectedParent.value) {
    fetchParents(newValue);
  }
});

const fetchParents = async (searchInput) => {
  if (!searchInput) return;
  console.log("Wywołanie fetchParents z tekstem: ", searchInput);
  loading.value = true;
  try {
    const data = await authStore.searchParents(searchInput);
    console.log(data);
    parents.value = data;
  } catch (error) {
    console.error("Błąd podczas pobierania rodziców:", error);
  } finally {
    loading.value = false;
  }
};

const getParentFullName = (parent) => {
  if (parent.role === "ADMIN") return "Administrator";
  return `${parent.firstname} ${parent.lastname} - ${parent.email}`;
};

const addParent = async () => {
  try {
    await classStore.addParent(selectedParent.value, route.params.id);
    await loadClassData();
    snackbarSuccess("Pomyślnie dodano rodzica.");
  } catch (error) {
    if (error.response && error.response.status === 409) {
      snackbarError("Rodzic już jest w tej klasie.");
    } else {
      snackbarError("Wystąpił błąd podczas dodawania rodzica.");
    }
  }
};

const updateClassName = async () => {
  try {
    const data = reactive({
      id: route.params.id,
      newClassName: classDataCopy.value.className,
    });
    await classStore.updateClassName(data);
    await loadClassData();
    snackbarSuccess("Pomyślnie zaktualizowano nazwę.");
  } catch (error) {
    snackbarError("Wystąpił błąd podczas aktualizowania nazwy.");
  }
};

const searchTreasurer = ref("");
const selectedTreasurer = ref();
const updateClassTreasurer = async () => {
  try {
    const data = reactive({
      classId: route.params.id,
      newTreasurerId: selectedTreasurer,
    });
    await classStore.updateClassTreasurer(data);
    await loadClassData();
    snackbarSuccess("Pomyślnie zaktualizowano skarbnika.");
  } catch (error) {
    console.log(error);
    snackbarError("Wystąpił błąd podczas aktualizowania skarbnika.");
  }
};

const showConfirmDeleteButtons = ref(false);
const showWarning = ref(false);

const confirmDelete = async () => {
  try {
    showConfirmDeleteButtons.value = false;
    await classStore.deleteClass(route.params.id);
    snackbarSuccess("Pomyślnie usunięto klasę.");
    navigateTo("/user/class");
  } catch (error) {
    snackbarError("Wystąpił błąd podczas usuwania klasy.");
  }
};
</script>
