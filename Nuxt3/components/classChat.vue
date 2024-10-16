<template>
  <div class="chat-container">
    <div class="messages">
      <ul>
        <li v-for="(message, index) in messages" :key="index">{{ message.senderName }}: {{ message.content }}</li>
      </ul>
    </div>
    <div class="message-input">
      <input v-model="newMessage" @keyup.enter="sendMessage" placeholder="Napisz wiadomość do innych rodziców" />
      <button @click="sendMessage">Wyślij</button>
    </div>
  </div>
</template>

<script setup>
import chatService from '@/services/chatService';
import { Client } from '@stomp/stompjs';
const authStore = useAuthStore();
const classStore = useClassStore();
const route = useRoute();

const messages = ref([]);
const newMessage = ref('');
const classId = route.params.id
let stompClient = null;

// Fetch messages from the server on component mount
const fetchMessages = async () => {
  try {
    const response = await chatService.fetchMessages(classId);
    messages.value = response.data;
  } catch (error) {
    console.error("Error fetching messages:", error);
  }
};

// Initialize WebSocket connection
const connect = () => {
  stompClient = new Client({
    brokerURL: 'ws://localhost:8080/chat?id=' + authStore.user.username,
    reconnectDelay: 5000,

    onConnect: (frame) => {
      console.log('Connected: ' + frame);

      // Subscribe to group chat
      stompClient.subscribe(`/topic/group/${classId}`, (message) => {
        const receivedMessage = JSON.parse(message.body);
        messages.value.push(receivedMessage);
      });
    },

    onStompError: (frame) => {
      console.error('Broker error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    },
  });

  stompClient.activate();
};

// Send message to the group
const sendMessage = () => {
  if (newMessage.value.trim()) {
    const messageContent = {
      senderId: authStore.user.id,
      senderName: `${authStore.user.firstname} ${authStore.user.lastname}`,
      content: newMessage.value,
      classId: classId,
    };

    stompClient.publish({
      destination: '/ws/message',
      body: JSON.stringify(messageContent),
    });

    newMessage.value = ''; // Clear input
  }
};

onMounted(() => {
  fetchMessages(); // Fetch messages from the server
  connect(); // Connect WebSocket
});

onBeforeUnmount(() => {
  if (stompClient !== null) {
    stompClient.deactivate();
  }
});
</script>


<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  min-height: 95%;
  max-height: 100%;
}
.messages {
  flex-grow: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  margin-bottom: 10px;
}
.message-input {
  display: flex;
  align-items: center;
}
.message-input input {
  flex-grow: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.message-input button {
  padding: 10px;
  margin-left: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.message-input button:hover {
  background-color: #45a049;
}
</style>
