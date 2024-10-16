import http from "../http-common";

class chatService {
    fetchMessages(classId){
        return http.get(`/chat/group/${classId}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
    fetchPrivateMessages(senderId, recipientId){
        return http.get(`/chat/private/${senderId}/${recipientId}`, {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        });
    }
}

export default new chatService();
