import { createContext, useContext, useEffect, useState, useCallback, useRef } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import websocketService from '../services/websocket';
import {
  taskCreated,
  taskUpdated,
  taskDeleted,
  commentAdded,
  commentDeleted,
} from '../store/slices/taskSlice';
import {
  projectUpdated,
  projectDeleted,
  memberAdded,
  memberRemoved,
} from '../store/slices/projectSlice';

const WebSocketContext = createContext(null);

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};

export const WebSocketProvider = ({ children }) => {
  const dispatch = useDispatch();
  const { token, user } = useSelector((state) => state.auth);
  const [connected, setConnected] = useState(false);
  const [onlineUsers, setOnlineUsers] = useState({});
  const currentProjectId = useRef(null);

  // Connect to WebSocket when authenticated
  useEffect(() => {
    if (token && user) {
      websocketService
        .connect(token)
        .then(() => {
          setConnected(true);
        })
        .catch((error) => {
          console.error('Failed to connect to WebSocket:', error);
          setConnected(false);
        });

      websocketService.onConnected(() => setConnected(true));
      websocketService.onDisconnected(() => setConnected(false));

      return () => {
        websocketService.disconnect();
        setConnected(false);
      };
    }
  }, [token, user]);

  // Handle WebSocket messages
  const handleProjectMessage = useCallback(
    (message) => {
      const { type, payload, userId } = message;

      // Skip messages from self (we already have optimistic updates)
      if (userId === user?.id) {
        return;
      }

      console.log('Received WebSocket message:', type, payload);

      switch (type) {
        case 'TASK_CREATED':
          dispatch(taskCreated(payload));
          break;
        case 'TASK_UPDATED':
        case 'TASK_STATUS_CHANGED':
        case 'TASK_ASSIGNED':
          dispatch(taskUpdated(payload));
          break;
        case 'TASK_DELETED':
          dispatch(taskDeleted({ projectId: payload.projectId, taskId: payload.id }));
          break;
        case 'COMMENT_ADDED':
          dispatch(commentAdded({ taskId: payload.taskId, comment: payload }));
          break;
        case 'COMMENT_DELETED':
          dispatch(commentDeleted({ taskId: payload.taskId, commentId: payload.commentId }));
          break;
        case 'PROJECT_UPDATED':
          dispatch(projectUpdated(payload));
          break;
        case 'PROJECT_DELETED':
          dispatch(projectDeleted(payload.id));
          break;
        case 'MEMBER_ADDED':
          dispatch(memberAdded({ projectId: payload.projectId, member: payload }));
          break;
        case 'MEMBER_REMOVED':
          dispatch(memberRemoved({ projectId: payload.projectId, userId: payload.userId }));
          break;
        default:
          console.log('Unknown WebSocket message type:', type);
      }
    },
    [dispatch, user?.id]
  );

  // Handle presence messages
  const handlePresenceMessage = useCallback(
    (message) => {
      const { type, userId, username, projectId, onlineUsers: users } = message;

      if (type === 'USER_JOINED') {
        setOnlineUsers((prev) => ({
          ...prev,
          [projectId]: [...(prev[projectId] || []).filter((u) => u.userId !== userId), { userId, username }],
        }));
      } else if (type === 'USER_LEFT') {
        setOnlineUsers((prev) => ({
          ...prev,
          [projectId]: (prev[projectId] || []).filter((u) => u.userId !== userId),
        }));
      } else if (type === 'ONLINE_USERS' && users) {
        setOnlineUsers((prev) => ({
          ...prev,
          [projectId]: users,
        }));
      }
    },
    []
  );

  // Subscribe to a project's updates
  const subscribeToProject = useCallback(
    (projectId) => {
      if (!connected || !projectId) {
        return;
      }

      // Unsubscribe from previous project
      if (currentProjectId.current && currentProjectId.current !== projectId) {
        unsubscribeFromProject(currentProjectId.current);
      }

      currentProjectId.current = projectId;

      // Subscribe to project updates
      websocketService.subscribe(`/topic/project/${projectId}`, handleProjectMessage);

      // Subscribe to presence updates
      websocketService.subscribe(`/topic/project/${projectId}/presence`, handlePresenceMessage);

      // Send join message
      websocketService.send('/app/project.join', { projectId });
    },
    [connected, handleProjectMessage, handlePresenceMessage]
  );

  // Unsubscribe from a project's updates
  const unsubscribeFromProject = useCallback(
    (projectId) => {
      if (!projectId) {
        return;
      }

      // Send leave message
      if (websocketService.isConnected()) {
        websocketService.send('/app/project.leave', { projectId });
      }

      // Unsubscribe from topics
      websocketService.unsubscribe(`/topic/project/${projectId}`);
      websocketService.unsubscribe(`/topic/project/${projectId}/presence`);

      // Clear online users for this project
      setOnlineUsers((prev) => {
        const newState = { ...prev };
        delete newState[projectId];
        return newState;
      });

      if (currentProjectId.current === projectId) {
        currentProjectId.current = null;
      }
    },
    []
  );

  // Subscribe to user notifications
  const subscribeToNotifications = useCallback(
    (callback) => {
      if (!connected) {
        return null;
      }
      return websocketService.subscribe('/user/queue/notifications', callback);
    },
    [connected]
  );

  // Get online users for a project
  const getOnlineUsers = useCallback(
    (projectId) => {
      return onlineUsers[projectId] || [];
    },
    [onlineUsers]
  );

  const value = {
    connected,
    subscribeToProject,
    unsubscribeFromProject,
    subscribeToNotifications,
    getOnlineUsers,
    onlineUsers,
  };

  return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>;
};

export default WebSocketContext;
