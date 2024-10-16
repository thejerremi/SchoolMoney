import http from "../http-common";

class reportService {
    fetchAllTransactionsFromFundraiser(fundraiserId){
        return http.get(`/reports/fundraiser/${fundraiserId}`, {
            responseType: 'blob',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchParentTransactionsFromFundraiser(fundraiserId, parentId){
        return http.get(`/reports/fundraiser/${fundraiserId}/parent/${parentId}`, {
            responseType: 'blob',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchParentTransactionsFromClassFundraisers(classId, parentId){
        return http.get(`/reports/class/${classId}/fundraisers/parent/${parentId}`, {
            responseType: 'blob',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchAllTransactionsFromClassFundraisers(classId){
        return http.get(`/reports/class/${classId}/fundraisers`, {
            responseType: 'blob',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchAllParentTransactions(parentId){
        return http.get(`/reports/parent/${parentId}`, {
            responseType: 'blob',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
}

export default new reportService();
