import api from './index'

export const getDishReviews = (dishId, params = {}) =>
  api.get(`/product-review/dish/${dishId}`, { params })

export const getStoreReviews = (storeId, params = {}) =>
  api.get(`/product-review/store/${storeId}`, { params })

export const getMyReviews = (params = {}) =>
  api.get('/product-review/my', { params })

export const getOrderReviews = (orderId) =>
  api.get(`/product-review/order/${orderId}`)

export const submitReview = (data) =>
  api.post('/product-review', data)

export const replyReview = (reviewId, content) =>
  api.post('/product-review/merchant/reply', { reviewId, content })

export const likeReview = (reviewId) =>
  api.post(`/product-review/${reviewId}/like`)

export const dislikeReview = (reviewId) =>
  api.post(`/product-review/${reviewId}/dislike`)

export const getUserVote = (reviewId) =>
  api.get(`/product-review/${reviewId}/vote`)
