import { useCallback, useEffect, useMemo, useState } from 'react';
import './App.css';
import {
  apiRequest,
  clearAuthToken,
  clearStoredUser,
  getAuthToken,
  getStoredUser,
  setAuthToken,
  setStoredUser,
} from './api';

function App() {
  const [path, setPath] = useState(window.location.pathname);
  const [token, setToken] = useState(getAuthToken());
  const [user, setUser] = useState(getStoredUser());

  const isLoggedIn = useMemo(() => Boolean(token), [token]);

  useEffect(() => {
    const onPopState = () => setPath(window.location.pathname);
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);

  const navigate = useCallback((to) => {
    if (to === path) return;
    window.history.pushState({}, '', to);
    setPath(to);
  }, [path]);

  const doLogout = useCallback(() => {
    clearAuthToken();
    clearStoredUser();
    setToken(null);
    setUser(null);
    navigate('/');
  }, [navigate]);

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [path]);

  useEffect(() => {
    if (!token) {
      setUser(null);
      return;
    }

    apiRequest('/api/users/me', { token })
      .then((profile) => {
        const safeUser = { id: profile.id, name: profile.name, email: profile.email, role: profile.role };
        setUser(safeUser);
        setStoredUser(safeUser);
      })
      .catch(() => {
        doLogout();
      });
  }, [token, doLogout]);

  const onAuthSuccess = (data) => {
    const safeUser = {
      id: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    };

    setAuthToken(data.token);
    setStoredUser(safeUser);
    setToken(data.token);
    setUser(safeUser);
    navigate('/dashboard');
  };

  const onClickGetStarted = () => {
    if (isLoggedIn) {
      navigate('/dashboard');
      return;
    }
    navigate('/signup');
  };

  return (
    <main className="iq-app">
      <div className="bg-orb orb-one" />
      <div className="bg-orb orb-two" />
      <div className="bg-orb orb-three" />

      <div className="iq-container">
        <Navbar
          navigate={navigate}
          isLoggedIn={isLoggedIn}
          onGetStarted={onClickGetStarted}
          currentPath={path}
        />

        {isProtected(path) ? (
          isLoggedIn ? (
            <DashboardLayout
              path={path}
              navigate={navigate}
              token={token}
              user={user}
              onLogout={doLogout}
            />
          ) : (
            <AuthRequired navigate={navigate} />
          )
        ) : (
          <PublicRoutes
            path={path}
            navigate={navigate}
            isLoggedIn={isLoggedIn}
            onAuthSuccess={onAuthSuccess}
          />
        )}
      </div>
    </main>
  );
}

function Navbar({ navigate, isLoggedIn, onGetStarted, currentPath }) {
  return (
    <header className="glass navbar">
      <button className="logo-wrap" onClick={() => navigate('/')}>
        <span className="logo-mark">IQ</span>
        <span className="logo-text">InterviewIQ</span>
      </button>

      <nav className="nav-links">
        <button className={currentPath === '/features' ? 'active' : ''} onClick={() => navigate('/features')}>Features</button>
        <button className={currentPath === '/pricing' ? 'active' : ''} onClick={() => navigate('/pricing')}>Pricing</button>
        <button className={currentPath === '/login' ? 'active' : ''} onClick={() => navigate('/login')}>Login</button>
      </nav>

      <button className="btn btn-light" onClick={onGetStarted}>
        {isLoggedIn ? 'Go to Dashboard' : 'Get Started'}
      </button>
    </header>
  );
}

function PublicRoutes({ path, navigate, isLoggedIn, onAuthSuccess }) {
  switch (path) {
    case '/':
      return <HomePage navigate={navigate} isLoggedIn={isLoggedIn} />;
    case '/features':
      return <FeaturesPage />;
    case '/pricing':
      return <PricingPage navigate={navigate} isLoggedIn={isLoggedIn} />;
    case '/login':
      return isLoggedIn ? <RedirectCard text="Already logged in" buttonText="Open Dashboard" onClick={() => navigate('/dashboard')} /> : <LoginPage onAuthSuccess={onAuthSuccess} navigate={navigate} />;
    case '/signup':
      return isLoggedIn ? <RedirectCard text="Account active" buttonText="Open Dashboard" onClick={() => navigate('/dashboard')} /> : <SignupPage onAuthSuccess={onAuthSuccess} navigate={navigate} />;
    default:
      return <RedirectCard text="Page not found" buttonText="Back Home" onClick={() => navigate('/')} />;
  }
}

function HomePage({ navigate, isLoggedIn }) {
  return (
    <section className="hero-grid">
      <div>
        <p className="eyebrow">AI Resume Builder + AI Interview Coach</p>
        <h1>Build AI-Powered Resumes &amp; Crack Interviews Faster</h1>
        <p className="hero-sub">
          InterviewIQ helps you optimize resume quality, improve ATS score, and sharpen interview responses with real-time AI feedback.
        </p>
        <div className="hero-actions">
          <button className="btn btn-primary" onClick={() => navigate(isLoggedIn ? '/resume-builder' : '/signup')}>Build Resume</button>
          <button className="btn btn-ghost" onClick={() => navigate(isLoggedIn ? '/interview' : '/signup')}>Start AI Interview</button>
        </div>
      </div>

      <div className="glass dashboard-card">
        <div className="between-row">
          <p>Resume Score</p>
          <span className="pill">Live AI Feedback</span>
        </div>
        <h2 className="score">89</h2>
        <div className="progress"><span /></div>
        <ul className="feedback-list">
          <li>Add measurable outcomes for your top projects.</li>
          <li>Improve keyword matching for target job role.</li>
          <li>Shorten summary to improve readability.</li>
        </ul>
      </div>
    </section>
  );
}

function FeaturesPage() {
  const items = [
    'AI Resume Builder',
    'ATS Score Analyzer',
    'AI Mock Interview',
    'Skill Gap Analysis',
    'Resume Templates',
  ];

  return (
    <section className="section-stack">
      <h2>Features</h2>
      <p className="muted">Everything needed to become interview-ready with an AI-first workflow.</p>
      <div className="feature-grid">
        {items.map((item) => (
          <article key={item} className="glass feature-card">
            <h3>{item}</h3>
            <p>Production-focused workflows for resume optimization and interview readiness.</p>
          </article>
        ))}
      </div>
    </section>
  );
}

function PricingPage({ navigate, isLoggedIn }) {
  return (
    <section className="section-stack">
      <h2>Pricing</h2>
      <div className="pricing-grid">
        <article className="glass plan">
          <h3>Free</h3>
          <p className="price">$0</p>
          <ul>
            <li>Basic resume builder</li>
            <li>Limited ATS analysis</li>
            <li>3 mock interview sessions/month</li>
          </ul>
          <button className="btn btn-ghost" onClick={() => navigate(isLoggedIn ? '/dashboard' : '/signup')}>Start Free</button>
        </article>

        <article className="glass plan plan-pro">
          <h3>Pro</h3>
          <p className="price">$19/mo</p>
          <ul>
            <li>Advanced resume optimization</li>
            <li>Unlimited AI mock interviews</li>
            <li>Deep skill-gap analysis</li>
          </ul>
          <button className="btn btn-light" onClick={() => navigate(isLoggedIn ? '/dashboard' : '/signup')}>Upgrade to Pro</button>
        </article>
      </div>
    </section>
  );
}

function LoginPage({ onAuthSuccess, navigate }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await apiRequest('/api/auth/login', {
        method: 'POST',
        body: { email, password },
      });
      onAuthSuccess(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-wrap glass">
      <h2>Login</h2>
      <form onSubmit={submit} className="auth-form">
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        {error ? <p className="form-error">{error}</p> : null}
        <button className="btn btn-primary" type="submit" disabled={loading}>{loading ? 'Logging in...' : 'Login'}</button>
      </form>
      <p className="muted">No account? <button className="text-link" onClick={() => navigate('/signup')}>Create one</button></p>
    </section>
  );
}

function SignupPage({ onAuthSuccess, navigate }) {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'JOB_SEEKER',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    try {
      const data = await apiRequest('/api/auth/register', {
        method: 'POST',
        body: form,
      });
      onAuthSuccess(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-wrap glass">
      <h2>Signup</h2>
      <form onSubmit={submit} className="auth-form">
        <label>Full Name</label>
        <input value={form.fullName} onChange={(e) => onChange('fullName', e.target.value)} required />

        <label>Email</label>
        <input type="email" value={form.email} onChange={(e) => onChange('email', e.target.value)} required />

        <label>Password</label>
        <input type="password" value={form.password} onChange={(e) => onChange('password', e.target.value)} required />

        <label>Confirm Password</label>
        <input type="password" value={form.confirmPassword} onChange={(e) => onChange('confirmPassword', e.target.value)} required />

        <label>Role</label>
        <select value={form.role} onChange={(e) => onChange('role', e.target.value)}>
          <option value="JOB_SEEKER">Job Seeker</option>
          <option value="STUDENT">Student</option>
        </select>

        {error ? <p className="form-error">{error}</p> : null}
        <button className="btn btn-primary" type="submit" disabled={loading}>{loading ? 'Creating account...' : 'Create Account'}</button>
      </form>
      <p className="muted">Already have an account? <button className="text-link" onClick={() => navigate('/login')}>Login</button></p>
    </section>
  );
}

function DashboardLayout({ path, navigate, token, user, onLogout }) {
  return (
    <section className="dashboard-shell">
      <aside className="glass sidebar">
        <p className="sidebar-title">Dashboard</p>
        <button className={path === '/dashboard' ? 'active' : ''} onClick={() => navigate('/dashboard')}>Overview</button>
        <button className={path === '/resume-builder' ? 'active' : ''} onClick={() => navigate('/resume-builder')}>Resume Builder</button>
        <button className={path === '/interview' ? 'active' : ''} onClick={() => navigate('/interview')}>Interview Coach</button>
        <button className="logout" onClick={onLogout}>Logout</button>
      </aside>

      <div className="dashboard-main">
        {path === '/dashboard' ? <DashboardHome token={token} user={user} navigate={navigate} /> : null}
        {path === '/resume-builder' ? <ResumeBuilderPage token={token} /> : null}
        {path === '/interview' ? <InterviewPage token={token} /> : null}
      </div>
    </section>
  );
}

function DashboardHome({ token, user, navigate }) {
  const [profile, setProfile] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    apiRequest('/api/users/me', { token }).then(setProfile).catch(() => {});
    apiRequest('/api/interview/history', { token }).then(setHistory).catch(() => {});
  }, [token]);

  return (
    <>
      <h2>Welcome {user?.name || 'User'}</h2>
      <div className="stats-grid">
        <article className="glass stat-card"><h3>Resume Score</h3><p>89</p></article>
        <article className="glass stat-card"><h3>Profile Completion</h3><p>{profile?.profileCompletion || 75}%</p></article>
        <article className="glass stat-card"><h3>Recent Activity</h3><p>{history.length} interview sessions</p></article>
      </div>
      <button className="btn btn-primary" onClick={() => navigate('/interview')}>Start AI Interview</button>
    </>
  );
}

function ResumeBuilderPage({ token }) {
  const [file, setFile] = useState(null);
  const [resume, setResume] = useState(null);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const upload = async () => {
    if (!file) return;
    setError('');
    try {
      const form = new FormData();
      form.append('file', file);
      const uploaded = await apiRequest('/api/resume/upload', {
        method: 'POST',
        body: form,
        token,
        isForm: true,
      });
      setResume(uploaded);
      setResult(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const analyze = async () => {
    if (!resume?.id) return;
    setError('');
    try {
      const analyzed = await apiRequest('/api/resume/analyze', {
        method: 'POST',
        token,
        body: { resumeId: resume.id },
      });
      setResult(analyzed);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <h2>Resume Builder</h2>
      <div className="glass tool-card">
        <label>Upload PDF Resume</label>
        <input type="file" accept="application/pdf" onChange={(e) => setFile(e.target.files?.[0] || null)} />
        <div className="hero-actions">
          <button className="btn btn-ghost" onClick={upload}>Upload PDF</button>
          <button className="btn btn-primary" onClick={analyze} disabled={!resume}>AI Analyze</button>
        </div>
        {error ? <p className="form-error">{error}</p> : null}
        {resume ? <p className="muted">Uploaded: {resume.fileUrl}</p> : null}
        {result ? <p className="muted">ATS Score: {result.atsScore} | Feedback: {result.aiFeedback}</p> : null}
      </div>
    </div>
  );
}

function InterviewPage({ token }) {
  const [interview, setInterview] = useState(null);
  const [answer, setAnswer] = useState('');
  const [error, setError] = useState('');

  const start = async () => {
    setError('');
    try {
      const data = await apiRequest('/api/interview/start', {
        method: 'POST',
        token,
        body: { title: 'Frontend Engineer Mock Interview' },
      });
      setInterview(data);
      setAnswer('');
    } catch (err) {
      setError(err.message);
    }
  };

  const submitAnswer = async () => {
    if (!interview || !answer.trim()) return;
    const question = interview.questions.find((q) => !q.userAnswer) || interview.questions[interview.questions.length - 1];
    if (!question) return;

    setError('');
    try {
      const data = await apiRequest('/api/interview/answer', {
        method: 'POST',
        token,
        body: {
          interviewId: interview.id,
          questionId: question.id,
          answer,
        },
      });
      setInterview(data);
      setAnswer('');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <h2>AI Interview Coach</h2>
      <div className="glass tool-card">
        <button className="btn btn-primary" onClick={start}>Start Interview Session</button>

        {interview ? (
          <>
            <div className="chat-area">
              {interview.questions.map((q) => (
                <div key={q.id}>
                  <div className="bubble ai">AI: {q.questionText}</div>
                  {q.userAnswer ? <div className="bubble user">You: {q.userAnswer}</div> : null}
                  {q.aiFeedback ? <div className="bubble feedback">Feedback: {q.aiFeedback} {q.score ? `(${q.score}/100)` : ''}</div> : null}
                </div>
              ))}
            </div>
            <textarea
              placeholder="Write your answer"
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              rows={4}
            />
            <button className="btn btn-ghost" onClick={submitAnswer}>Submit Answer</button>
            <p className="muted">Current Score: {interview.totalScore || 0}</p>
          </>
        ) : (
          <p className="muted">Start a session to receive AI interview questions.</p>
        )}

        {error ? <p className="form-error">{error}</p> : null}
      </div>
    </div>
  );
}

function RedirectCard({ text, buttonText, onClick }) {
  return (
    <section className="auth-wrap glass">
      <h2>{text}</h2>
      <button className="btn btn-primary" onClick={onClick}>{buttonText}</button>
    </section>
  );
}

function AuthRequired({ navigate }) {
  return (
    <section className="auth-wrap glass">
      <h2>Authentication required</h2>
      <p className="muted">Please login to access dashboard features.</p>
      <button className="btn btn-primary" onClick={() => navigate('/login')}>Go to Login</button>
    </section>
  );
}

function isProtected(path) {
  return path === '/dashboard' || path === '/resume-builder' || path === '/interview';
}

export default App;
