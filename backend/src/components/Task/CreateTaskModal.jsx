import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { createTask } from '../../store/slices/taskSlice';

const CreateTaskModal = ({ open, onClose, projectId, initialStatus = 'TODO', members = [] }) => {
  const dispatch = useDispatch();
  const { loading } = useSelector((state) => state.tasks);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    status: initialStatus,
    priority: 'MEDIUM',
    assignedToId: '',
    dueDate: null,
  });

  useEffect(() => {
    if (open) {
      setFormData({
        title: '',
        description: '',
        status: initialStatus,
        priority: 'MEDIUM',
        assignedToId: '',
        dueDate: null,
      });
    }
  }, [open, initialStatus]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async () => {
    const taskData = {
      ...formData,
      assignedToId: formData.assignedToId || null,
      dueDate: formData.dueDate?.toISOString().split('T')[0],
    };

    const result = await dispatch(createTask({ projectId, taskData }));
    if (!result.error) {
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Create New Task</DialogTitle>
      <DialogContent>
        <Box sx={{ pt: 1 }}>
          <TextField
            fullWidth
            label="Task Title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            margin="normal"
            required
            autoFocus
          />

          <TextField
            fullWidth
            label="Description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            margin="normal"
            multiline
            rows={3}
          />

          <Box display="flex" gap={2} mt={2}>
            <FormControl fullWidth>
              <InputLabel>Status</InputLabel>
              <Select
                name="status"
                value={formData.status}
                onChange={handleChange}
                label="Status"
              >
                <MenuItem value="TODO">To Do</MenuItem>
                <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                <MenuItem value="REVIEW">Review</MenuItem>
                <MenuItem value="DONE">Done</MenuItem>
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Priority</InputLabel>
              <Select
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                label="Priority"
              >
                <MenuItem value="LOW">Low</MenuItem>
                <MenuItem value="MEDIUM">Medium</MenuItem>
                <MenuItem value="HIGH">High</MenuItem>
                <MenuItem value="URGENT">Urgent</MenuItem>
              </Select>
            </FormControl>
          </Box>

          <Box display="flex" gap={2} mt={2}>
            <FormControl fullWidth>
              <InputLabel>Assign To</InputLabel>
              <Select
                name="assignedToId"
                value={formData.assignedToId}
                onChange={handleChange}
                label="Assign To"
              >
                <MenuItem value="">Unassigned</MenuItem>
                {members.map((member) => (
                  <MenuItem key={member.user?.id} value={member.user?.id}>
                    {member.user?.fullName || member.user?.username}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DatePicker
                label="Due Date"
                value={formData.dueDate}
                onChange={(date) => setFormData((prev) => ({ ...prev, dueDate: date }))}
                slotProps={{
                  textField: { fullWidth: true },
                }}
              />
            </LocalizationProvider>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={loading || !formData.title.trim()}
        >
          {loading ? <CircularProgress size={24} /> : 'Create Task'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CreateTaskModal;
