import api from './index'

export const getStoreReviews = (storeId, page = 1, size = 10, ratingFilter = null) =>
  api.get(`/review/store/${storeId}`, { params: { page, size, ratingFilter } })

export const getMerchantReviews = (params = {}) =>
  api.get('/product-review/merchant', { params })

export const replyReview = (reviewId, content) =>
  api.post('/product-review/merchant/reply', { reviewId, content })

export const updateReviewStatus = (reviewId, status) =>
  api.put('/merchant/review/status', { reviewId, status })
