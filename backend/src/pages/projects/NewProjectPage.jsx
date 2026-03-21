import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import { createProject } from '../../store/slices/projectSlice';

const NewProjectPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.projects);

  const [formData, setFormData] = useState({
    name: '',
    description: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(createProject(formData));
    if (!result.error) {
      navigate(`/projects/${result.payload.id}`);
    }
  };

  return (
    <Box maxWidth="sm" mx="auto">
      <Button
        startIcon={<ArrowBackIcon />}
        onClick={() => navigate(-1)}
        sx={{ mb: 2 }}
      >
        Back
      </Button>

      <Card>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" fontWeight="bold" gutterBottom>
            Create New Project
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Set up a new project to organize your tasks
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Project Name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              margin="normal"
              required
              autoFocus
              placeholder="e.g., Website Redesign"
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
              placeholder="What is this project about?"
            />

            <Box display="flex" gap={2} mt={3}>
              <Button
                variant="outlined"
                onClick={() => navigate(-1)}
                disabled={loading}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                disabled={loading || !formData.name.trim()}
                sx={{ flexGrow: 1 }}
              >
                {loading ? <CircularProgress size={24} /> : 'Create Project'}
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default NewProjectPage;
