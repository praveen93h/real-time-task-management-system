import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  Box,
  TextField,
  Typography,
  IconButton,
  Chip,
  Avatar,
  Divider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  CircularProgress,
} from '@mui/material';
import {
  Close as CloseIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Send as SendIcon,
  Flag as FlagIcon,
  AccessTime as AccessTimeIcon,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import {
  updateTask,
  deleteTask,
  fetchTaskComments,
  addTaskComment,
  deleteTaskComment,
} from '../../store/slices/taskSlice';

const priorityColors = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'error',
  URGENT: 'error',
};

const TaskDetailModal = ({ open, onClose, task, members = [] }) => {
  const dispatch = useDispatch();
  const { comments, loading } = useSelector((state) => state.tasks);
  const { user } = useSelector((state) => state.auth);

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({});
  const [newComment, setNewComment] = useState('');

  const taskComments = comments[task?.id] || [];

  useEffect(() => {
    if (task) {
      setFormData({
        title: task.title,
        description: task.description || '',
        priority: task.priority || 'MEDIUM',
        status: task.status,
        assignedToId: task.assignedTo?.id || '',
        dueDate: task.dueDate ? new Date(task.dueDate) : null,
      });
      dispatch(fetchTaskComments(task.id));
    }
  }, [task, dispatch]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    await dispatch(updateTask({
      taskId: task.id,
      data: {
        ...formData,
        assignedToId: formData.assignedToId || null,
        dueDate: formData.dueDate?.toISOString().split('T')[0],
      },
    }));
    setIsEditing(false);
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      await dispatch(deleteTask(task.id));
      onClose();
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    await dispatch(addTaskComment({ taskId: task.id, content: newComment }));
    setNewComment('');
  };

  const handleDeleteComment = async (commentId) => {
    await dispatch(deleteTaskComment({ taskId: task.id, commentId }));
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString();
  };

  if (!task) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box display="flex" alignItems="center" gap={1}>
          <Typography variant="h6">Task Details</Typography>
          <Chip
            label={task.status.replace('_', ' ')}
            size="small"
            color="primary"
            variant="outlined"
          />
        </Box>
        <Box>
          <IconButton onClick={() => setIsEditing(!isEditing)} size="small">
            <EditIcon />
          </IconButton>
          <IconButton onClick={handleDelete} size="small" color="error">
            <DeleteIcon />
          </IconButton>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        <Box display="flex" gap={3}>
          {/* Main Content */}
          <Box flex={2}>
            {isEditing ? (
              <>
                <TextField
                  fullWidth
                  label="Title"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  margin="normal"
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
                />
                <Box display="flex" gap={2} mt={2} mb={2}>
                  <Button variant="contained" onClick={handleSave}>
                    Save Changes
                  </Button>
                  <Button onClick={() => setIsEditing(false)}>Cancel</Button>
                </Box>
              </>
            ) : (
              <>
                <Typography variant="h5" gutterBottom>
                  {task.title}
                </Typography>
                <Typography variant="body1" color="text.secondary" paragraph>
                  {task.description || 'No description provided'}
                </Typography>
              </>
            )}

            <Divider sx={{ my: 2 }} />

            {/* Comments Section */}
            <Typography variant="h6" gutterBottom>
              Comments ({taskComments.length})
            </Typography>

            <Box display="flex" gap={1} mb={2}>
              <TextField
                fullWidth
                size="small"
                placeholder="Add a comment..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleAddComment()}
              />
              <IconButton
                color="primary"
                onClick={handleAddComment}
                disabled={!newComment.trim()}
              >
                <SendIcon />
              </IconButton>
            </Box>

            <List>
              {taskComments.map((comment) => (
                <ListItem
                  key={comment.id}
                  alignItems="flex-start"
                  secondaryAction={
                    comment.user?.id === user?.id && (
                      <IconButton
                        edge="end"
                        size="small"
                        onClick={() => handleDeleteComment(comment.id)}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    )
                  }
                >
                  <ListItemAvatar>
                    <Avatar src={comment.user?.avatarUrl}>
                      {(comment.user?.fullName || comment.user?.username)?.[0]}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box display="flex" gap={1} alignItems="center">
                        <Typography variant="subtitle2">
                          {comment.user?.fullName || comment.user?.username}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {formatDate(comment.createdAt)}
                        </Typography>
                      </Box>
                    }
                    secondary={comment.content}
                  />
                </ListItem>
              ))}
              {taskComments.length === 0 && (
                <Typography variant="body2" color="text.secondary" textAlign="center" py={2}>
                  No comments yet
                </Typography>
              )}
            </List>
          </Box>

          {/* Sidebar */}
          <Box flex={1} sx={{ borderLeft: 1, borderColor: 'divider', pl: 3 }}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Status
            </Typography>
            {isEditing ? (
              <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                <Select name="status" value={formData.status} onChange={handleChange}>
                  <MenuItem value="TODO">To Do</MenuItem>
                  <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                  <MenuItem value="REVIEW">Review</MenuItem>
                  <MenuItem value="DONE">Done</MenuItem>
                </Select>
              </FormControl>
            ) : (
              <Chip label={task.status.replace('_', ' ')} sx={{ mb: 2 }} />
            )}

            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Priority
            </Typography>
            {isEditing ? (
              <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                <Select name="priority" value={formData.priority} onChange={handleChange}>
                  <MenuItem value="LOW">Low</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HIGH">High</MenuItem>
                  <MenuItem value="URGENT">Urgent</MenuItem>
                </Select>
              </FormControl>
            ) : (
              <Chip
                icon={<FlagIcon />}
                label={task.priority || 'None'}
                color={priorityColors[task.priority]}
                variant="outlined"
                sx={{ mb: 2 }}
              />
            )}

            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Assignee
            </Typography>
            {isEditing ? (
              <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                <Select
                  name="assignedToId"
                  value={formData.assignedToId}
                  onChange={handleChange}
                >
                  <MenuItem value="">Unassigned</MenuItem>
                  {members.map((member) => (
                    <MenuItem key={member.user?.id} value={member.user?.id}>
                      {member.user?.fullName || member.user?.username}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            ) : (
              <Box display="flex" alignItems="center" gap={1} mb={2}>
                {task.assignedTo ? (
                  <>
                    <Avatar src={task.assignedTo.avatarUrl} sx={{ width: 24, height: 24 }}>
                      {task.assignedTo.fullName?.[0]}
                    </Avatar>
                    <Typography variant="body2">
                      {task.assignedTo.fullName || task.assignedTo.username}
                    </Typography>
                  </>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Unassigned
                  </Typography>
                )}
              </Box>
            )}

            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Due Date
            </Typography>
            {isEditing ? (
              <LocalizationProvider dateAdapter={AdapterDateFns}>
                <DatePicker
                  value={formData.dueDate}
                  onChange={(date) => setFormData((prev) => ({ ...prev, dueDate: date }))}
                  slotProps={{ textField: { size: 'small', fullWidth: true } }}
                />
              </LocalizationProvider>
            ) : (
              <Box display="flex" alignItems="center" gap={1} mb={2}>
                <AccessTimeIcon fontSize="small" color="action" />
                <Typography variant="body2">
                  {task.dueDate ? formatDate(task.dueDate) : 'No due date'}
                </Typography>
              </Box>
            )}

            <Divider sx={{ my: 2 }} />

            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Created By
            </Typography>
            <Box display="flex" alignItems="center" gap={1} mb={2}>
              <Avatar src={task.createdBy?.avatarUrl} sx={{ width: 24, height: 24 }}>
                {task.createdBy?.fullName?.[0]}
              </Avatar>
              <Typography variant="body2">
                {task.createdBy?.fullName || task.createdBy?.username}
              </Typography>
            </Box>

            <Typography variant="caption" color="text.secondary">
              Created: {formatDate(task.createdAt)}
            </Typography>
          </Box>
        </Box>
      </DialogContent>
    </Dialog>
  );
};

export default TaskDetailModal;
