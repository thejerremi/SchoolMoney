<template>
  <v-container class="d-flex flex-column fill-height">

<v-card variant="outlined" elevation="16" height="100%" width="100%" v-if="selectedFundraiser">
  <v-tabs v-model="selectedTab">
        <v-tab value="details">Szczegóły</v-tab>
        <v-tab value="history">Historia</v-tab>
        <v-tab value="report">Raporty</v-tab>
        <v-tab value="settings" v-if="(authStore.user.id === selectedFundraiser.author.id && selectedFundraiser.status !== 'CANCELLED' && selectedFundraiser.status !== 'BLOCKED')
          || authStore.user.role === 'ADMIN'
        ">Ustawienia</v-tab>
      </v-tabs>

      <v-card-text>
        <v-tabs-window v-model="selectedTab">
          <v-tabs-window-item value="details">
            <FundraiserDetails :selectedFundraiser="selectedFundraiser" @refresh-fundraiser="refreshFundraiserDetails"/>
          </v-tabs-window-item>

          <v-tabs-window-item value="history">
            <FundraiserHistory :selectedFundraiser="selectedFundraiser" />
          </v-tabs-window-item>

          <v-tabs-window-item value="report">
            <FundraiserReport />
          </v-tabs-window-item>

          <v-tabs-window-item value="settings">
            <FundraiserSettings :selectedFundraiser="selectedFundraiser" @refresh-fundraiser="refreshFundraiserDetails" @change-tab="changeTabToDetails"/>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
</v-card>
</v-container>
</template>

<script setup>
const route = useRoute();
const authStore = useAuthStore();
const fundraiserStore = useFundraiserStore();
const selectedFundraiser = ref(null);
const selectedTab = ref(0);
const refreshFundraiserDetails = async () => {
  await fundraiserStore.fetchFundraiserDetails(route.params.id);
  selectedFundraiser.value = fundraiserStore.selectedFundraiser;
};
onMounted(async () => {
  await refreshFundraiserDetails();
})

const changeTabToDetails = () => {
  selectedTab.value = 0;
}

</script>

<style scoped>

</style>