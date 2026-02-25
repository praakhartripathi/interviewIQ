import { render, screen } from '@testing-library/react';
import App from './App';

test('renders InterviewIQ heading', () => {
  render(<App />);
  const heading = screen.getByText(/Build AI-Powered Resumes/i);
  expect(heading).toBeInTheDocument();
});
