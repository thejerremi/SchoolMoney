<template>
  <v-container class="text-center grey d-flex flex-column align-center justify-space-evenly
 h-100">
    <div class="text-h2 font-weight-bold">SchoolMoney</div>
    <div class="text-h4 mt-5 text-center">Jedyna potrzebna aplikacja do zarządzania zbiórkami w ramach szkolnych klas.
    </div>
    <div class="text-h4 mt-3 font-weight-bold">Rodzicu, załóż konto już teraz!</div>
    <div class="mt-5" v-if="!authStore.user">
      <v-btn class="mr-4" color="black" to="/auth" variant="outlined">Zaloguj się</v-btn>
      <v-btn color="black" to="/auth?openAccount=true">Otwórz konto</v-btn>
    </div>
    <div class="mt-5" v-else>
      <v-btn color="black" to="/user" variant="outlined">Przejdź do pulpitu</v-btn>
    </div>
    <v-timeline direction="horizontal">
      <v-timeline-item>
        <div>
          <div class="text-h6 font-weight-bold">Twórz klasy</div>
          <p>
            Załóż klasę dla swojego dziecka i zaproś innych rodziców do wspólnej zbiórki.
          </p>
        </div>
      </v-timeline-item>

      <v-timeline-item>
        <div>
          <div class="text-h6 font-weight-bold">Otwieraj zbiórki</div>
          <p>
            Otwórz zbiórkę na dowolny cel i zbieraj pieniądze od innych rodziców.
          </p>
        </div>
      </v-timeline-item>

      <v-timeline-item>
        <div>
          <div class="text-h6 font-weight-bold">Bezpieczeństwo na Pierwszym Miejscu</div>
          <p>
            Twoje bezpieczeństwo jest dla nas priorytetem.<br> Korzystamy z najnowocześniejszych technologii, aby
            chronić
            Twoje dane i pieniądze.
          </p>
        </div>
      </v-timeline-item>
    </v-timeline>
  </v-container>
</template>

<script setup>
import { useAuthStore } from "~/stores/AuthStore";
const authStore = useAuthStore();
</script>

<style scoped></style>