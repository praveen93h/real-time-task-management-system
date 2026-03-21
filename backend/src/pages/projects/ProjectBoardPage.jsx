import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box,
  Typography,
  Button,
  IconButton,
  Tooltip,
  Skeleton,
  Alert,
  Menu,
  MenuItem,
  ListItemIcon,
  Divider,
  AvatarGroup,
  Avatar,
  Chip,
  Badge,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Settings as SettingsIcon,
  Group as GroupIcon,
  MoreVert as MoreVertIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Circle as CircleIcon,
} from '@mui/icons-material';
import { DragDropContext } from '@hello-pangea/dnd';
import { fetchProjectById, deleteProject, fetchProjectMembers } from '../../store/slices/projectSlice';
import { fetchTasksByProject, updateTaskStatus, optimisticStatusUpdate } from '../../store/slices/taskSlice';
import { KanbanColumn, TaskDetailModal, CreateTaskModal } from '../../components/Task';
import ProjectSettingsModal from '../../components/Project/ProjectSettingsModal';
import { useWebSocket } from '../../contexts';

const STATUSES = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE'];

const ProjectBoardPage = () => {
  const { id: projectId } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { currentProject, members, loading: projectLoading, error: projectError } = useSelector(
    (state) => state.projects
  );
  const { tasksByProject, loading: tasksLoading } = useSelector((state) => state.tasks);
  const { user } = useSelector((state) => state.auth);

  const tasks = tasksByProject[projectId] || [];
  const projectMembers = members[projectId] || [];

  const [selectedTask, setSelectedTask] = useState(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [createStatus, setCreateStatus] = useState('TODO');
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [menuAnchor, setMenuAnchor] = useState(null);

  // WebSocket integration
  const { connected, subscribeToProject, unsubscribeFromProject, getOnlineUsers } = useWebSocket();
  const onlineUsers = getOnlineUsers(projectId);

  useEffect(() => {
    if (projectId) {
      dispatch(fetchProjectById(projectId));
      dispatch(fetchTasksByProject(projectId));
      dispatch(fetchProjectMembers(projectId));
    }
  }, [dispatch, projectId]);

  // Subscribe to WebSocket updates when connected
  useEffect(() => {
    if (connected && projectId) {
      subscribeToProject(projectId);
    }

    return () => {
      if (projectId) {
        unsubscribeFromProject(projectId);
      }
    };
  }, [connected, projectId, subscribeToProject, unsubscribeFromProject]);

  // Helper to check if a user is online
  const isUserOnline = (userId) => {
    return onlineUsers.some((u) => u.userId === userId);
  };

  const handleDragEnd = (result) => {
    const { destination, source, draggableId } = result;

    if (!destination) return;
    if (destination.droppableId === source.droppableId && destination.index === source.index) {
      return;
    }

    const taskId = parseInt(draggableId);
    const newStatus = destination.droppableId;
    const newPosition = destination.index;

    // Optimistic update
    dispatch(optimisticStatusUpdate({
      projectId: parseInt(projectId),
      taskId,
      newStatus,
      newPosition,
    }));

    // API call
    dispatch(updateTaskStatus({ taskId, status: newStatus, position: newPosition }));
  };

  const handleTaskClick = (task) => {
    setSelectedTask(task);
  };

  const handleAddTask = (status) => {
    setCreateStatus(status);
    setCreateModalOpen(true);
  };

  const handleDeleteProject = async () => {
    if (window.confirm('Are you sure you want to delete this project? This action cannot be undone.')) {
      await dispatch(deleteProject(projectId));
      navigate('/dashboard');
    }
  };

  const getTasksByStatus = (status) => {
    return tasks.filter((task) => task.status === status);
  };

  if (projectLoading && !currentProject) {
    return (
      <Box>
        <Skeleton variant="text" width={200} height={40} />
        <Box display="flex" gap={2} mt={2}>
          {STATUSES.map((status) => (
            <Skeleton key={status} variant="rounded" width={300} height={400} />
          ))}
        </Box>
      </Box>
    );
  }

  if (projectError) {
    return (
      <Box>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/dashboard')} sx={{ mb: 2 }}>
          Back to Dashboard
        </Button>
        <Alert severity="error">{projectError}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ height: 'calc(100vh - 140px)', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box display="flex" alignItems="center" gap={2}>
          <IconButton onClick={() => navigate('/dashboard')}>
            <ArrowBackIcon />
          </IconButton>
          <Box>
            <Typography variant="h5" fontWeight="bold">
              {currentProject?.name}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {currentProject?.description}
            </Typography>
          </Box>
        </Box>

        <Box display="flex" alignItems="center" gap={2}>
          {/* Connection Status */}
          <Chip
            size="small"
            icon={<CircleIcon sx={{ fontSize: 10 }} />}
            label={connected ? 'Live' : 'Offline'}
            color={connected ? 'success' : 'default'}
            variant="outlined"
            sx={{ 
              '& .MuiChip-icon': { 
                color: connected ? 'success.main' : 'text.disabled' 
              } 
            }}
          />

          {/* Online Users Count */}
          {onlineUsers.length > 0 && (
            <Tooltip title={`${onlineUsers.length} user${onlineUsers.length > 1 ? 's' : ''} online: ${onlineUsers.map(u => u.username).join(', ')}`}>
              <Chip
                size="small"
                icon={<GroupIcon sx={{ fontSize: 16 }} />}
                label={`${onlineUsers.length} online`}
                color="primary"
                variant="outlined"
              />
            </Tooltip>
          )}

          {/* Members with online status */}
          <AvatarGroup max={5}>
            {projectMembers.map((member) => {
              const isOnline = isUserOnline(member.user?.id);
              return (
                <Tooltip 
                  key={member.user?.id} 
                  title={`${member.user?.fullName || member.user?.username}${isOnline ? ' (online)' : ''}`}
                >
                  <Badge
                    overlap="circular"
                    anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                    badgeContent={
                      isOnline ? (
                        <CircleIcon 
                          sx={{ 
                            fontSize: 12, 
                            color: 'success.main',
                            backgroundColor: 'background.paper',
                            borderRadius: '50%',
                          }} 
                        />
                      ) : null
                    }
                  >
                    <Avatar src={member.user?.avatarUrl} sx={{ width: 32, height: 32 }}>
                      {(member.user?.fullName || member.user?.username)?.[0]}
                    </Avatar>
                  </Badge>
                </Tooltip>
              );
            })}
          </AvatarGroup>

          <Tooltip title="Project Settings">
            <IconButton onClick={() => setSettingsOpen(true)}>
              <SettingsIcon />
            </IconButton>
          </Tooltip>

          <IconButton onClick={(e) => setMenuAnchor(e.currentTarget)}>
            <MoreVertIcon />
          </IconButton>

          <Menu
            anchorEl={menuAnchor}
            open={Boolean(menuAnchor)}
            onClose={() => setMenuAnchor(null)}
          >
            <MenuItem onClick={() => { setSettingsOpen(true); setMenuAnchor(null); }}>
              <ListItemIcon>
                <EditIcon fontSize="small" />
              </ListItemIcon>
              Edit Project
            </MenuItem>
            <MenuItem onClick={() => { setSettingsOpen(true); setMenuAnchor(null); }}>
              <ListItemIcon>
                <GroupIcon fontSize="small" />
              </ListItemIcon>
              Manage Members
            </MenuItem>
            <Divider />
            <MenuItem onClick={handleDeleteProject} sx={{ color: 'error.main' }}>
              <ListItemIcon>
                <DeleteIcon fontSize="small" color="error" />
              </ListItemIcon>
              Delete Project
            </MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* Kanban Board */}
      <DragDropContext onDragEnd={handleDragEnd}>
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            flexGrow: 1,
            overflowX: 'auto',
            pb: 2,
          }}
        >
          {STATUSES.map((status) => (
            <KanbanColumn
              key={status}
              status={status}
              tasks={getTasksByStatus(status)}
              onTaskClick={handleTaskClick}
              onAddTask={handleAddTask}
            />
          ))}
        </Box>
      </DragDropContext>

      {/* Task Detail Modal */}
      <TaskDetailModal
        open={Boolean(selectedTask)}
        onClose={() => setSelectedTask(null)}
        task={selectedTask}
        members={projectMembers}
      />

      {/* Create Task Modal */}
      <CreateTaskModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        projectId={parseInt(projectId)}
        initialStatus={createStatus}
        members={projectMembers}
      />

      {/* Project Settings Modal */}
      <ProjectSettingsModal
        open={settingsOpen}
        onClose={() => setSettingsOpen(false)}
        project={currentProject}
      />
    </Box>
  );
};

export default ProjectBoardPage;
