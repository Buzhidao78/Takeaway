import api from './index'

export const cartList = () => api.get('/cart/list')
export const addCart = (storeId, dishId, quantity = 1) =>
  api.post('/cart/add', { storeId, dishId, quantity })
export const updateCartQty = (cartId, quantity) =>
  api.put(`/cart/${cartId}/quantity`, { quantity })
export const removeCart = (cartId) => api.delete(`/cart/${cartId}`)
