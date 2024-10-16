import { defineStore } from 'pinia'
import adminService from '@/services/adminService';

export const useAdminStore = defineStore({
  id: 'AdminStore',
  state: () => ({ 
    usersList: [],
    classesList: [],
    fundraisersList: [],
  }),
  actions: {
    async fetchAllUsers(){
      const response = await adminService.fetchAllUsers();
      this.usersList = response.data;
    },
    async lockUser(id){
      return await adminService.lockUser(id);
    },
    async unlockUser(id){
      return await adminService.unlockUser(id);
    },
    async fetchAllClasses(){
      const response = await adminService.fetchAllClasses();
      this.classesList = response.data;
    },
    async fetchAllFundraisers(){
      const response = await adminService.fetchAllFundraisers();
      this.fundraisersList = response.data;
    },
    async lockFundraiser(id){
      return await adminService.lockFundraiser(id);
    }
  }
})
