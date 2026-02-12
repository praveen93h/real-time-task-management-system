import { useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import { toast } from 'react-toastify';

export const useRealtimeNotifications = (projectId) => {
  const { tasksByProject } = useSelector((state) => state.tasks);
  const { user } = useSelector((state) => state.auth);
  const prevTasksRef = useRef(null);

  useEffect(() => {
    const currentTasks = tasksByProject[projectId] || [];
    const prevTasks = prevTasksRef.current;

    if (prevTasks && currentTasks.length !== prevTasks.length) {
      // New task added
      if (currentTasks.length > prevTasks.length) {
        const newTask = currentTasks.find(
          (task) => !prevTasks.some((pt) => pt.id === task.id)
        );
        if (newTask && newTask.createdBy?.id !== user?.id) {
          toast.info(`New task "${newTask.title}" was created`, {
            position: 'bottom-right',
            autoClose: 3000,
          });
        }
      }
    }

    prevTasksRef.current = currentTasks;
  }, [tasksByProject, projectId, user?.id]);
};

export default useRealtimeNotifications;
