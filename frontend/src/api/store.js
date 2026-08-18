import api from './index'

export const storeList = (page = 1, size = 10, keyword) =>
  api.get('/store/list', { params: { page, size, keyword } })
export const storeDetail = (id) => api.get(`/store/detail/${id}`)
export const categories = (storeId) => api.get(`/store/${storeId}/categories`)
export const dishes = (storeId, categoryId) =>
  api.get(`/store/${storeId}/dishes`, { params: { categoryId } })
export const getDishDetail = (id) => api.get(`/store/dish/${id}`)
export const getStoreCategories = (storeId) => api.get(`/admin/store/${storeId}/categories`)
export const bindStoreCategories = (storeId, categoryIds) => api.post(`/admin/store/${storeId}/categories`, categoryIds)
