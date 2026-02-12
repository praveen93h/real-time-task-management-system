import { Box, Typography, Paper, IconButton, Tooltip, Badge } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { Droppable } from '@hello-pangea/dnd';
import TaskCard from './TaskCard';

const statusColors = {
  TODO: '#1976d2',
  IN_PROGRESS: '#ed6c02',
  REVIEW: '#9c27b0',
  DONE: '#2e7d32',
};

const statusLabels = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  REVIEW: 'Review',
  DONE: 'Done',
};

const KanbanColumn = ({ status, tasks, onTaskClick, onAddTask }) => {
  return (
    <Box
      sx={{
        width: 300,
        minWidth: 300,
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
      }}
    >
      {/* Column Header */}
      <Paper
        elevation={0}
        sx={{
          p: 1.5,
          mb: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          bgcolor: 'background.paper',
          borderLeft: 3,
          borderColor: statusColors[status],
        }}
      >
        <Box display="flex" alignItems="center" gap={1}>
          <Typography variant="subtitle1" fontWeight="bold">
            {statusLabels[status]}
          </Typography>
          <Badge
            badgeContent={tasks.length}
            color="default"
            sx={{
              '& .MuiBadge-badge': {
                bgcolor: 'grey.200',
                color: 'text.secondary',
              },
            }}
          />
        </Box>
        <Tooltip title="Add task">
          <IconButton size="small" onClick={() => onAddTask(status)}>
            <AddIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Paper>

      {/* Droppable Area */}
      <Droppable droppableId={status}>
        {(provided, snapshot) => (
          <Box
            ref={provided.innerRef}
            {...provided.droppableProps}
            sx={{
              flexGrow: 1,
              p: 1,
              bgcolor: snapshot.isDraggingOver ? 'action.hover' : 'grey.50',
              borderRadius: 1,
              minHeight: 200,
              transition: 'background-color 0.2s',
            }}
          >
            {tasks
              .sort((a, b) => a.position - b.position)
              .map((task, index) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  index={index}
                  onClick={onTaskClick}
                />
              ))}
            {provided.placeholder}

            {tasks.length === 0 && !snapshot.isDraggingOver && (
              <Box
                sx={{
                  height: 100,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'text.disabled',
                  border: '2px dashed',
                  borderColor: 'divider',
                  borderRadius: 1,
                }}
              >
                <Typography variant="body2">Drop tasks here</Typography>
              </Box>
            )}
          </Box>
        )}
      </Droppable>
    </Box>
  );
};

export default KanbanColumn;
