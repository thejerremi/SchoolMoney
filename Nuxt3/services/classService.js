import http from "../http-common";

class classService {
    fetchClassById(id){
        return http.get(`/class?id=${id}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchClassByParent(){
        return http.get(`/class/by-parent`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    addClass(data){
        return http.post(`/class/add`, data, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    addParent(parentId, classId){
        return http.post(`/class/add-parent?parentId=${parentId}&classId=${classId}`, {}, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    editClass(data){
        return http.patch(`/class/edit`, data, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    updateClassName(data){
        return http.patch(`/class/update-name`, data, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    updateClassTreasurer(data){
        return http.patch(`/class/update-treasurer`, data, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    deleteParent(parentId, classId){
        return http.delete(`/class/delete-parent?parentId=${parentId}&classId=${classId}`, {
            headers: {   
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    deleteClass(id){
        return http.delete(`/class/delete?id=${id}`, {
            headers: {   
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
}

export default new classService();
