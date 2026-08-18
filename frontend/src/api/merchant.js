import api from './index'

export const getStore = () => api.get('/merchant/store')
export const updateStore = (data) => api.put('/merchant/store', data)
export const updateStoreStatus = (status) => api.put('/merchant/store/status', { status })
export const getDashboardData = () => api.get('/merchant/dashboard')
export const categories = () => api.get('/merchant/categories')
export const saveCategory = (data) => api.post('/merchant/category', data)
export const deleteCategory = (id) => api.delete(`/merchant/category/${id}`)
export const dishes = (categoryId) => api.get('/merchant/dishes', { params: { categoryId } })
export const saveDish = (data) => api.post('/merchant/dish', data)
export const deleteDish = (id) => api.delete(`/merchant/dish/${id}`)
export const orders = (status, page, size) => api.get('/merchant/orders', { params: { status, page, size } })
export const orderDetail = (id) => api.get(`/merchant/order/${id}`)
export const updateOrderStatus = (id, status) => api.put(`/merchant/order/${id}/status`, { status })

// 配送配置
export const getMerchantConfig = () => api.get('/merchant/delivery/config')
export const saveMerchantConfig = (data) => api.post('/merchant/delivery/config', data)
export const getDeliveryZones = () => api.get('/merchant/delivery/zones')
export const saveDeliveryZone = (data) => api.post('/merchant/delivery/zones', data)
export const deleteDeliveryZone = (id) => api.delete(`/merchant/delivery/zones/${id}`)

// 收益
export const getEarningsStats = () => api.get('/merchant/earnings/stats')
export const getEarnings = (params = {}) => api.get('/merchant/earnings', { params })
