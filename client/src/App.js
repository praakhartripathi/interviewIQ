import './App.css';

function App() {
  const features = [
    {
      title: 'AI Resume Builder',
      description: 'Generate tailored resumes from your profile, role target, and experience.',
      icon: 'RB',
    },
    {
      title: 'ATS Score Analyzer',
      description: 'Get instant ATS compatibility scoring with keyword-level recommendations.',
      icon: 'AS',
    },
    {
      title: 'AI Mock Interviews',
      description: 'Practice role-based interviews with adaptive follow-up questions.',
      icon: 'MI',
    },
    {
      title: 'Skill Gap Analysis',
      description: 'Compare your profile against top job descriptions and identify missing skills.',
      icon: 'SG',
    },
    {
      title: 'Resume Templates',
      description: 'Use polished, ATS-friendly templates optimized for modern hiring systems.',
      icon: 'RT',
    },
    {
      title: 'Instant Feedback',
      description: 'Receive concise, actionable feedback on answers, structure, and confidence.',
      icon: 'IF',
    },
  ];

  const steps = [
    { title: 'Upload Resume', detail: 'Drop your current resume or import profile details in seconds.' },
    { title: 'AI Analysis', detail: 'Get ATS score, keyword guidance, and role-specific improvements.' },
    { title: 'Interview Practice', detail: 'Practice AI mock interviews and improve with instant feedback.' },
  ];

  return (
    <main className="landing">
      <div className="bg-orb orb-1" />
      <div className="bg-orb orb-2" />
      <div className="bg-orb orb-3" />

      <div className="container">
        <nav className="navbar glass">
          <div className="brand">
            <div className="brand-logo">IQ</div>
            <span className="brand-name">InterviewIQ</span>
          </div>
          <div className="nav-links">
            <a href="#features">Features</a>
            <a href="#pricing">Pricing</a>
            <a href="#login">Login</a>
          </div>
          <button className="btn btn-light">Get Started</button>
        </nav>

        <section className="hero">
          <div className="hero-copy fade-up">
            <p className="eyebrow">AI Resume Builder + AI Interview Coach</p>
            <h1>Build AI-Powered Resumes &amp; Crack Interviews Faster</h1>
            <p className="hero-subtext">
              Create high-impact resumes with AI-driven optimization, then practice mock interviews with real-time feedback to improve confidence and conversion rates.
            </p>
            <div className="hero-actions">
              <button className="btn btn-primary">Build Resume</button>
              <button className="btn btn-ghost">Start AI Interview</button>
            </div>
          </div>

          <div className="dashboard glass fade-up delayed">
            <div className="row between">
              <p className="muted-title">Resume Intelligence Dashboard</p>
              <span className="pill">Live Analysis</span>
            </div>
            <div className="score-card panel">
              <p className="label">ATS Score</p>
              <div className="row between end">
                <p className="score">89</p>
                <p className="growth">+24 this week</p>
              </div>
              <div className="progress-track">
                <div className="progress-fill" />
              </div>
            </div>
            <div className="feedback panel">
              <p className="label">AI Feedback</p>
              <ul>
                <li>Add quantified impact in your last two bullet points.</li>
                <li>Include React + Spring Boot keywords for ATS matching.</li>
                <li>Shorten summary to 3 lines for stronger readability.</li>
              </ul>
            </div>
          </div>
        </section>

        <section id="features" className="section">
          <h2>Everything you need to get interview-ready</h2>
          <p className="section-subtext">
            Built for developers and job seekers who want strong resumes and focused interview preparation.
          </p>
          <div className="feature-grid">
            {features.map((feature) => (
              <article key={feature.title} className="feature-card glass">
                <div className="feature-icon">{feature.icon}</div>
                <h3>{feature.title}</h3>
                <p>{feature.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="section">
          <h2>How it works</h2>
          <div className="steps-grid">
            {steps.map((step, idx) => (
              <article key={step.title} className="step-card glass">
                <p className="step-id">Step {idx + 1}</p>
                <h3>{step.title}</h3>
                <p>{step.detail}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="section">
          <div className="chat-preview glass">
            <h2>Interview Practice Preview</h2>
            <div className="chat-box panel">
              <div className="bubble ai">AI Coach: Tell me about a project where you improved performance. What bottleneck did you identify first?</div>
              <div className="bubble user">You: I profiled our API and found repeated DB queries in a loop. I introduced batching and reduced response time by 42%.</div>
              <div className="bubble ai-alt">AI Coach: Great structure. Add metric context: request volume and user impact for stronger storytelling.</div>
            </div>
          </div>
        </section>

        <section className="section">
          <div className="stats-grid">
            <article className="stat-card glass">
              <p className="stat-value">+37%</p>
              <p>Average resume score increase after AI optimization</p>
            </article>
            <article className="stat-card glass">
              <p className="stat-value">2.8x</p>
              <p>Higher interview callback rate across active users</p>
            </article>
            <article className="stat-card glass">
              <p className="stat-value">10k+</p>
              <p>Mock interview sessions completed every month</p>
            </article>
          </div>
        </section>

        <section id="pricing" className="section">
          <div className="final-cta glass">
            <h2>Start Your AI Career Journey Today</h2>
            <p>Build a better resume, practice interviews with AI, and land your next role with confidence.</p>
            <div className="hero-actions center">
              <button className="btn btn-light">Get Started Free</button>
              <button id="login" className="btn btn-ghost">Book Demo</button>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}

export default App;
