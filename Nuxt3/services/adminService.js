import http from "../http-common";

class adminService {
  fetchAllUsers(){
    return http.get(`/admin/users`, {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
  }
  lockUser(id){
    return http.post(`/admin/lock-user/${id}`, {}, {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
  }
  unlockUser(id){
    return http.post(`/admin/unlock-user/${id}`, {}, {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
  }
  fetchAllClasses(){
    return http.get(`/admin/classes`, {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
  }
  fetchAllFundraisers(){
    return http.get(`/admin/fundraisers`, {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
  }
  lockFundraiser(id){
    return http.post(`/admin/lock-fundraiser/${id}`, {}, {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
  }
}

export default new adminService();
