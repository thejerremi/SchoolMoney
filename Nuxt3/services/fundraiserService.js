import http from "../http-common";

class fundraiserService {
    fetchFundraiserByClassId(id){
        return http.get(`/fundraisers/class/${id}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }

    // Utwórz nową zbiórkę
    createFundraiser(formData){
        return http.post('/fundraisers/create', formData, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'multipart/form-data'
            }
        });
    }

    // Zaktualizuj istniejącą zbiórkę
    updateFundraiser(id, formData){
        return http.patch(`/fundraisers/update/${id}`, formData, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'multipart/form-data'
            }
        });
    }

    // Usuń zbiórkę
    deleteFundraiser(id){
        return http.delete(`/fundraisers/delete/${id}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }

    // Pobierz szczegóły zbiórki
    fetchFundraiserDetails(id){
        return http.get(`/fundraisers/details/${id}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }

    // Wpłać środki na zbiórkę
    depositForFundraiser(transaction){
        return http.post(`/transactions/fundraiser/deposit`, transaction, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    withdrawFromFundraiser(amount, fundraiserId){
        return http.post(`/transactions/fundraiser/withdraw?amount=${amount}&fundraiserId=${fundraiserId}`, {}, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchFundraiserHistory(fundraiserId){
        return http.get(`/transactions/fundraiser/history?fundraiserId=${fundraiserId}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    updateFundraiserOwner(fundraiserId, newOwnerId){
        return http.patch(`/fundraisers/update-owner/${fundraiserId}?newOwnerId=${newOwnerId}`, {},{
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    cancelFundraiser(fundraiserId){
        return http.post(`/fundraisers/cancel?fundraiserId=${fundraiserId}`, {},{
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
}

export default new fundraiserService();
