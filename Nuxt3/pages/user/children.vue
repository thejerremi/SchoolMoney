<template>
  <v-container class="px-12">
    <div class="text-h3 text-center font-weight-bold my-4">Twoje dzieci</div>
    <v-row>
      <v-col v-for="child in childrenList.sort((a, b) => a.firstName.localeCompare(b.firstName))
" :key="child" cols="12" md="4">
        <v-card variant="tonal" elevation="8">
          <v-card-title class="text-center">{{ child.firstName }} {{ child.lastName }}</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item title="Klasa:" :subtitle="getClassNameForChild(child) || 'Brak klasy'" />
              <v-list-item title="Data urodzenia:" :subtitle="child.dob" />
              <v-list-item title="Nr. rachunku" :subtitle="child.accountNumber" />
            </v-list>
          </v-card-text>
          <v-card-actions class="justify-center">
            <v-btn variant="outlined" color="green" @click="toggleEdit(child.id)">Edytuj</v-btn>
            <v-btn variant="outlined" color="red" @click="toggleDelete(child.id)">Usuń</v-btn>
          </v-card-actions>

          <v-expand-transition>
            <v-card v-if="showEditChild[child.id]" class="position-absolute w-100 text-center" height="100%"
              style="bottom: 0;" title="Edycja dziecka">
              <v-form v-model="isEditFormValid[child.id]" ref="formRef">
                <v-card-text>
                  <div class="d-flex ga-2">
                    <v-text-field v-model="editedChildren[child.id].firstName" label="Imię" :rules="[rules.required]"
                      required></v-text-field>
                    <v-text-field v-model="editedChildren[child.id].lastName" label="Nazwisko" :rules="[rules.required]"
                      required></v-text-field>
                  </div>
                  <div class="d-flex ga-2">
                    <div class="w-50">
                      <v-select label="Klasa" v-model="editedChildren[child.id]._class"
                      :items="[{ id: null, className: 'Brak klasy' }, ...parentClasses]" item-title="className"
                      item-value="id"></v-select> 
                    </div>
                      <div class="w-50">
                        <v-date-input prepend-icon="" v-model="editedChildren[child.id].dob"
                      label="Data urodzenia" :rules="[rules.required, rules.date, rules.notFutureDate]"
                      placeholder="DD.MM.RRRR" required></v-date-input>
                      </div>
                  </div>
                </v-card-text>
                <v-card-actions class="pt-0 justify-center">
                  <v-btn color="green" text="Zapisz" variant="outlined"
                    @click="editChild(child.id); toggleEdit(child.id)"
                    :disabled="!isEditFormValid[child.id]">Zapisz</v-btn>
                  <v-btn color="red" text="Anuluj" variant="outlined" @click="toggleEdit(child.id)">Anuluj</v-btn>
                </v-card-actions>
              </v-form>
            </v-card>
          </v-expand-transition>

          <v-expand-transition>
            <v-card v-if="showDeleteChild[child.id]"
              class="position-absolute w-100 text-center justify-center align-center d-flex flex-column" height="100%"
              style="bottom: 0;" title="Czy na pewno chcesz usunąć?">
              <v-card-actions class="pt-0 ">
                <v-btn color="green" text="Tak" variant="outlined"
                  @click="deleteChild(child.id); toggleDelete(child.id)"></v-btn>
                <v-btn color="red" text="Nie" variant="outlined" @click="toggleDelete(child.id)"></v-btn>
              </v-card-actions>
            </v-card>
          </v-expand-transition>

        </v-card>
      </v-col>
      <v-col cols="12" md="4">

        <v-dialog max-width="500">
          <template v-slot:activator="{ props }">
            <v-hover>
              <template v-slot:default="{ isHovering }">
                <v-card variant="tonal" elevation="8"
                  class="text-center grey d-flex flex-column align-center justify-center cursor-pointer" v-bind="props"
                  :color="isHovering ? 'hover' : undefined" @click="showAddCard = true">
                  <div>
                    <v-icon size="10rem">mdi-plus-circle</v-icon>
                    <h3 class="mt-2">Dodaj dziecko</h3>
                  </div>
                </v-card>
              </template>
            </v-hover>
          </template>

          <template v-slot:default="{ isActive }">
            <v-card class="text-center" title="Dodaj dziecko">
              <v-form v-model="isAddFormValid" ref="form">
                <v-card-text>
                  <v-text-field v-model="childData.firstName" label="Imię" :rules="[rules.required]"
                    required></v-text-field>

                  <v-text-field v-model="childData.lastName" label="Nazwisko" :rules="[rules.required]"
                    required></v-text-field>

                  <v-select label="Klasa" v-model="childData._class"
                    :items="[{ id: null, className: 'Brak klasy' }, ...parentClasses]" item-title="className"
                    item-value="id"></v-select>

                  <v-date-input v-model="childData.dob" label="Data urodzenia"
                    :rules="[rules.required, rules.date, rules.notFutureDate]" placeholder="DD.MM.RRRR"
                    required></v-date-input>
                </v-card-text>

                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn text="Dodaj" color="green" @click="addChild(); isActive.value = false"
                    :disabled="!isAddFormValid" />
                  <v-btn text="Anuluj" @click="isActive.value = false" />
                </v-card-actions>
              </v-form>
            </v-card>
          </template>
        </v-dialog>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
const { snackbarSuccess, snackbarError } = useSnack();
const childStore = useChildStore();
const authStore = useAuthStore();
const classStore = useClassStore();
const childrenList = ref([]);
const childrenCount = ref();
const parentClasses = ref([]);
onMounted(async () => {
  if (childStore.childrenList.length === 0) {
    await childStore.fetchChildren();
  }
  childrenList.value = childStore.childrenList;
  childrenCount.value = childStore.childrenList.length;
  await classStore.fetchClassesByParent();
  parentClasses.value = classStore.classList;
});

const getClassNameForChild = (child) => {
  const foundClass = parentClasses.value.find(parentClass => parentClass.id === child._class);
  return foundClass ? foundClass.className : null;
};

const showEditChild = reactive({});

const editedChildren = reactive({});

const isEditFormValid = reactive({});

const toggleEdit = (childId) => {
  if (!showEditChild[childId]) {
    showEditChild[childId] = true;
    const child = childrenList.value.find(child => child.id === childId);
    if (child) {
      if (!editedChildren[childId]) {
        editedChildren[childId] = {
          id: child.id,
          firstName: child.firstName,
          lastName: child.lastName,
          _class: child._class,
          dob: new Date(child.dob)
        };
      }
    }
  } else {
    showEditChild[childId] = !showEditChild[childId];
  }
};

const editChild = async (childId) => {
  try {
    const editedChild = editedChildren[childId];
    if (editedChild) {
      editedChild.dob = formatDateWithoutTimezone(editedChild.dob);
      await childStore.editChild(editedChild);
      snackbarSuccess(`Pomyślnie edytowano dziecko o ID: ${childId}.`);
      childrenList.value = childStore.childrenList;
    }
  } catch (error) {
    snackbarError(`Wystąpił błąd podczas edytowania dziecka o ID: ${childId}.`);
  }
};

const showDeleteChild = reactive({});
const toggleDelete = (childId) => {
  if (showDeleteChild[childId] === undefined) {
    showDeleteChild[childId] = true;
  } else {
    showDeleteChild[childId] = !showDeleteChild[childId];
  }
};
const deleteChild = async (childId) => {
  try {
    await childStore.deleteChild(childId);
    snackbarSuccess('Pomyślnie usunięto dziecko.');
    childrenList.value = childStore.childrenList;
    childrenCount.value = childStore.childrenList.length;
  } catch (error) {
    snackbarError('Wystąpił błąd podczas usuwania dziecka.');
  }
};
const childData = reactive({
  firstName: '',
  lastName: '',
  _class: '',
  dob: null
});
watch(() => childData.firstName, (firstName) => {
  childData.firstName = firstName.charAt(0).toUpperCase() + firstName.slice(1);
})

watch(() => childData.lastName, (newlastName) => {
  childData.lastName = newlastName.charAt(0).toUpperCase() + newlastName.slice(1);
})
const isAddFormValid = ref(false);
const today = new Date();
const rules = {
  required: value => !!value || 'To pole jest wymagane',
  date: value => /^\d{2}\.\d{2}\.\d{4}$/.test(value) || 'Niepoprawny format daty (DD.MM.RRRR)',
  notFutureDate: value => {
    const [day, month, year] = value.split('.');
    const enteredDate = new Date(`${year}-${month}-${day}`);
    return enteredDate <= today || 'Data nie może być z przyszłości';
  }
};


const addChild = async () => {
  try {
    childData.dob = formatDateWithoutTimezone(new Date(childData.dob));
    await childStore.addChild(childData)
    snackbarSuccess('Pomyślnie dodano dziecko.');
    childrenList.value = childStore.childrenList;
    childrenCount.value = childStore.childrenList.length;
  } catch (error) {
    snackbarError('Wystąpił błąd podczas dodawania dziecka.');
  }
};

const formatDateWithoutTimezone = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
</script>

<style scoped>
.v-card {
  min-height: 300px;
}
</style>