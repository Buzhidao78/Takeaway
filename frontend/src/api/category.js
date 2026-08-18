import api from './index'

export const categoryList = () => api.get('/category/list')
export const categoryListAll = () => api.get('/admin/category/all')
export const getStoresByCategory = (categoryId, page = 1, size = 10) =>
  api.get(`/category/${categoryId}/stores`, { params: { page, size } })
export const getCategoryStats = () => api.get('/category/stats')
export const addCategory = (data) => api.post('/admin/category', data)
export const updateCategory = (id, data) => api.put(`/admin/category/${id}`, data)
export const deleteCategory = (id) => api.delete(`/admin/category/${id}`)
