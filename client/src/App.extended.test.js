import { render, screen, fireEvent } from '@testing-library/react';
import App from './App';
import { apiRequest } from './api';

jest.mock('./api', () => ({
  ...jest.requireActual('./api'),
  apiRequest: jest.fn(),
}));

const mockNavigate = jest.fn();

describe('App component', () => {
  beforeEach(() => {
    apiRequest.mockClear();
    window.history.pushState({}, '', '/');
  });

  test('renders InterviewIQ heading on home page', () => {
    render(<App />);
    const heading = screen.getByText(/Build AI-Powered Resumes/i);
    expect(heading).toBeInTheDocument();
  });

  test('clicking "Get Started" without being logged in navigates to the signup page', () => {
    render(<App />);
    fireEvent.click(screen.getByText('Get Started'));
    const signupHeading = screen.getByText(/Signup/i);
    expect(signupHeading).toBeInTheDocument();
  });

  test('clicking "Login" on the navbar navigates to the login page', () => {
    render(<App />);
    fireEvent.click(screen.getByText('Login'));
    const loginHeading = screen.getByText(/Login/i);
    expect(loginHeading).toBeInTheDocument();
  });

  test('successful login navigates to the dashboard', async () => {
    apiRequest.mockResolvedValue({
      token: 'test-token',
      userId: '1',
      name: 'Test User',
      email: 'test@example.com',
      role: 'JOB_SEEKER',
    });
    render(<App />);
    fireEvent.click(screen.getByText('Login'));

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    await screen.findByText('Welcome Test User');
    expect(screen.getByText('Welcome Test User')).toBeInTheDocument();
  });

  test('logout button logs the user out and navigates to the home page', async () => {
    apiRequest.mockResolvedValue({
      token: 'test-token',
      userId: '1',
      name: 'Test User',
      email: 'test@example.com',
      role: 'JOB_SEEKER',
    });
    render(<App />);
    fireEvent.click(screen.getByText('Login'));

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    await screen.findByText('Welcome Test User');
    fireEvent.click(screen.getByText('Logout'));
    const heading = screen.getByText(/Build AI-Powered Resumes/i);
    expect(heading).toBeInTheDocument();
  });
});
