import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// Async thunks
export const fetchTasksByProject = createAsyncThunk(
  'tasks/fetchByProject',
  async (projectId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/projects/${projectId}/tasks`);
      return { projectId, tasks: response.data };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch tasks');
    }
  }
);

export const fetchTaskById = createAsyncThunk(
  'tasks/fetchById',
  async (taskId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/tasks/${taskId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch task');
    }
  }
);

export const createTask = createAsyncThunk(
  'tasks/create',
  async ({ projectId, taskData }, { rejectWithValue }) => {
    try {
      const response = await api.post(`/projects/${projectId}/tasks`, taskData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create task');
    }
  }
);

export const updateTask = createAsyncThunk(
  'tasks/update',
  async ({ taskId, data }, { rejectWithValue }) => {
    try {
      const response = await api.put(`/tasks/${taskId}`, data);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update task');
    }
  }
);

export const updateTaskStatus = createAsyncThunk(
  'tasks/updateStatus',
  async ({ taskId, status, position }, { rejectWithValue }) => {
    try {
      const response = await api.patch(`/tasks/${taskId}/status`, { status, position });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update task status');
    }
  }
);

export const assignTask = createAsyncThunk(
  'tasks/assign',
  async ({ taskId, userId }, { rejectWithValue }) => {
    try {
      const response = await api.patch(`/tasks/${taskId}/assign`, { userId });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to assign task');
    }
  }
);

export const deleteTask = createAsyncThunk(
  'tasks/delete',
  async (taskId, { rejectWithValue }) => {
    try {
      await api.delete(`/tasks/${taskId}`);
      return taskId;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete task');
    }
  }
);

export const fetchTaskComments = createAsyncThunk(
  'tasks/fetchComments',
  async (taskId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/tasks/${taskId}/comments`);
      return { taskId, comments: response.data };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch comments');
    }
  }
);

export const addTaskComment = createAsyncThunk(
  'tasks/addComment',
  async ({ taskId, content }, { rejectWithValue }) => {
    try {
      const response = await api.post(`/tasks/${taskId}/comments`, { content });
      return { taskId, comment: response.data };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to add comment');
    }
  }
);

export const deleteTaskComment = createAsyncThunk(
  'tasks/deleteComment',
  async ({ taskId, commentId }, { rejectWithValue }) => {
    try {
      await api.delete(`/tasks/${taskId}/comments/${commentId}`);
      return { taskId, commentId };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete comment');
    }
  }
);

const initialState = {
  tasksByProject: {},
  currentTask: null,
  comments: {},
  loading: false,
  error: null,
};

const taskSlice = createSlice({
  name: 'tasks',
  initialState,
  reducers: {
    clearCurrentTask: (state) => {
      state.currentTask = null;
    },
    setCurrentTask: (state, action) => {
      state.currentTask = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
    clearTasks: (state) => {
      state.tasksByProject = {};
      state.currentTask = null;
    },
    // Real-time updates
    taskCreated: (state, action) => {
      const task = action.payload;
      if (state.tasksByProject[task.projectId]) {
        state.tasksByProject[task.projectId].push(task);
      }
    },
    taskUpdated: (state, action) => {
      const task = action.payload;
      if (state.tasksByProject[task.projectId]) {
        const index = state.tasksByProject[task.projectId].findIndex(t => t.id === task.id);
        if (index !== -1) {
          state.tasksByProject[task.projectId][index] = task;
        }
      }
      if (state.currentTask?.id === task.id) {
        state.currentTask = task;
      }
    },
    taskDeleted: (state, action) => {
      const { projectId, taskId } = action.payload;
      if (state.tasksByProject[projectId]) {
        state.tasksByProject[projectId] = state.tasksByProject[projectId].filter(t => t.id !== taskId);
      }
      if (state.currentTask?.id === taskId) {
        state.currentTask = null;
      }
    },
    commentAdded: (state, action) => {
      const { taskId, comment } = action.payload;
      if (state.comments[taskId]) {
        state.comments[taskId].push(comment);
      }
    },
    commentDeleted: (state, action) => {
      const { taskId, commentId } = action.payload;
      if (state.comments[taskId]) {
        state.comments[taskId] = state.comments[taskId].filter(c => c.id !== commentId);
      }
    },
    // Optimistic update for drag-drop
    optimisticStatusUpdate: (state, action) => {
      const { projectId, taskId, newStatus, newPosition } = action.payload;
      if (state.tasksByProject[projectId]) {
        const task = state.tasksByProject[projectId].find(t => t.id === taskId);
        if (task) {
          task.status = newStatus;
          task.position = newPosition;
        }
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch tasks by project
      .addCase(fetchTasksByProject.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTasksByProject.fulfilled, (state, action) => {
        state.loading = false;
        state.tasksByProject[action.payload.projectId] = action.payload.tasks;
      })
      .addCase(fetchTasksByProject.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Fetch task by ID
      .addCase(fetchTaskById.fulfilled, (state, action) => {
        state.currentTask = action.payload;
      })
      // Create task
      .addCase(createTask.pending, (state) => {
        state.loading = true;
      })
      .addCase(createTask.fulfilled, (state, action) => {
        state.loading = false;
        const task = action.payload;
        if (state.tasksByProject[task.projectId]) {
          state.tasksByProject[task.projectId].push(task);
        }
      })
      .addCase(createTask.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Update task
      .addCase(updateTask.fulfilled, (state, action) => {
        const task = action.payload;
        if (state.tasksByProject[task.projectId]) {
          const index = state.tasksByProject[task.projectId].findIndex(t => t.id === task.id);
          if (index !== -1) {
            state.tasksByProject[task.projectId][index] = task;
          }
        }
        if (state.currentTask?.id === task.id) {
          state.currentTask = task;
        }
      })
      // Update task status
      .addCase(updateTaskStatus.fulfilled, (state, action) => {
        const task = action.payload;
        if (state.tasksByProject[task.projectId]) {
          const index = state.tasksByProject[task.projectId].findIndex(t => t.id === task.id);
          if (index !== -1) {
            state.tasksByProject[task.projectId][index] = task;
          }
        }
      })
      // Assign task
      .addCase(assignTask.fulfilled, (state, action) => {
        const task = action.payload;
        if (state.tasksByProject[task.projectId]) {
          const index = state.tasksByProject[task.projectId].findIndex(t => t.id === task.id);
          if (index !== -1) {
            state.tasksByProject[task.projectId][index] = task;
          }
        }
      })
      // Delete task
      .addCase(deleteTask.fulfilled, (state, action) => {
        const taskId = action.payload;
        Object.keys(state.tasksByProject).forEach(projectId => {
          state.tasksByProject[projectId] = state.tasksByProject[projectId].filter(t => t.id !== taskId);
        });
      })
      // Fetch comments
      .addCase(fetchTaskComments.fulfilled, (state, action) => {
        state.comments[action.payload.taskId] = action.payload.comments;
      })
      // Add comment
      .addCase(addTaskComment.fulfilled, (state, action) => {
        const { taskId, comment } = action.payload;
        if (!state.comments[taskId]) {
          state.comments[taskId] = [];
        }
        state.comments[taskId].push(comment);
      })
      // Delete comment
      .addCase(deleteTaskComment.fulfilled, (state, action) => {
        const { taskId, commentId } = action.payload;
        if (state.comments[taskId]) {
          state.comments[taskId] = state.comments[taskId].filter(c => c.id !== commentId);
        }
      });
  },
});

export const {
  clearCurrentTask,
  setCurrentTask,
  clearError,
  clearTasks,
  taskCreated,
  taskUpdated,
  taskDeleted,
  commentAdded,
  commentDeleted,
  optimisticStatusUpdate,
} = taskSlice.actions;

export default taskSlice.reducer;
