import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// Async thunks
export const fetchProjects = createAsyncThunk(
  'projects/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/projects');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch projects');
    }
  }
);

export const fetchProjectById = createAsyncThunk(
  'projects/fetchById',
  async (projectId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/projects/${projectId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch project');
    }
  }
);

export const createProject = createAsyncThunk(
  'projects/create',
  async (projectData, { rejectWithValue }) => {
    try {
      const response = await api.post('/projects', projectData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create project');
    }
  }
);

export const updateProject = createAsyncThunk(
  'projects/update',
  async ({ projectId, data }, { rejectWithValue }) => {
    try {
      const response = await api.put(`/projects/${projectId}`, data);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update project');
    }
  }
);

export const deleteProject = createAsyncThunk(
  'projects/delete',
  async (projectId, { rejectWithValue }) => {
    try {
      await api.delete(`/projects/${projectId}`);
      return projectId;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete project');
    }
  }
);

export const fetchProjectMembers = createAsyncThunk(
  'projects/fetchMembers',
  async (projectId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/projects/${projectId}/members`);
      return { projectId, members: response.data };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch members');
    }
  }
);

export const addProjectMember = createAsyncThunk(
  'projects/addMember',
  async ({ projectId, userId, role }, { rejectWithValue }) => {
    try {
      const response = await api.post(`/projects/${projectId}/members`, { userId, role });
      return { projectId, member: response.data };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to add member');
    }
  }
);

export const removeProjectMember = createAsyncThunk(
  'projects/removeMember',
  async ({ projectId, userId }, { rejectWithValue }) => {
    try {
      await api.delete(`/projects/${projectId}/members/${userId}`);
      return { projectId, userId };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to remove member');
    }
  }
);

const initialState = {
  projects: [],
  currentProject: null,
  members: {},
  loading: false,
  error: null,
};

const projectSlice = createSlice({
  name: 'projects',
  initialState,
  reducers: {
    clearCurrentProject: (state) => {
      state.currentProject = null;
    },
    clearError: (state) => {
      state.error = null;
    },
    setCurrentProject: (state, action) => {
      state.currentProject = action.payload;
    },
    // Real-time updates
    projectUpdated: (state, action) => {
      const index = state.projects.findIndex(p => p.id === action.payload.id);
      if (index !== -1) {
        state.projects[index] = action.payload;
      }
      if (state.currentProject?.id === action.payload.id) {
        state.currentProject = action.payload;
      }
    },
    projectDeleted: (state, action) => {
      state.projects = state.projects.filter(p => p.id !== action.payload);
      if (state.currentProject?.id === action.payload) {
        state.currentProject = null;
      }
    },
    memberAdded: (state, action) => {
      const { projectId, member } = action.payload;
      if (state.members[projectId]) {
        state.members[projectId].push(member);
      }
    },
    memberRemoved: (state, action) => {
      const { projectId, userId } = action.payload;
      if (state.members[projectId]) {
        state.members[projectId] = state.members[projectId].filter(m => m.user.id !== userId);
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch all projects
      .addCase(fetchProjects.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProjects.fulfilled, (state, action) => {
        state.loading = false;
        state.projects = action.payload;
      })
      .addCase(fetchProjects.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Fetch project by ID
      .addCase(fetchProjectById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProjectById.fulfilled, (state, action) => {
        state.loading = false;
        state.currentProject = action.payload;
      })
      .addCase(fetchProjectById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Create project
      .addCase(createProject.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createProject.fulfilled, (state, action) => {
        state.loading = false;
        state.projects.push(action.payload);
      })
      .addCase(createProject.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Update project
      .addCase(updateProject.fulfilled, (state, action) => {
        const index = state.projects.findIndex(p => p.id === action.payload.id);
        if (index !== -1) {
          state.projects[index] = action.payload;
        }
        if (state.currentProject?.id === action.payload.id) {
          state.currentProject = action.payload;
        }
      })
      // Delete project
      .addCase(deleteProject.fulfilled, (state, action) => {
        state.projects = state.projects.filter(p => p.id !== action.payload);
      })
      // Fetch members
      .addCase(fetchProjectMembers.fulfilled, (state, action) => {
        state.members[action.payload.projectId] = action.payload.members;
      })
      // Add member
      .addCase(addProjectMember.fulfilled, (state, action) => {
        const { projectId, member } = action.payload;
        if (state.members[projectId]) {
          state.members[projectId].push(member);
        }
      })
      // Remove member
      .addCase(removeProjectMember.fulfilled, (state, action) => {
        const { projectId, userId } = action.payload;
        if (state.members[projectId]) {
          state.members[projectId] = state.members[projectId].filter(m => m.user.id !== userId);
        }
      });
  },
});

export const {
  clearCurrentProject,
  clearError,
  setCurrentProject,
  projectUpdated,
  projectDeleted,
  memberAdded,
  memberRemoved,
} = projectSlice.actions;

export default projectSlice.reducer;
