import { render, screen, fireEvent } from '@testing-library/react';
import App from './App';
import { apiRequest, clearAuthToken, clearStoredUser } from './api';

jest.mock('./api', () => ({
  ...jest.requireActual('./api'),
  apiRequest: jest.fn(),
}));

describe('App component', () => {
  beforeEach(() => {
    apiRequest.mockReset();
    clearAuthToken();
    clearStoredUser();
    window.history.pushState({}, '', '/');
  });

  test('renders InterviewIQ heading on home page', () => {
    render(<App />);
    const heading = screen.getByText(/Build AI-Powered Resumes/i);
    expect(heading).toBeInTheDocument();
  });

  test('clicking Get Started without auth opens signup page', () => {
    render(<App />);
    fireEvent.click(screen.getByText('Get Started'));
    expect(screen.getByRole('heading', { name: 'Signup' })).toBeInTheDocument();
  });

  test('clicking Login on navbar opens login page', () => {
    render(<App />);
    fireEvent.click(screen.getByText('Login'));
    expect(screen.getByRole('heading', { name: 'Welcome Back' })).toBeInTheDocument();
  });

  test('successful login navigates to resume builder first', async () => {
    apiRequest.mockImplementation((path) => {
      if (path === '/api/auth/login') {
        return Promise.resolve({
          token: 'test-token',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'JOB_SEEKER',
        });
      }
      if (path === '/api/users/me') {
        return Promise.resolve({
          id: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'JOB_SEEKER',
          profileCompletion: 90,
        });
      }
      if (path === '/api/interview/history') {
        return Promise.resolve([]);
      }
      return Promise.resolve({});
    });

    render(<App />);
    fireEvent.click(screen.getByText('Login'));

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(await screen.findByRole('heading', { name: 'Resume Builder' })).toBeInTheDocument();
  });

  test('logout returns user to home page', async () => {
    apiRequest.mockImplementation((path) => {
      if (path === '/api/auth/login') {
        return Promise.resolve({
          token: 'test-token',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'JOB_SEEKER',
        });
      }
      if (path === '/api/users/me') {
        return Promise.resolve({
          id: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'JOB_SEEKER',
          profileCompletion: 90,
        });
      }
      if (path === '/api/interview/history') {
        return Promise.resolve([]);
      }
      return Promise.resolve({});
    });

    render(<App />);
    fireEvent.click(screen.getByText('Login'));

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(await screen.findByRole('heading', { name: 'Resume Builder' })).toBeInTheDocument();
    fireEvent.click(screen.getByText('Logout'));
    expect(await screen.findByText(/Build AI-Powered Resumes/i)).toBeInTheDocument();
  });
});
