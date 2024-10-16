import http from "../http-common";

class childService {
    fetchChildren(){
        return http.get(`/child`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchChildrenByClass(classId){
        return http.get(`/child/by-class/${classId}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    addChild(data){
        return http.post(`/child/add`, data, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    editChild(data){
        return http.patch(`/child/edit`, data, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    deleteChild(id){
        return http.delete(`/child/delete?id=${id}`, {
            headers: {   
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    deleteChildFromClass(childId){
        return http.patch(`/child/remove-class?childId=${childId}`, {}, {
            headers: {   
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
}

export default new childService();
