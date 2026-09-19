const API_BASE_URL = "http://localhost:8080";

export async function getReviews() {
  const response = await fetch(`${API_BASE_URL}/api/reviews`);

  if (!response.ok) {
    throw new Error("Failed to fetch reviews");
  }

  return response.json();
}

export async function getReview(reviewId: number) {
  const response = await fetch(
    `${API_BASE_URL}/api/reviews/${reviewId}`
  );

  if (!response.ok) {
    throw new Error("Failed to fetch review");
  }

  return response.json();
}