import api from './index'

export const createOrder = (data) =>
  api.post('/order/create', data)
export const myOrders = (status, page = 1, size = 10) =>
  api.get('/order/my', { params: { status, page, size } })
export const orderDetail = (id) => api.get(`/order/${id}`)
export const payOrder = (id) => api.post(`/order/${id}/pay`)
export const cancelOrder = (id) => api.post(`/order/${id}/cancel`)
export const submitReview = (orderId, rating, content) =>
  api.post('/review', { orderId, rating, content })
export const getMyReviews = (page = 1, size = 10) =>
  api.get('/review/my', { params: { page, size } })
