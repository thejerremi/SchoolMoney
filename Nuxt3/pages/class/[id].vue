<template>
  <v-container class="d-flex flex-column fill-height">

    <v-card variant="outlined" elevation="16" height="100%" width="100%" style="position: relative">
      <v-tabs v-model="selectedTab">
        <v-tab value="details">Szczegóły</v-tab>
        <v-tab value="parents">Rodzice</v-tab>
        <v-tab value="children">Dzieci</v-tab>
        <v-tab value="fundraiser">Zbiórki</v-tab>
        <v-tab value="report">Raporty</v-tab>
        <v-tab value="settings">Ustawienia</v-tab>
      </v-tabs>

      <v-card-text class="fill-height">
        <v-tabs-window v-model="selectedTab" class="fill-height">
          <v-tabs-window-item value="details" class="fill-height">
            <ClassDetails v-if="_class" :classData="_class" />
          </v-tabs-window-item>

          <v-tabs-window-item value="parents">
            <ClassParents :classData="_class" :isTreasurer="isTreasurer"/>
          </v-tabs-window-item>

          <v-tabs-window-item value="children">
            <ClassChildren :classData="_class" :isTreasurer="isTreasurer" />
          </v-tabs-window-item>
          <v-tabs-window-item value="fundraiser">
            <ClassFundraiser />
          </v-tabs-window-item>
          <v-tabs-window-item value="report">
            <ClassReport />
          </v-tabs-window-item>
          <v-tabs-window-item value="settings">
            <ClassSetings v-if="_class" :classData="_class" :isTreasurer="isTreasurer" @loadClass="loadClass"/>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>

const authStore = useAuthStore();
const classStore = useClassStore();
const route = useRoute();
const classId = route.params.id;
const _class = ref(null);
const isTreasurer = ref(false);

const loadClass = async () => {
  if (classStore.selectedClass === null || classStore.selectedClass.id !== classId) {
    await classStore.fetchClassById(classId);
  }
  _class.value = classStore.selectedClass;
  isTreasurer.value = _class.value.treasurer.id === authStore.user.id
}
onMounted(async () => {
  await loadClass();
});


const selectedTab = ref();

</script>

<style>
</style>