import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  Skeleton,
  Alert,
  Chip,
  Avatar,
  AvatarGroup,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Add as AddIcon,
  Folder as FolderIcon,
  ArrowForward as ArrowForwardIcon,
  Group as GroupIcon,
  Assignment as AssignmentIcon,
} from '@mui/icons-material';
import { fetchProjects } from '../../store/slices/projectSlice';

const DashboardPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { projects, loading, error } = useSelector((state) => state.projects);

  useEffect(() => {
    dispatch(fetchProjects());
  }, [dispatch]);

  const handleProjectClick = (projectId) => {
    navigate(`/projects/${projectId}`);
  };

  if (loading && projects.length === 0) {
    return (
      <Box>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h4">Dashboard</Typography>
        </Box>
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={i}>
              <Skeleton variant="rounded" height={200} />
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" fontWeight="bold">
            Dashboard
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {projects.length} project{projects.length !== 1 ? 's' : ''}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/projects/new')}
        >
          New Project
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Projects Grid */}
      {projects.length === 0 ? (
        <Card sx={{ textAlign: 'center', py: 6 }}>
          <FolderIcon sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No projects yet
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Create your first project to get started
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/projects/new')}
          >
            Create Project
          </Button>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {projects.map((project) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={project.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  cursor: 'pointer',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
                onClick={() => handleProjectClick(project.id)}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" alignItems="flex-start" justifyContent="space-between" mb={1}>
                    <Typography variant="h6" component="div" noWrap sx={{ flexGrow: 1 }}>
                      {project.name}
                    </Typography>
                  </Box>
                  
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      mb: 2,
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                      minHeight: 40,
                    }}
                  >
                    {project.description || 'No description'}
                  </Typography>

                  <Box display="flex" gap={1} flexWrap="wrap" mb={2}>
                    <Chip
                      icon={<AssignmentIcon />}
                      label={`${project.taskCount || 0} tasks`}
                      size="small"
                      variant="outlined"
                    />
                    <Chip
                      icon={<GroupIcon />}
                      label={`${project.memberCount || 1} members`}
                      size="small"
                      variant="outlined"
                    />
                  </Box>

                  {project.members && project.members.length > 0 && (
                    <AvatarGroup max={4} sx={{ justifyContent: 'flex-start' }}>
                      {project.members.map((member) => (
                        <Tooltip key={member.user?.id} title={member.user?.fullName || member.user?.username}>
                          <Avatar
                            src={member.user?.avatarUrl}
                            alt={member.user?.fullName}
                            sx={{ width: 28, height: 28 }}
                          >
                            {(member.user?.fullName || member.user?.username)?.[0]}
                          </Avatar>
                        </Tooltip>
                      ))}
                    </AvatarGroup>
                  )}
                </CardContent>

                <CardActions sx={{ justifyContent: 'flex-end', pt: 0 }}>
                  <Tooltip title="Open project">
                    <IconButton size="small" color="primary">
                      <ArrowForwardIcon />
                    </IconButton>
                  </Tooltip>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
};

export default DashboardPage;
