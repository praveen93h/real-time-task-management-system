import { Box, Container, Typography } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { Dashboard as DashboardIcon } from '@mui/icons-material';

const AuthLayout = () => {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        bgcolor: 'background.default',
      }}
    >
      {/* Header */}
      <Box
        sx={{
          py: 2,
          textAlign: 'center',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
          <DashboardIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          <Typography variant="h5" fontWeight="bold" color="primary.main">
            TaskFlow
          </Typography>
        </Box>
      </Box>

      {/* Content */}
      <Container
        maxWidth="sm"
        sx={{
          flexGrow: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          py: 4,
        }}
      >
        <Outlet />
      </Container>

      {/* Footer */}
      <Box sx={{ py: 2, textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          © 2026 TaskFlow. All rights reserved.
        </Typography>
      </Box>
    </Box>
  );
};

export default AuthLayout;
