import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.connected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
    this.onConnectedCallbacks = [];
    this.onDisconnectedCallbacks = [];
    this.onErrorCallbacks = [];
  }

  connect(token) {
    if (this.client?.connected) {
      console.log('WebSocket already connected');
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS(WS_URL),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log('[STOMP Debug]', str);
          }
        },
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket connected');
          this.connected = true;
          this.reconnectAttempts = 0;
          this.onConnectedCallbacks.forEach((cb) => cb());
          resolve();
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected');
          this.connected = false;
          this.onDisconnectedCallbacks.forEach((cb) => cb());
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame.headers?.message || frame);
          this.onErrorCallbacks.forEach((cb) => cb(frame));
          reject(new Error(frame.headers?.message || 'STOMP connection error'));
        },
        onWebSocketError: (event) => {
          console.error('WebSocket error:', event);
          this.reconnectAttempts++;
          if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            this.disconnect();
            reject(new Error('Max reconnection attempts reached'));
          }
        },
      });

      this.client.activate();
    });
  }

  disconnect() {
    if (this.client) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();
      this.client.deactivate();
      this.client = null;
      this.connected = false;
    }
  }

  subscribe(destination, callback) {
    if (!this.client?.connected) {
      console.warn('WebSocket not connected, cannot subscribe to:', destination);
      return null;
    }

    // Check if already subscribed
    if (this.subscriptions.has(destination)) {
      console.log('Already subscribed to:', destination);
      return destination;
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
        callback(message.body);
      }
    });

    this.subscriptions.set(destination, subscription);
    console.log('Subscribed to:', destination);
    return destination;
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
      console.log('Unsubscribed from:', destination);
    }
  }

  send(destination, body) {
    if (!this.client?.connected) {
      console.warn('WebSocket not connected, cannot send to:', destination);
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });
  }

  isConnected() {
    return this.connected && this.client?.connected;
  }

  onConnected(callback) {
    this.onConnectedCallbacks.push(callback);
  }

  onDisconnected(callback) {
    this.onDisconnectedCallbacks.push(callback);
  }

  onError(callback) {
    this.onErrorCallbacks.push(callback);
  }

  clearCallbacks() {
    this.onConnectedCallbacks = [];
    this.onDisconnectedCallbacks = [];
    this.onErrorCallbacks = [];
  }
}

// Singleton instance
const websocketService = new WebSocketService();

export default websocketService;
