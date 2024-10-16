import { defineStore } from 'pinia'
import childService from '@/services/childService';

export const useChildStore = defineStore('childStore', {
  state: () => ({ 
    childrenList: [],
    childrenCount: 0,
    childrenListInClass: []
  }),
  actions: {
    async fetchChildren(){
      const response = await childService.fetchChildren();
      this.childrenList = response.data;
      this.childrenCount = response.data.length;
    },
    async fetchChildrenByClass(classId){
      const response = await childService.fetchChildrenByClass(classId);
      this.childrenListInClass = response.data;
    },
    async addChild(data){
      await childService.addChild(data);
      await this.fetchChildren();
    },
    async editChild(data){
      await childService.editChild(data);
      await this.fetchChildren();
    },
    async deleteChild(id){
      await childService.deleteChild(id);
      await this.fetchChildren();
    },
    async deleteChildFromClass(childId){
      await childService.deleteChildFromClass(childId);
    }
  }
})
