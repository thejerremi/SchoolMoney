<template>
  <div class="mt-6">
    <v-form @submit.prevent="handleLogin">
      <v-text-field
        v-model="loginData.email"
        class="mx-auto"
        label="Email"
        variant="outlined"
        type="email"
        :rules="emailRules"
      />
      <v-text-field
        v-model="loginData.password"
        class="mx-auto"
        label="Hasło"
        variant="outlined"
        type="password"
        :rules="passwordRules"
      />
      <div class="text-center">
        <v-btn type="submit">Zaloguj</v-btn>
      </div>
    </v-form>
  </div>
</template>

<script setup>
const { snackbarSuccess, snackbarError } = useSnack();
const authStore = useAuthStore();
const emailRules = [
  (v) => !!v || "Email jest wymagany",
  (v) => /.+@.+\..+/.test(v) || "Email musi być poprawny",
];
const passwordRules = [(v) => !!v || "Hasło jest wymagane"];

const loginData = reactive({
  email: "",
  password: "",
});

const handleLogin = async () => {
  try {
    await authStore.loginUser({
      email: loginData.email,
      password: loginData.password,
    });
  } catch (error) {
    // Sprawdzenie kodu statusu HTTP
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // Niepoprawne dane logowania
          snackbarError(
            "Nie udało się zalogować. Niepoprawny email lub hasło."
          );
          break;
        case 423:
          // Konto zablokowane
          snackbarError(
            "Twoje konto zostało zablokowane. Skontaktuj się z administratorem."
          );
          break;
        case 500:
        default:
          // Ogólny błąd serwera
          snackbarError("Wystąpił błąd serwera. Spróbuj ponownie później.");
          break;
      }
    } else {
      // Błąd bez odpowiedzi z serwera (np. brak połączenia)
      snackbarError(
        "Brak odpowiedzi z serwera. Sprawdź połączenie z internetem i spróbuj ponownie."
      );
    }
  }
};
</script>

<style scoped>
.v-text-field {
  max-width: 66%;
}
</style>
