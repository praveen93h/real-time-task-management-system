import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  Tabs,
  Tab,
  Box,
  TextField,
  Button,
  Typography,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction,
  Avatar,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Divider,
  Alert,
  Autocomplete,
  CircularProgress,
} from '@mui/material';
import {
  Close as CloseIcon,
  Delete as DeleteIcon,
  PersonAdd as PersonAddIcon,
} from '@mui/icons-material';
import {
  updateProject,
  fetchProjectMembers,
  addProjectMember,
  removeProjectMember,
} from '../../store/slices/projectSlice';
import api from '../../services/api';

const ProjectSettingsModal = ({ open, onClose, project }) => {
  const dispatch = useDispatch();
  const { members, loading } = useSelector((state) => state.projects);
  const { user } = useSelector((state) => state.auth);

  const [tabValue, setTabValue] = useState(0);
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedRole, setSelectedRole] = useState('MEMBER');
  const [error, setError] = useState('');

  const projectMembers = members[project?.id] || [];

  useEffect(() => {
    if (project) {
      setFormData({
        name: project.name || '',
        description: project.description || '',
      });
      dispatch(fetchProjectMembers(project.id));
    }
  }, [project, dispatch]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSaveProject = async () => {
    await dispatch(updateProject({ projectId: project.id, data: formData }));
    onClose();
  };

  const handleSearchUsers = async (query) => {
    if (!query || query.length < 2) {
      setSearchResults([]);
      return;
    }

    setSearching(true);
    try {
      const response = await api.get(`/users/search?query=${query}`);
      // Filter out existing members
      const existingIds = projectMembers.map((m) => m.user?.id);
      setSearchResults(response.data.filter((u) => !existingIds.includes(u.id)));
    } catch (err) {
      console.error('Search error:', err);
    }
    setSearching(false);
  };

  const handleAddMember = async () => {
    if (!selectedUser) return;
    setError('');
    
    try {
      await dispatch(addProjectMember({
        projectId: project.id,
        userId: selectedUser.id,
        role: selectedRole,
      })).unwrap();
      setSelectedUser(null);
      setSearchQuery('');
      setSearchResults([]);
    } catch (err) {
      setError(err || 'Failed to add member');
    }
  };

  const handleRemoveMember = async (userId) => {
    if (window.confirm('Are you sure you want to remove this member?')) {
      await dispatch(removeProjectMember({ projectId: project.id, userId }));
    }
  };

  const isOwner = project?.owner?.id === user?.id;

  if (!project) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">Project Settings</Typography>
        <IconButton onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} sx={{ mb: 2 }}>
          <Tab label="General" />
          <Tab label="Members" />
        </Tabs>

        {/* General Settings Tab */}
        {tabValue === 0 && (
          <Box>
            <TextField
              fullWidth
              label="Project Name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              margin="normal"
              disabled={!isOwner}
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              margin="normal"
              multiline
              rows={4}
              disabled={!isOwner}
            />
            {isOwner && (
              <Box mt={3}>
                <Button variant="contained" onClick={handleSaveProject} disabled={loading}>
                  {loading ? <CircularProgress size={24} /> : 'Save Changes'}
                </Button>
              </Box>
            )}
          </Box>
        )}

        {/* Members Tab */}
        {tabValue === 1 && (
          <Box>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            {/* Add Member Section */}
            {isOwner && (
              <Box mb={3}>
                <Typography variant="subtitle2" gutterBottom>
                  Add Member
                </Typography>
                <Box display="flex" gap={2} alignItems="flex-start">
                  <Autocomplete
                    sx={{ flexGrow: 1 }}
                    options={searchResults}
                    getOptionLabel={(option) => option.fullName || option.username}
                    value={selectedUser}
                    onChange={(e, value) => setSelectedUser(value)}
                    inputValue={searchQuery}
                    onInputChange={(e, value) => {
                      setSearchQuery(value);
                      handleSearchUsers(value);
                    }}
                    loading={searching}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        label="Search users..."
                        size="small"
                        InputProps={{
                          ...params.InputProps,
                          endAdornment: (
                            <>
                              {searching && <CircularProgress size={20} />}
                              {params.InputProps.endAdornment}
                            </>
                          ),
                        }}
                      />
                    )}
                    renderOption={(props, option) => (
                      <li {...props}>
                        <Avatar src={option.avatarUrl} sx={{ width: 24, height: 24, mr: 1 }}>
                          {option.fullName?.[0]}
                        </Avatar>
                        <Box>
                          <Typography variant="body2">{option.fullName || option.username}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            @{option.username}
                          </Typography>
                        </Box>
                      </li>
                    )}
                  />
                  <FormControl size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Role</InputLabel>
                    <Select
                      value={selectedRole}
                      onChange={(e) => setSelectedRole(e.target.value)}
                      label="Role"
                    >
                      <MenuItem value="MEMBER">Member</MenuItem>
                      <MenuItem value="ADMIN">Admin</MenuItem>
                    </Select>
                  </FormControl>
                  <Button
                    variant="contained"
                    startIcon={<PersonAddIcon />}
                    onClick={handleAddMember}
                    disabled={!selectedUser}
                  >
                    Add
                  </Button>
                </Box>
              </Box>
            )}

            <Divider sx={{ my: 2 }} />

            {/* Members List */}
            <Typography variant="subtitle2" gutterBottom>
              Team Members ({projectMembers.length})
            </Typography>
            <List>
              {projectMembers.map((member) => (
                <ListItem key={member.user?.id}>
                  <ListItemAvatar>
                    <Avatar src={member.user?.avatarUrl}>
                      {(member.user?.fullName || member.user?.username)?.[0]}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        {member.user?.fullName || member.user?.username}
                        {member.user?.id === project.owner?.id && (
                          <Chip label="Owner" size="small" color="primary" />
                        )}
                        {member.role === 'ADMIN' && member.user?.id !== project.owner?.id && (
                          <Chip label="Admin" size="small" variant="outlined" />
                        )}
                      </Box>
                    }
                    secondary={`@${member.user?.username}`}
                  />
                  <ListItemSecondaryAction>
                    {isOwner && member.user?.id !== project.owner?.id && (
                      <IconButton
                        edge="end"
                        onClick={() => handleRemoveMember(member.user?.id)}
                        color="error"
                      >
                        <DeleteIcon />
                      </IconButton>
                    )}
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          </Box>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ProjectSettingsModal;
