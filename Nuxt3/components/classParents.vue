<template>
  <v-card>
    <v-data-iterator
      :items="classDataCopy.parents"
      :items-per-page="9"
      :search="search"
    >
      <template v-slot:header>
        <v-toolbar class="px-2 bg-surface">
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
                <v-list-item :subtitle="item.raw.firstname" class="mb-2">
                  <template v-slot:title>
                    <strong class="text-h6 mb-2">{{ item.raw.lastname }}</strong>
                    <strong v-if="classData.treasurer.id === item.raw.id" class="text-h6 mb-2"> - Skarbnik</strong>
                  </template>
                </v-list-item>

                <div class="d-flex flex-column justify-space-between px-4">
                  <div class="d-flex align-center text-caption text-medium-emphasis me-1">
                    <div class="text-truncate">{{ item.raw.email || "Brak maila" }}</div>
                  </div>
                  <div class="d-flex align-center text-caption text-medium-emphasis me-1">
                    <div class="text-truncate">{{ item.raw.accountNumber || "Brak numeru konta" }}</div>
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
                        v-if="(isTreasurer && item.raw.id !== authStore.user.id) || (item.raw.id === authStore.user.id && !isTreasurer)"
                        class="text-none mr-1"
                        size="small"
                        icon="mdi-account-minus"
                        @click="startDelete(item.raw.id)"
                        border
                        flat
                      ></v-btn>
                      <v-btn
                      @click="navigateTo('/user/chat')"
                        class="text-none"
                        size="small"
                        icon="mdi-message"
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
const search = ref('');
const confirmingDelete = ref(null);

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

const startDelete = (parentId) => {
  confirmingDelete.value = parentId;
};

const confirmDelete = async (parentId) => {
  try {
    confirmingDelete.value = null;
    await classStore.deleteParent(parentId, route.params.id);
    loadClassData();
    snackbarSuccess('Pomyślnie usunięto rodzica.');
  } catch (error) {
    console.log(error)
      snackbarError('Wystąpił błąd podczas usuwania rodzica.');
  }
};

const cancelDelete = () => {
  confirmingDelete.value = null;
};
</script>

<style scoped>
</style>
