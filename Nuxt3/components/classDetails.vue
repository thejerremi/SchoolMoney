<template>
  <div class="d-flex fill-height">
    <div class="w-50">
      <h1 class="text-center font-weight-bold">{{ classData.className }}</h1>
      <v-list lines="one">
        <v-list-item title="Liczba dzieci" :subtitle="classData.children.length" />
        <v-list-item title="Liczba rodziców" :subtitle="classData.parents.length" />
        <v-list-item title="Skarbnik" :subtitle="classData.treasurer.firstname + ' ' + classData.treasurer.lastname" />
      </v-list>
    </div>
    <div class="w-50">
      <ClassChat />
    </div>
  </div>
</template>

<script setup>
defineProps({
  classData: {
    type: Object,
    required: true
  }
});
</script>

<style></style>