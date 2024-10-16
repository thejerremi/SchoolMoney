<template>
  <div style="position:relative">
    <div class="search-bar bg-surface">
      <v-autocomplete v-model="selectedRecipient" :items="recipients" :loading="loading" v-model:search="search"
        :hide-no-data="true" label="Wyszukaj osobę" :item-title="getRecipientFullName" item-value="username"
        hide-details clearable></v-autocomplete>
    </div>
    <!-- Lista wiadomości -->
    <div>
      <div class="messages d-flex flex-column flex-grow-1">
        <ul>
          <li v-for="(message, index) in messages" :key="index">{{ message.senderName || 'Ty' }}: {{ message.content }}
          </li>
        </ul>
      </div>
    </div>


    <!-- Pole do wpisywania wiadomości -->
    <div class="message-input ma-2 input-bar bg-surface">
      <input v-model="newMessage" @keyup.enter="sendMessage" :disabled="!selectedRecipient"
        placeholder="Napisz wiadomość" />
      <button @click="sendMessage">Wyślij</button>
    </div>
  </div>
</template>

<script setup>
import chatService from '@/services/chatService';
import { Client } from '@stomp/stompjs';

const authStore = useAuthStore();
const messages = ref([]);
const newMessage = ref('');
let stompClient = null;
const selectedRecipient = ref(null);
const selectedRecipientId = ref(null);
const recipients = ref([]);
const loading = ref(false);
const search = ref('');

// Pobranie wiadomości z serwera
const fetchMessages = async () => {
  try {
    if (selectedRecipientId.value) {
      const response = await chatService.fetchPrivateMessages(authStore.user.id, selectedRecipientId.value);
      messages.value = response.data;
      console.log(messages.value);
    }
  } catch (error) {
    console.error("Error fetching messages:", error);
  }
};

// Watcher do filtrowania wiadomości
const filterMessages = () => {
  messages.value = messages.value.filter(message =>
    (message.senderId === selectedRecipientId.value && message.recipientId === authStore.user.id)
    || (message.senderId === authStore.user.id && message.recipientId === selectedRecipientId.value)
  );
};

// Watcher na zmienną `search` do wyszukiwania odbiorców
watch(search, (newValue) => {
  if (newValue && !selectedRecipient.value) {
    fetchRecipients(newValue);
  }
});
watch(selectedRecipient, () => {
  fetchMessages(); // Pobranie wiadomości z serwera
  if (stompClient)
    stompClient.deactivate();
  connect();
})


// Pobranie listy potencjalnych odbiorców
const fetchRecipients = async (searchInput) => {
  if (!searchInput) return;
  loading.value = true;
  try {
    const data = await authStore.searchParents(searchInput);
    recipients.value = data;
  } catch (error) {
    console.error("Błąd podczas pobierania listy:", error);
  } finally {
    loading.value = false;
  }
};

// Funkcja do uzyskania pełnego imienia i nazwiska odbiorcy
const getRecipientFullName = (recipient) => {
  selectedRecipientId.value = recipient.id;
  return `${recipient.firstname} ${recipient.lastname} - ${recipient.username}`;
};
// Inicjalizacja połączenia WebSocket
const connect = () => {
  console.log('Logged as: ' + authStore.user.username)
  stompClient = new Client({
    brokerURL: 'ws://localhost:8080/chat?id=' + authStore.user.username,
    reconnectDelay: 5000,
    onConnect: (frame) => {
      console.log('Connected: ' + frame);

      // Subscribe to private messages
      stompClient.subscribe(`/user/topic/private-messages`, (message) => {
        const receivedMessage = JSON.parse(message.body);
        console.log(receivedMessage);
        messages.value.push(receivedMessage);
        filterMessages();
      });
    },

    onStompError: (frame) => {
      console.error('Broker error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    },
  });

  stompClient.activate();
};

// Wysyłanie prywatnej wiadomości do wybranego odbiorcy
const sendMessage = () => {
  if (newMessage.value.trim() && selectedRecipient.value !== -1) {
    const messageContent = {
      recipientUsername: selectedRecipient.value,
      content: newMessage.value,
    };
    stompClient.publish({
      destination: '/ws/private-message', // Prywatna wiadomość
      body: JSON.stringify(messageContent),
    });
    messageContent.senderId = authStore.user.id;
    messageContent.recipientId = selectedRecipientId.value;
    messages.value.push(messageContent);
    newMessage.value = ''; // Wyczyść pole
  }
};

onMounted(() => {
  connect(); // Inicjalizacja WebSocket
});

onBeforeUnmount(() => {
  if (stompClient !== null) {
    stompClient.deactivate();
  }
});
</script>

<style scoped>
body{
  overflow: hidden;
}

.search-bar {
  position: sticky;
  top: 64px;
}

.input-bar {
  position: fixed;
  bottom: 0;
}


.messages {
  height: 80vh;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  margin-bottom: 66px;
}

.message-input {
  display: flex;
  align-items: center;
  width: 99%;
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
