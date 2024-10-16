<template>
  <v-card class="mx-auto" fill-height>
    <v-card-title>
      Użytkownicy
    </v-card-title>

    <v-divider></v-divider>

    <v-virtual-scroll :items="usersList" height="320" item-height="48">
      <template v-slot:default="{ item }">
        <v-list-item :subtitle="item.email" :title="`${item.firstname} ${item.lastname} (${item.id})`">
          <template v-slot:prepend>
            <v-icon>mdi-account</v-icon>
          </template>
          <template v-slot:append>
            <div class="d-flex ga-2">

              <v-dialog max-width="500">
                <template v-slot:activator="{ props: activatorProps }">
                  <v-btn v-bind="activatorProps" icon="mdi-magnify" size="x-small" variant="tonal"></v-btn>
                </template>

                <template v-slot:default="{ isActive }">
                  <v-card :title="item.firstname + ' ' + item.lastname">
                    <v-card-text>
                      <v-list lines="one">
                        <v-list-item title="Id:" :subtitle="item.id" />
                        <v-list-item title="Email:" :subtitle="item.email" />
                        <v-list-item title="Pesel:" :subtitle="item.pesel" />
                        <v-list-item title="Data urodzenia:" :subtitle="item.dob" />
                        <v-list-item title="Stan konta:" :subtitle="item.balance + ' PLN'" />
                        <v-list-item title="Numer konta:" :subtitle="item.accountNumber" />
                        <v-list-item title="Rola w bazie:" :subtitle="item.role" />
                        <v-list-item title="Czy konto aktywne:" :subtitle="item.isNonLocked ? 'Tak' : 'Nie'" />
                        <v-list-item title="Skarbnik w klasach:" />
                        <div class="ml-4 mt-n2 font-weight-bold" v-for="(classItem, index) in item.treasurerOfClasses" :key="index">
                          • {{ classItem.className }}
                      </div>
                      </v-list>
                    </v-card-text>

                    <v-card-actions>
                      <v-spacer></v-spacer>

                      <v-btn text="Zamknij" @click="isActive.value = false"></v-btn>
                    </v-card-actions>
                  </v-card>
                </template>
              </v-dialog>


              <v-tooltip text="Zablokuj konto" v-if="item.isNonLocked">
                <template v-slot:activator="{ props }">
                  <v-btn @click="lockUser(item.id)" v-bind="props" color="red" icon="mdi-account-cancel" size="x-small"
                    variant="tonal"></v-btn>
                </template>
              </v-tooltip>

              <v-tooltip text="Odblokuj konto" v-if="!item.isNonLocked">
                <template v-slot:activator="{ props }">
                  <v-btn @click="unlockUser(item.id)" v-bind="props" color="green" icon="mdi-account-cancel"
                    size="x-small" variant="tonal"></v-btn>
                </template>
              </v-tooltip>
            </div>
          </template>
        </v-list-item>
      </template>
    </v-virtual-scroll>
  </v-card>


  <v-card class="mx-auto mt-2" fill-height>
    <v-card-title>
      Klasy
    </v-card-title>

    <v-divider></v-divider>

    <v-virtual-scroll :items="classesList" height="320" item-height="48">
      <template v-slot:default="{ item }">
        <v-list-item :subtitle="`Ilość zbiórek: ${countFundraisersInClass(item.id)}`" :title="item.className">
          <template v-slot:prepend>
            <v-icon>mdi-account-group</v-icon>
          </template>
          <template v-slot:append>
            <v-tooltip text="Przejdź do klasy">
                <template v-slot:activator="{ props }">
                  <v-btn @click="goToClass(item)" v-bind="props" icon="mdi-magnify" size="x-small"
                    variant="tonal"></v-btn>
                </template>
              </v-tooltip>
          </template>
          
        </v-list-item>
      </template>
    </v-virtual-scroll>
  </v-card>

  <div class="d-flex mx-10 mt-3">
    <v-select v-model="selectedParent" :items="usersList" :item-title="getParentFullName" item-value="id" />
    <v-btn class="mt-3 ml-3" @click="generateReport">Pobierz raport finansowy</v-btn>
  </div>
</template>

<script setup>
const { snackbarSuccess, snackbarError, clearSnackbars, snackbarInfo } = useSnack();
const adminStore = useAdminStore();
const classStore = useClassStore();
import reportService from '@/services/reportService';
const fundraiserStore = useFundraiserStore();
const usersList = ref([]);
const classesList = ref([]);
const fundraisersList = ref([]);
onMounted(async () => {
  await adminStore.fetchAllUsers();
  usersList.value = adminStore.usersList;
  await adminStore.fetchAllClasses();
  classesList.value = adminStore.classesList;
  await adminStore.fetchAllFundraisers();
  fundraisersList.value = adminStore.fundraisersList;
});

const lockUser = async (id) => {
  try {
    await adminStore.lockUser(id);
    await adminStore.fetchAllUsers();
    usersList.value = adminStore.usersList;
    snackbarSuccess('Zablokowano użytkownika.');
  } catch (error) {
    snackbarError('Wystąpił błąd podczas blokowania użytkownika.');
  }
}

const unlockUser = async (id) => {
  try {
    await adminStore.unlockUser(id);
    await adminStore.fetchAllUsers();
    usersList.value = adminStore.usersList;
    snackbarSuccess('Odblokowano użytkownika.');
  } catch (error) {
    snackbarError('Wystąpił błąd podczas odblokowywania użytkownika.');
  }
}

const countFundraisersInClass = (id) =>{
  return fundraisersList.value.filter(fundraiser => fundraiser.classId === id).length;
}

const goToClass = (_class) => {
  classStore.selectedClass = _class;
  navigateTo('/class/' + _class.id);
}

const selectedParent = ref();
const getParentFullName = (parent) => {
  if(parent.role === 'ADMIN')
    return "Administrator";
  return `${parent.firstname} ${parent.lastname} - ${parent.email}`;
};

const generateReport = async () => {
  try {
    snackbarInfo('Trwa generowanie raportu...', 150000)
    const response = await reportService.fetchAllParentTransactions(selectedParent.value);

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
</script>

<style scoped></style>