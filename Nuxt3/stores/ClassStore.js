import { defineStore } from 'pinia'
import classService from '@/services/classService';

export const useClassStore = defineStore('classStore', {
  state: () => ({ 
    selectedClass: null,
    classList: [],
    classCount: 0
  }),
  actions: {
    async fetchClassById(id){
      const response = await classService.fetchClassById(id);
      this.selectedClass = response.data;
      this.classCount = response.data.length;
    },
    async fetchClassesByParent(){
      const response = await classService.fetchClassByParent();
      this.classList = response.data;
      this.classCount = response.data.length;
    },
    async addClass(data){
      await classService.addClass(data);
      await this.fetchClassesByParent();
    },
    async addParent(parentId, classId){
      const response = await classService.addParent(parentId, classId);
      return response;
    },
    async editClass(data){
      await classService.editClass(data);
      await this.fetchClass();
    },
    async updateClassName(data){
      await classService.updateClassName(data);
      await this.fetchClassesByParent();
    },
    async updateClassTreasurer(data){
      await classService.updateClassTreasurer(data);
      await this.fetchClassesByParent();
    },
    async deleteParent(parentId, classId){
      await classService.deleteParent(parentId, classId);
      await this.fetchClassesByParent();
    },
    async deleteClass(id){
      await classService.deleteClass(id);
      this.selectedClass = null;
      await this.fetchClassesByParent();
    }
  }
})
