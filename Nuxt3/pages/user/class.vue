<template>
  <v-container class="px-12">
    <v-banner v-if="showBanner" class="bg-red" lines="one" :text="'Istnieje już klasa o nazwie: ' + newClassName + ' - Czy chcesz skontaktować się ze skarbnikiem żeby dodał cię do tej klasy?'" 
    :stacked="false" elevation="12">
  <template v-slot:actions>
    <v-btn @click="navigateTo('/user/chat')">Skontakuj</v-btn>
    <v-btn @click="showBanner = false">Zamknij</v-btn>
  </template>
</v-banner>
    <div class="text-h3 text-center font-weight-bold my-4">Twoje klasy</div>
    <v-row>
      <v-col v-for="_class in classList.sort((a, b) => a.className.localeCompare(b.className))" :key="_class" cols="12" md="4">

        <v-hover>
          <template v-slot:default="{ isHovering, props }">
            <v-card variant="tonal" elevation="8" class="d-flex flex-column justify-center align-center cursor-pointer"
              :color="isHovering ? 'hover' : undefined" v-bind="props" @click="navigateTo(`/class/${_class.id}`)">
              <v-icon size="10rem">mdi-account-group</v-icon>
              <h3 class="mt-2">{{ _class.className }}</h3>
            </v-card>
          </template>
        </v-hover>
      </v-col>
      <v-col cols="12" md="4">
        <v-hover>
          <template v-slot:default="{ isHovering, props }">
            <v-card variant="tonal" elevation="8"
              class="text-center grey d-flex flex-column align-center justify-center cursor-pointer" v-bind="props"
              :color="isHovering ? 'hover' : undefined" @click="showAddCard = true">
              <div>
                <v-icon size="10rem">mdi-plus-circle</v-icon>
                <h3 class="mt-2">Dodaj klasę</h3>
              </div>

              <v-expand-transition>
                <v-card v-if="showAddCard" class="position-absolute w-100 d-flex flex-column align-content-space-evenly"
                  height="100%" style="bottom: 0;">
                  <v-card-title>
                    <p class="text-h4">Dodaj klasę</p>
                  </v-card-title>
                  <v-card-text class="pb-0 align-content-center">
                    <v-form v-model="isAddFormValid">
                      <v-text-field v-model="newClassName" label="Nazwa klasy" :rules="[value => !!value || 'To pole jest wymagane']"/>
                    </v-form>
                  </v-card-text>

                  <v-card-actions class="pt-0 justify-center">
                    <v-btn text="Dodaj" color="green" variant="text" @click.stop="addClass();showAddCard = false" :disabled="!isAddFormValid"></v-btn>
                    <v-btn text="Anuluj" color="red" variant="text" @click.stop="showAddCard = false"></v-btn>
                  </v-card-actions>
                </v-card>
              </v-expand-transition>
            </v-card>
          </template>
        </v-hover>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { useAuthStore } from "~/stores/AuthStore";
import { useClassStore } from "~/stores/ClassStore";
import { useSnack } from '@/composables/useSnack';
import LoginForm from "~/components/loginForm.vue";
const { snackbarSuccess, snackbarError } = useSnack();
const classStore = useClassStore();
const authStore = useAuthStore();
const classList = ref([]);
const classCount = ref();

onMounted(async () => {
  if (classStore.classList.length === 0) {
    await classStore.fetchClassesByParent();
  }
  classList.value = classStore.classList;
  classCount.value = classStore.classCount;
});

const showAddCard = ref(false);
const isAddFormValid = ref(false);
const newClassName = ref('')
const showBanner = ref(false);
const addClass = async () => {
  try {
    const data = reactive({
      className: newClassName
    });

    await classStore.addClass(data);
    classList.value = classStore.classList;
    snackbarSuccess('Pomyślnie utworzono klasę.');
  } catch (error) {
    if (error.response && error.response.status === 409) {
      const treasurerData = error.response.data;
      showBanner.value = true
      snackbarError(
        `Klasa o tej nazwie już istnieje. Skarbnik: ${treasurerData.treasurerFirstName} ${treasurerData.treasurerLastName} (${treasurerData.treasurerEmail})`, 
        1000000,
      );
    } else {
      snackbarError('Wystąpił błąd podczas dodawania klasy.');
    }
  }
};

</script>

<style scoped>
.v-card {
  min-height: 300px;
}
</style>