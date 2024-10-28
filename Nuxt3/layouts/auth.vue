<template>
  <v-app>
    <v-main>
      <slot />
      <NuxtSnackbar />
    </v-main>
  </v-app>
</template>

<script setup>
const authStore = useAuthStore();
watch(
  () => authStore.user,
  (user) => {
    if (user) {
      navigateTo("/user");
    }
  }
);
</script>

<style scoped></style>
