<template>
  <v-card>
    <v-data-iterator
      :items="childrenInClass"
      :items-per-page="9"
      :search="search"
    >
      <template v-slot:header>
        <v-toolbar class="px-2 bg-surface">
          <v-tooltip text="Dzieci mogą zostać przypisane do klasy tylko przez ich rodziców">
  <template v-slot:activator="{ props }">
    <v-icon v-bind="props" class="mr-6">mdi-information</v-icon>
  </template>
</v-tooltip>
          <v-text-field
            class="mx-1"
            v-model="search"
            placeholder="Wyszukaj"
            prepend-inner-icon="mdi-magnify"
            variant="solo"
            clearable
            hide-details
            elevation="16"
          ></v-text-field>
        </v-toolbar>
      </template>

      <template v-slot:default="{ items }">
        <v-container class="pa-2" fluid>
          <v-row dense>
            <v-col
              v-for="item in items"
              :key="item.raw.id"
              cols="12"
              md="4"
            >
              <v-card class="pb-3" border flat>
                <v-list-item :subtitle="item.raw.firstName" class="mb-2">
                  <template v-slot:title>
                    <strong class="text-h6 mb-2">{{ item.raw.lastName }}</strong>
                  </template>
                </v-list-item>

                <div class="d-flex flex-column justify-space-between px-4">
                  <div class="d-flex align-center text-caption text-medium-emphasis me-1">
                    <div class="text-truncate">Data urodzenia: {{ item.raw.dob || "Brak podanej daty urodzenia" }}</div>
                  </div>
                  <div class="d-flex align-center text-caption text-medium-emphasis me-1">
                    <div class="text-truncate">Numer konta: {{ item.raw.accountNumber || "Brak numeru konta" }}</div>
                  </div>

                  <div class="d-flex justify-end">
                    <template v-if="confirmingDelete === item.raw.id">
                      <v-btn
                        color="green"
                        class="mr-1"
                        size="small"
                        icon="mdi-check"
                        @click="confirmDelete(item.raw.id)"
                      ></v-btn>
                      <v-btn
                        color="red"
                        size="small"
                        icon="mdi-close"
                        @click="cancelDelete"
                      ></v-btn>
                    </template>

                    <template v-else>
                      <v-btn
                        v-if="isTreasurer || item.raw.parentId === authStore.user.id"
                        class="text-none mr-1"
                        size="small"
                        icon="mdi-account-minus"
                        @click="startDelete(item.raw.id)"
                        border
                        flat
                      ></v-btn>
                    </template>
                  </div>
                </div>
              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </template>

      <template v-slot:footer="{ page, pageCount, prevPage, nextPage }">
        <div class="d-flex align-center justify-center pa-4">
          <v-btn
            :disabled="page === 1"
            density="comfortable"
            icon="mdi-arrow-left"
            variant="tonal"
            rounded
            @click="prevPage"
          ></v-btn>

          <div class="mx-2 text-caption">
            Strona {{ page }} z {{ pageCount }}
          </div>

          <v-btn
            :disabled="page >= pageCount"
            density="comfortable"
            icon="mdi-arrow-right"
            variant="tonal"
            rounded
            @click="nextPage"
          ></v-btn>
        </div>
      </template>
    </v-data-iterator>
  </v-card>
</template>

<script setup>
const { snackbarSuccess, snackbarError } = useSnack();

const route = useRoute();
const authStore = useAuthStore();
const classStore = useClassStore();
const childStore = useChildStore();
const search = ref('');
const confirmingDelete = ref(null);
const childrenInClass = ref([]);

const props = defineProps({
  classData: {
    type: Object,
    required: true
  },
  isTreasurer: {
    type: Boolean,
    required: true
  }
});
const classDataCopy = ref({ ...props.classData });

onMounted(async () => {
  await childStore.fetchChildrenByClass(route.params.id)
  childrenInClass.value = childStore.childrenListInClass;
})

watch(() => props.classData, (newClassData) => {
  classDataCopy.value = { ...newClassData };
}, { deep: true });

const loadClassData = async () => {
  try {
    await classStore.fetchClassById(route.params.id);
    classDataCopy.value = { ...classStore.selectedClass };
    emit('loadClass', classStore.selectedClass); 
  } catch (error) {
    console.error("Błąd podczas odświeżania danych klasy:", error);
  }
};

const startDelete = (childId) => {
  confirmingDelete.value = childId;
};

const confirmDelete = async (childId) => {
  try {
    confirmingDelete.value = null;
    await childStore.deleteChildFromClass(childId);
    await childStore.fetchChildrenByClass(route.params.id);
    childrenInClass.value = childStore.childrenListInClass;
    await childStore.fetchChildren();
    loadClassData();
    snackbarSuccess('Pomyślnie usunięto dziecko z klasy.');
  } catch (error) {
    console.log(error)
    snackbarError('Wystąpił błąd podczas usuwania dziecka z klasy.');
  }
};

const cancelDelete = () => {
  confirmingDelete.value = null;
};
</script>

<style scoped>
</style>
