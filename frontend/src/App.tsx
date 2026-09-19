import { useEffect, useState } from "react";
import "./App.css";
import { getReviews } from "./api";

type Finding = {
  id: number;
  filePath: string;
  lineNumber: number;
  category: string;
  severity: string;
  title: string;
  description: string;
  suggestedFix: string;
  confidence: number;
};

type Review = {
  id: number;
  repository: string;
  pullRequestNumber: number;
  commitSha: string;
  status: string;
  provider: string;
  createdAt: string;
  completedAt: string | null;
  findings: Finding[];
};

function App() {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [selectedReview, setSelectedReview] = useState<Review | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    getReviews()
      .then((data) => {
        setReviews(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError("Failed to load reviews");
        setLoading(false);
      });
  }, []);

  const totalFindings = reviews.reduce(
    (total, review) => total + review.findings.length,
    0
  );

  const highSeverityCount = reviews.reduce(
    (total, review) =>
      total +
      review.findings.filter((finding) => finding.severity === "HIGH").length,
    0
  );

  const latestReview = reviews.length > 0 ? reviews[reviews.length - 1] : null;

  if (loading) {
    return (
      <div className="app">
        <div className="loading">Loading DevAssist...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="app">
        <div className="error">{error}</div>
      </div>
    );
  }

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">D</div>
          <span>DevAssist</span>
        </div>

        <div className="topbar-right">
          <span className="status-dot"></span>
          <span>AI Review Engine Online</span>
        </div>
      </header>

      <main className="container">
        <section className="hero">
          <div>
            <p className="eyebrow">AI-POWERED CODE REVIEW</p>
            <h1>Ship better code with AI.</h1>
            <p className="hero-text">
              DevAssist automatically analyzes pull requests, detects potential
              issues, and provides actionable fixes.
            </p>
          </div>

          <div className="hero-badge">
            <span>●</span>
            Groq AI
          </div>
        </section>

        <section className="stats-grid">
          <div className="stat-card">
            <span className="stat-label">Total Reviews</span>
            <strong>{reviews.length}</strong>
          </div>

          <div className="stat-card">
            <span className="stat-label">Total Findings</span>
            <strong>{totalFindings}</strong>
          </div>

          <div className="stat-card">
            <span className="stat-label">High Severity</span>
            <strong>{highSeverityCount}</strong>
          </div>

          <div className="stat-card">
            <span className="stat-label">AI Provider</span>
            <strong>{latestReview?.provider || "—"}</strong>
          </div>
        </section>

        <section className="reviews-section">
          <div className="section-header">
            <div>
              <p className="eyebrow">HISTORY</p>
              <h2>Recent Reviews</h2>
            </div>

            <span className="review-count">
              {reviews.length} reviews
            </span>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Review</th>
                  <th>Repository</th>
                  <th>PR</th>
                  <th>Status</th>
                  <th>Findings</th>
                  <th>Provider</th>
                </tr>
              </thead>

              <tbody>
                {reviews.map((review) => (
                <tr
                  key={review.id}
                  onClick={() => setSelectedReview(review)}
                  className="review-row"
                >
                    <td>
                      <strong>#{review.id}</strong>
                    </td>

                    <td>{review.repository}</td>

                    <td>#{review.pullRequestNumber}</td>

                    <td>
                      <span
                        className={`status status-${review.status.toLowerCase()}`}
                      >
                        {review.status}
                      </span>
                    </td>

                    <td>{review.findings.length}</td>

                    <td>{review.provider}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
        {selectedReview && (
        <section className="review-detail">
          <div className="section-header">
            <div>
              <p className="eyebrow">REVIEW DETAILS</p>
              <h2>
                Review #{selectedReview.id}
              </h2>
              <p className="detail-repo">
                {selectedReview.repository} · PR #
                {selectedReview.pullRequestNumber}
              </p>
            </div>

            <button
              className="close-button"
              onClick={() => setSelectedReview(null)}
            >
              Close
            </button>
          </div>

          <div className="findings-list">
            {selectedReview.findings.length === 0 ? (
              <div className="empty-state">
                No issues were found in this review.
              </div>
            ) : (
              selectedReview.findings.map((finding) => (
                <div className="finding-card" key={finding.id}>
                  <div className="finding-header">
                    <div>
                      <span
                        className={`severity severity-${finding.severity.toLowerCase()}`}
                      >
                        {finding.severity}
                      </span>

                      <span className="category">
                        {finding.category}
                      </span>
                    </div>

                    <span className="confidence">
                      {Math.round(finding.confidence * 100)}% confidence
                    </span>
                  </div>

                  <h3>{finding.title}</h3>

                  <p>{finding.description}</p>

                  <div className="finding-meta">
                    <span>
                      📄 {finding.filePath || "Unknown file"}
                    </span>

                    {finding.lineNumber && (
                      <span>
                        Line {finding.lineNumber}
                      </span>
                    )}
                  </div>

                  {finding.suggestedFix && (
                    <div className="suggested-fix">
                      <strong>Suggested Fix</strong>
                      <p>{finding.suggestedFix}</p>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </section>
      )}
      </main>
    </div>
  );
}

export default App;