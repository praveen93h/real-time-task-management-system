import {
  Card,
  CardContent,
  Typography,
  Box,
  Chip,
  Avatar,
  Tooltip,
  IconButton,
} from '@mui/material';
import {
  Flag as FlagIcon,
  Comment as CommentIcon,
  MoreVert as MoreVertIcon,
  AccessTime as AccessTimeIcon,
} from '@mui/icons-material';
import { Draggable } from '@hello-pangea/dnd';

const priorityColors = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'error',
  URGENT: 'error',
};

const priorityLabels = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
};

const TaskCard = ({ task, index, onClick }) => {
  const formatDate = (dateStr) => {
    if (!dateStr) return null;
    const date = new Date(dateStr);
    const now = new Date();
    const diffDays = Math.ceil((date - now) / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) return { text: 'Overdue', color: 'error' };
    if (diffDays === 0) return { text: 'Today', color: 'warning' };
    if (diffDays === 1) return { text: 'Tomorrow', color: 'info' };
    if (diffDays <= 7) return { text: `${diffDays} days`, color: 'default' };
    return { text: date.toLocaleDateString(), color: 'default' };
  };

  const dueInfo = formatDate(task.dueDate);

  return (
    <Draggable draggableId={String(task.id)} index={index}>
      {(provided, snapshot) => (
        <Card
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
          sx={{
            mb: 1.5,
            cursor: 'pointer',
            bgcolor: snapshot.isDragging ? 'action.hover' : 'background.paper',
            boxShadow: snapshot.isDragging ? 4 : 1,
            transition: 'box-shadow 0.2s',
            '&:hover': {
              boxShadow: 2,
            },
          }}
          onClick={() => onClick(task)}
        >
          <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
            {/* Priority */}
            {task.priority && (
              <Box mb={1}>
                <Chip
                  icon={<FlagIcon />}
                  label={priorityLabels[task.priority]}
                  size="small"
                  color={priorityColors[task.priority]}
                  variant="outlined"
                  sx={{ height: 22, '& .MuiChip-label': { px: 1 } }}
                />
              </Box>
            )}

            {/* Title */}
            <Typography
              variant="body1"
              fontWeight="medium"
              sx={{
                mb: 1,
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
              }}
            >
              {task.title}
            </Typography>

            {/* Description preview */}
            {task.description && (
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{
                  mb: 1.5,
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden',
                }}
              >
                {task.description}
              </Typography>
            )}

            {/* Footer */}
            <Box display="flex" alignItems="center" justifyContent="space-between">
              <Box display="flex" alignItems="center" gap={1}>
                {/* Due date */}
                {dueInfo && (
                  <Chip
                    icon={<AccessTimeIcon />}
                    label={dueInfo.text}
                    size="small"
                    color={dueInfo.color}
                    variant="outlined"
                    sx={{ height: 22, '& .MuiChip-label': { px: 1 } }}
                  />
                )}

                {/* Comments count */}
                {task.commentCount > 0 && (
                  <Box display="flex" alignItems="center" gap={0.5} color="text.secondary">
                    <CommentIcon sx={{ fontSize: 16 }} />
                    <Typography variant="caption">{task.commentCount}</Typography>
                  </Box>
                )}
              </Box>

              {/* Assignee */}
              {task.assignedTo && (
                <Tooltip title={task.assignedTo.fullName || task.assignedTo.username}>
                  <Avatar
                    src={task.assignedTo.avatarUrl}
                    alt={task.assignedTo.fullName}
                    sx={{ width: 28, height: 28 }}
                  >
                    {(task.assignedTo.fullName || task.assignedTo.username)?.[0]}
                  </Avatar>
                </Tooltip>
              )}
            </Box>
          </CardContent>
        </Card>
      )}
    </Draggable>
  );
};

export default TaskCard;
