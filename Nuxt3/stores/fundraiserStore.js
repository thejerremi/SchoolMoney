import { defineStore } from 'pinia';
import fundraiserService from '@/services/fundraiserService';

export const useFundraiserStore = defineStore('fundraiserStore', {
  state: () => ({
    selectedFundraiser: null,
    classFundraisers: [],
    classFundraisersCount: 0
  }),
  actions: {
    // Pobierz wszystkie zbiórki dla danej klasy
    async fetchFundraiserByClassId(id){
      const response = await fundraiserService.fetchFundraiserByClassId(id);
      this.classFundraisers = response.data;
      this.classFundraisersCount = response.data.length;
    },

    // Utwórz nową zbiórkę
    async createFundraiser(formData){
      const response = await fundraiserService.createFundraiser(formData);
    },

    // Zaktualizuj istniejącą zbiórkę
    async updateFundraiser(id, formData){
      const response = await fundraiserService.updateFundraiser(id, formData);
      this.selectedFundraiser = response.data;
      // Zaktualizuj zbiórkę w liście zbiórek
      const index = this.classFundraisers.findIndex(fundraiser => fundraiser.id === id);
      if (index !== -1) {
        this.classFundraisers.splice(index, 1, response.data);
      }
    },

    // Usuń zbiórkę
    async deleteFundraiser(id){
      await fundraiserService.deleteFundraiser(id);
      // Usuń zbiórkę z listy
      this.classFundraisers = this.classFundraisers.filter(fundraiser => fundraiser.id !== id);
      this.classFundraisersCount -= 1;
    },

    // Pobierz szczegóły wybranej zbiórki
    async fetchFundraiserDetails(id){
      const response = await fundraiserService.fetchFundraiserDetails(id);
      this.selectedFundraiser = response.data;
    },

    // Wpłać środki na zbiórkę
    async depositForFundraiser(transaction){
      const response = await fundraiserService.depositForFundraiser(transaction);
      return response;
    },
    async withdrawFromFundraiser(amount, fundraiserId){
      const response = await fundraiserService.withdrawFromFundraiser(amount, fundraiserId);
      return response;
    },
    async fetchFundraiserHistory(fundraiserId){
      const response = await fundraiserService.fetchFundraiserHistory(fundraiserId);
      return response.data;
    },
    async updateFundraiserOwner(fundraiserId, newOwnerId){
      await fundraiserService.updateFundraiserOwner(fundraiserId, newOwnerId);
    },
    async cancelFundraiser(fundraiserId){
      await fundraiserService.cancelFundraiser(fundraiserId);
    }
  }
});
